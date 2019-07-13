package org.tmt.embedded_keycloak.impl

import java.util.{Timer, TimerTask}

import org.tmt.embedded_keycloak.Settings
import requests.Response

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Future, Promise}
import scala.util.Try
import scala.util.control.NonFatal

private[embedded_keycloak] class HealthCheck(settings: Settings) {
  def checkHealth(): Future[Unit] = retry[Response](makeCall()).map(_ => ())

  private def retry[T](f: => Future[T], attempts: Int = 10, interval: FiniteDuration = 3.seconds): Future[T] = {
    f.recoverWith {
      case NonFatal(_) if attempts > 0 => delay(interval) { retry(f, attempts - 1, interval) }.flatten
    }
  }

  def delay[T](delay: FiniteDuration)(block: => T): Future[T] = {
    val promise = Promise[T]()
    val t       = new Timer()
    t.schedule(new TimerTask {
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
