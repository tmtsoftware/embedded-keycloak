package org.tmt.embedded_keycloak.impl

import java.util.concurrent.atomic.AtomicInteger
import java.util.{Timer, TimerTask}

import org.tmt.embedded_keycloak.Settings
import requests.Response

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Future, Promise}
import scala.util.Try
import scala.util.control.NonFatal

private[embedded_keycloak] class HealthCheck(settings: Settings) {

  /**
    * This ID is used to generate thread names.
    */
  private val nextSerialNumber = new AtomicInteger(0)
  private def serialNumber = nextSerialNumber.getAndIncrement

  def checkHealth(): Future[Unit] = {
    val timer = new Timer(s"embedded-keyclock-timer-$serialNumber")
    retry[Response](makeCall(), timer = timer)
      .transform(_ => Try(timer.cancel()))
  }

  private def retry[T](f: => Future[T],
                       attempts: Int = 10,
                       interval: FiniteDuration = 3.seconds,
                       timer: Timer): Future[T] = {
    f.recoverWith {
      case NonFatal(_) if attempts > 0 =>
        delay(interval, timer) { retry(f, attempts - 1, interval, timer) }.flatten
    }
  }

  def delay[T](delay: FiniteDuration, timer: Timer)(block: => T): Future[T] = {
    val promise = Promise[T]()
    timer.schedule(new TimerTask {
      override def run(): Unit = {
        promise.complete(Try(block))
      }
    }, delay.toMillis)
    promise.future
  }

  private def makeCall(): Future[Response] = {
    println("RETRY: probing keycloak instance")
    Future {
      val response = requests.get(s"http://${settings.host}:${settings.port}")
      if (response.statusCode != 200)
        throw new RuntimeException("keycloak health-check failed")
      else response
    }
  }
}
