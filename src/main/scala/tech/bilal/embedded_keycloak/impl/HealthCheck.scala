package tech.bilal.embedded_keycloak.impl

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import retry.Success
import tech.bilal.embedded_keycloak.Settings

import scala.concurrent.{ExecutionContext, Future}

private[embedded_keycloak] class HealthCheck(settings: Settings)(
    implicit actorSystem: ActorSystem) {
  def checkHealth(): Future[Unit] = {

    implicit val ec = actorSystem.dispatcher

    implicit val success: Success[HttpResponse] =
      Success[HttpResponse](_.status == StatusCodes.OK)

    val f = retry
      .Backoff()
      .apply(() => makeCall)
      .map(_ => ())
    f
  }

  private def makeCall(implicit actorSystem: ActorSystem,
                       ec: ExecutionContext): Future[HttpResponse] = {
    println("RETRY: probing keycloak instance")
    Http().singleRequest(
      HttpRequest(HttpMethods.GET,
                  Uri(s"http://${settings.host}:${settings.port}")))
  }
}
