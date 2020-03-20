package throttling

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model._
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

class AkkaHttpClientThrottler(override val throttler: Throttler)
                             (implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends Logging with HttpClientThrottler[HttpRequest, HttpResponse] {
  override def requestHandler: HttpRequest => HttpResponse = {
    case r: HttpRequest =>
      r.discardEntityBytes()
      log.info(r.toString())
      throttler.shouldThrottle match {
        case ThrottlingResult.NotFiltered =>
          HttpResponse(StatusCodes.OK)
        case ThrottlingResult.FilteredOut(retryInterval) =>
          HttpResponse(StatusCodes.TooManyRequests, entity = s"Rate limit exceeded. Try again in ${retryInterval.toSeconds} seconds")
      }
  }

  def serverBidingRequests(host: String, port: Int): Future[ServerBinding] =
    Http().bindAndHandleSync(requestHandler, host, port)

  def shutDown(serverBiding: Future[ServerBinding]): Unit = {
    serverBiding
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}

