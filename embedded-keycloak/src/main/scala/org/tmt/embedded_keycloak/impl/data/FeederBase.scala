package org.tmt.embedded_keycloak.impl.data

import org.tmt.embedded_keycloak.Settings
import org.tmt.embedded_keycloak.utils.BearerToken
import requests._
import ujson.{Bool, Obj, Value}
import upickle.default.{macroRW, ReadWriter => RW}

//import scala.collection.mutable.{LinkedHashMap => MutableMap}
import upickle.core.{LinkedHashMap => MutableMap}
import scala.language.implicitConversions

private[embedded_keycloak] abstract class FeederBase(settings: Settings) {

  import settings._

  protected val jTrue: Bool  = Bool(true)
  protected val jFalse: Bool = Bool(false)

  case class RoleRepresentation(name: String, id: String, containerId: String, composite: Boolean, clientRole: Boolean)

  object RoleRepresentation {
    implicit val rw: RW[RoleRepresentation] = macroRW
  }

  protected def kPost(url: String, data: String)(implicit bearerToken: BearerToken): Response = {
    sendRequest("POST", url, data)
  }

  protected def kPut(url: String, data: String)(implicit bearerToken: BearerToken): Response = {
    sendRequest("PUT", url, data)
  }

  protected def kGet(url: String)(implicit bearerToken: BearerToken): Response = {
    sendRequest("GET", url)
  }

  protected def realmUrl = s"http://localhost:$port/auth/admin/realms"

  protected def realmUrl(realmName: String): String = s"http://localhost:$port/auth/admin/realms/$realmName"

  protected given Conversion[Map[String, Value], MutableMap[String, Value]] = map => {
    val mutableMap = MutableMap[String, Value]()
    map.foreach(mutableMap += _)
    mutableMap
  }

//  protected implicit def toMutableMap(map: Map[String, Value]): MutableMap[String, Value] = {
//    val mutableMap = MutableMap[String, Value]()
//    map.foreach(mutableMap += _)
//    mutableMap
//  }

  protected def getId(response: Response): String = {
    val url = response.headers("location").head
    url.split("/").last
  }

  protected given Conversion[Map[String, Value], String] = map => ujson.write(Obj(map))

  protected implicit def requester(method: String): Requester = method match {
    case "POST" => post
    case "GET"  => get
    case "PUT"  => put
  }

  private def sendRequest(requester: Requester, url: String, stringData: String = null)( // scalastyle:ignore
      implicit bearerToken: BearerToken
  ): Response = {
    val response = requester(
      url = url,
      auth = bearerToken,
      data =
        if (stringData == null || stringData.isBlank) RequestBlob.EmptyRequestBlob
        else stringData,
      headers = Map("Content-Type" -> "application/json")
    )

    lazy val error = s"""
                        | request failed
                        | ${requester.verb.toLowerCase} $url
                        | response status: ${response.statusCode},
                        | response text: ${response.text()}
    """.stripMargin

    if (!Set(200, 201, 204).contains(response.statusCode)) throw new RuntimeException(error)
    else response
  }
}
