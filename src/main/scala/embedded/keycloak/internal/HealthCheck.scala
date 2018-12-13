package embedded.keycloak.internal
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import embedded.keycloak.models.Settings
import retry.Success

import scala.concurrent.{ExecutionContext, Future}

class HealthCheck(settings: Settings)(implicit actorSystem: ActorSystem) {
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
