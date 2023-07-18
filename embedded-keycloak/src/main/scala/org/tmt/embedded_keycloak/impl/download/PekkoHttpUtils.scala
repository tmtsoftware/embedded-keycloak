package org.tmt.embedded_keycloak.impl.download

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.headers.Location
import org.apache.pekko.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}

import scala.concurrent.{ExecutionContext, Future}

object PekkoHttpUtils {

  private val maxRedirectCount = 3

  // pekko-http does not support redirects - https://github.com/pekko/pekko-http/issues/195
  def singleRequestWithRedirect(req: HttpRequest)(implicit system: ActorSystem): Future[HttpResponse] = {
    implicit val ec: ExecutionContext = system.dispatcher

    def go(req: HttpRequest, count: Int): Future[HttpResponse] =
      Http().singleRequest(req).flatMap { resp =>
        resp.status match {
          case StatusCodes.Found =>
            resp
              .header[Location]
              .map { loc =>
                val newReq = req.withUri(loc.uri)
                if (count < maxRedirectCount) go(newReq, count + 1) else Http().singleRequest(newReq)
              }
              .getOrElse(throw new RuntimeException(s"location not found on 302 for ${req.uri}"))
          case _                 => Future(resp)
        }
      }

    go(req, 0)
  }

}
