package tech.bilal.embedded_keycloak.impl

import requests.Response
import retry.Success
import tech.bilal.embedded_keycloak.Settings

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

private[embedded_keycloak] class HealthCheck(settings: Settings) {
  def checkHealth(): Future[Unit] = {

    implicit val success: Success[Response] =
      Success[Response](_.statusCode == 200)

    val f = retry
      .Backoff()
      .apply(makeCall())
      .map(_ => ())
    f
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
