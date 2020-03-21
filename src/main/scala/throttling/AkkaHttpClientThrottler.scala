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
                             (implicit system: ActorSystem, mat: Materializer, ec: ExecutionContext) extends Logging with HttpClient[HttpRequest, HttpResponse] with Throttler[String] {
  private val usersRateLimiters: concurrent.Map[String, GenericRateLimiter] = new ConcurrentHashMap[String, GenericRateLimiter]().asScala

  override def shouldThrottle(key: String): Option[ThrottlingResult] =  {
    usersRateLimiters.get(key).map {
      rateLimiter =>
        if (rateLimiter.tryAcquire) ThrottlingResult.NotFiltered
        else
          ThrottlingResult.FilteredOut(rateLimiter.retryInterval)
    }
  }

  override def requestHandler: HttpRequest => HttpResponse =  {
    case r: HttpRequest =>
      r.discardEntityBytes()
      log.info(r.toString())
      val maybeHttpResponse = IpUtils.getIpFromRequest(r).map(
        ip => synchronized{
          if (usersRateLimiters.get(ip).isEmpty)
            usersRateLimiters += ((ip, genericRateLimiter.copyRateLimiter))
          ip
        }).flatMap(shouldThrottle(_).map {
        case ThrottlingResult.NotFiltered =>
          HttpResponse(StatusCodes.OK)
        case ThrottlingResult.FilteredOut(retryInterval) =>
          HttpResponse(StatusCodes.TooManyRequests, entity = s"Rate limit exceeded. Try again in ${retryInterval.toSeconds} seconds")
      })
      maybeHttpResponse.getOrElse(HttpResponse(StatusCodes.Unauthorized))
  }


  def serverBidingRequests(host: String, port: Int): Future[ServerBinding] =
    Http().bindAndHandleSync(requestHandler, host, port)

  def shutDown(serverBiding: Future[ServerBinding]): Unit = {
    serverBiding
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}

