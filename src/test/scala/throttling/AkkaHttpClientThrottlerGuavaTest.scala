package throttling

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.`X-Forwarded-For`
import akka.http.scaladsl.model.{HttpRequest, RemoteAddress, StatusCodes}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.concurrent.ConductorMethods
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps
import scala.util.Random


class AkkaHttpClientThrottlerGuavaTest extends AnyFunSpecLike with ConductorMethods with Matchers with MockitoSugar {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val retryInterval: FiniteDuration = 1 minute

  var guavaRateLimiter: GuavaRateLimiter = mock[GuavaRateLimiter]
  when(guavaRateLimiter.copyRateLimiter) thenReturn (guavaRateLimiter)
  val testedHttpClientThrottler = new AkkaHttpClientThrottler(guavaRateLimiter)

  def randomIp: String = "100.0.0." + Random.nextInt(255)

  def headerIp(ip: String): `X-Forwarded-For` = `X-Forwarded-For`(RemoteAddress(InetAddress.getByName(ip)))

  describe("AkkaHttpClientThrottler Tests") {
    val ip: String = "100.0.0.1"
    val key = HttpKey(ip)
    val httpRequest = HttpRequest(uri = "testRequest", headers = Seq(headerIp(ip)))

    it("requestHandler should return statusCode= 200") {
      when(guavaRateLimiter.tryAcquire) thenReturn true
      val response = testedHttpClientThrottler.requestHandler(httpRequest)
      response.map(_.status should be(StatusCodes.OK))
    }

    it("requestHandler should return statusCode= 429") {
      when(guavaRateLimiter.tryAcquire) thenReturn false
      when(guavaRateLimiter.retryInterval) thenReturn (retryInterval)
      val response = testedHttpClientThrottler.requestHandler(httpRequest)
      response.map(_.status should be(StatusCodes.TooManyRequests))
    }

    it("request without Ip: requestHandler should return statusCode= 401") {
      val httpRequest = HttpRequest(uri = "testRequest")
      val response = testedHttpClientThrottler.requestHandler(httpRequest)
      response.map(_.status should be(StatusCodes.Unauthorized))
    }

    it("shouldThrottle: FilteredOut") {
      when(guavaRateLimiter.tryAcquire) thenReturn false
      when(guavaRateLimiter.retryInterval) thenReturn retryInterval
      testedHttpClientThrottler.shouldThrottle(key) should be(Some(ThrottlingResult.FilteredOut(retryInterval)))
    }

    it("shouldThrottle: NotFiltered") {
      when(guavaRateLimiter.tryAcquire) thenReturn true
      testedHttpClientThrottler.shouldThrottle(key) should be(Some(ThrottlingResult.NotFiltered))
    }
    Utils.forall { i =>
      it(s"Test 2 users $i") {
        val duration = 30 seconds
        val maxPermits = 1
        val guavaRateLimiterNew: GuavaRateLimiter = new GuavaRateLimiter(maxPermits, duration)
        val testedHttpClientThrottlerNew = new AkkaHttpClientThrottler(guavaRateLimiterNew)

        val ip1: String = "100.0.0.2"
        val httpRequest1 = HttpRequest(uri = "testRequest", headers = Seq(headerIp(ip1)))

        val response1User1 = testedHttpClientThrottlerNew.requestHandler(httpRequest1)
        response1User1.map(_.status should be(StatusCodes.OK))

        val response2User1 = testedHttpClientThrottlerNew.requestHandler(httpRequest1)
        response2User1.map(_.status should be(StatusCodes.TooManyRequests))

        val ip2: String = "100.0.0.3"
        val httpRequest2 = HttpRequest(uri = "testRequest", headers = Seq(headerIp(ip2)))

        val response1User2 = testedHttpClientThrottlerNew.requestHandler(httpRequest2)
        response1User2.map(_.status should be(StatusCodes.OK))

        val response2User2 = testedHttpClientThrottlerNew.requestHandler(httpRequest2)
        response2User2.map(_.status should be(StatusCodes.TooManyRequests))
      }
    }
  }
}
