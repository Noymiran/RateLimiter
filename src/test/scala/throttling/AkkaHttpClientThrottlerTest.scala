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


class AkkaHttpClientThrottlerTest extends AnyFunSpecLike with ConductorMethods with Matchers with MockitoSugar {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val retryInterval: FiniteDuration = 1 minute

  var simpleRateLimiter: SimpleRateLimiter = mock[SimpleRateLimiter]
  when(simpleRateLimiter.copyRateLimiter) thenReturn (simpleRateLimiter)
  val testedHttpClientThrottler = new AkkaHttpClientThrottler(simpleRateLimiter)

  def randomIp: String = "100.0.0." + Random.nextInt(255)

  def headerIp(ip: String): `X-Forwarded-For` = `X-Forwarded-For`(RemoteAddress(InetAddress.getByName(ip)))

  describe("AkkaHttpClientThrottler Tests") {
    val ip: String = randomIp
    val key=HttpKey(ip)
    val httpRequest = HttpRequest(uri = "testRequest", headers = Seq(headerIp(ip)))

    it("requestHandler should return statusCode= 200") {
      when(simpleRateLimiter.tryAcquire) thenReturn true
      val response = testedHttpClientThrottler.requestHandler(httpRequest)
      response.status should be(StatusCodes.OK)
    }

    it("requestHandler should return statusCode= 429") {
      when(simpleRateLimiter.tryAcquire) thenReturn false
      when(simpleRateLimiter.retryInterval) thenReturn (retryInterval)
      val response = testedHttpClientThrottler.requestHandler(httpRequest)
      response.status should be(StatusCodes.TooManyRequests)
    }

    it("request without Ip: requestHandler should return statusCode= 401") {
      val httpRequest = HttpRequest(uri = "testRequest")
      val response = testedHttpClientThrottler.requestHandler(httpRequest)
      response.status should be(StatusCodes.Unauthorized)
    }

    it("shouldThrottle: FilteredOut") {
      when(simpleRateLimiter.tryAcquire) thenReturn false
      when(simpleRateLimiter.retryInterval) thenReturn retryInterval
      testedHttpClientThrottler.shouldThrottle(key) should be(Some(ThrottlingResult.FilteredOut(retryInterval)))
    }

    it("shouldThrottle: NotFiltered") {
      when(simpleRateLimiter.tryAcquire) thenReturn true
      testedHttpClientThrottler.shouldThrottle(key) should be(Some(ThrottlingResult.NotFiltered))
    }

    it("Test 2 users") {
      val duration = 1 hour
      val maxPermits = 1
      val simpleRateLimiterNew: SimpleRateLimiter = SimpleRateLimiter(maxPermits, duration)
      val testedHttpClientThrottlerNew = new AkkaHttpClientThrottler(simpleRateLimiterNew)

      val ip1: String = randomIp
      val httpRequest1 = HttpRequest(uri = "testRequest", headers = Seq(headerIp(ip1)))

      val response1User1 = testedHttpClientThrottlerNew.requestHandler(httpRequest1)
      response1User1.status should be(StatusCodes.OK)

      val response2User1 = testedHttpClientThrottlerNew.requestHandler(httpRequest1)
      response2User1.status should be(StatusCodes.TooManyRequests)

      val ip2: String = randomIp
      val httpRequest2 = HttpRequest(uri = "testRequest", headers = Seq(headerIp(ip2)))

      val response1User2 = testedHttpClientThrottlerNew.requestHandler(httpRequest2)
      response1User2.status should be(StatusCodes.OK)

      val response2User2 = testedHttpClientThrottlerNew.requestHandler(httpRequest2)
      response2User2.status should be(StatusCodes.TooManyRequests)
    }
  }
}
