package throttling

import java.util.concurrent.ConcurrentHashMap

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model._
import akka.stream.Materializer

import scala.collection._
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._

class AkkaHttpClientThrottler(val genericRateLimiter: GenericRateLimiter)
                             (implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends Logging with HttpClient[HttpRequest, HttpResponse] with Throttler {
  private val usersRateLimiters: concurrent.Map[Key, GenericRateLimiter] = new ConcurrentHashMap[Key, GenericRateLimiter]().asScala

  override def shouldThrottle(key: Key): Option[ThrottlingResult] = usersRateLimiters.get(key).map(rateLimiter => {
    if (rateLimiter.tryAcquire) ThrottlingResult.NotFiltered
    else
      ThrottlingResult.FilteredOut(rateLimiter.retryInterval)
  })

  private def updateIfKeyDefined(r: HttpRequest): Option[HttpKey] = {
    for {
      ip <- IpUtils.getIpFromRequest(r)
      key = HttpKey(ip)
    } yield {
      usersRateLimiters.putIfAbsent(key, genericRateLimiter.copyRateLimiter)
      key
    }
  }

  private def generateResponse(key: _root_.throttling.HttpKey) = shouldThrottle(key) match {
    case Some(ThrottlingResult.NotFiltered) =>
      HttpResponse(StatusCodes.OK)
    case Some(ThrottlingResult.FilteredOut(retryInterval)) =>
      HttpResponse(StatusCodes.TooManyRequests, entity = s"Rate limit exceeded. Try again in ${retryInterval.toSeconds} seconds")
    case None =>
      HttpResponse(StatusCodes.InternalServerError)
  }

  override def requestHandler: HttpRequest => Future[HttpResponse] = {
    case r: HttpRequest =>
      r.discardEntityBytes()
      val response = updateIfKeyDefined(r) match {
        case Some(key) =>
          generateResponse(key)
        case None =>
          HttpResponse(StatusCodes.Unauthorized)
      }
      val futureResponse = Future.successful(response)
      futureResponse
  }

  def serverBidingRequests(host: String, port: Int): Future[ServerBinding] =
    Http().bindAndHandleAsync(requestHandler, host, port)

  def shutDown(serverBiding: Future[ServerBinding]): Unit = {
    serverBiding
      .flatMap(_.unbind())
      .onComplete(_ =>
        system.terminate())
  }

}


