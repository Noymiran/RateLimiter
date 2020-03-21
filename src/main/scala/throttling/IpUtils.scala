package throttling

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.`X-Forwarded-For`

import scala.util.Try

object IpUtils extends Logging {

  def getIpFromRequest(request: HttpRequest): Option[String] = {
    val ip = Try(request.header[`X-Forwarded-For`])
      .toEither match {
      case Left(value) => {
        log.error(value.getMessage)
        None
      }
      case Right(value) =>
        value.map(_.getAddresses.iterator().next().getAddress.get().toString.substring(1))
    }
    ip
  }}
