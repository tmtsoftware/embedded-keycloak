package org.tmt.embedded_keycloak.impl

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{RestartSource, Sink, Source}
import org.tmt.embedded_keycloak.Settings
import requests.Response

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.Try
import scala.util.control.NoStackTrace

object HealthCheckFailedException extends NoStackTrace {
  override def getMessage: String = "keycloak health-check failed"
}

private[embedded_keycloak] class HealthCheck(settings: Settings)(implicit val system: ActorSystem) {

  private def restartSource(url: String, successCode: Int): Source[Response, NotUsed] =
    RestartSource.onFailuresWithBackoff(
      minBackoff = 3.seconds,
      maxBackoff = 3.seconds,
      randomFactor = 0.2,
      maxRestarts = 10
    ) { () =>
      Source
        .future {
          Future {
            val response = Try(requests.get(url)).recover(_ => throw HealthCheckFailedException).get
            if (response.statusCode != successCode) throw HealthCheckFailedException
            else response
          }
        }
        .log("Restarting ..")
    }

  def keycloakHealth(): Future[Response] = checkHealth(s"http://localhost:${settings.port}", 200)
  def checkHealth(url: String, successCode: Int): Future[Response] =
    restartSource(url, successCode).runWith(Sink.head)
}
