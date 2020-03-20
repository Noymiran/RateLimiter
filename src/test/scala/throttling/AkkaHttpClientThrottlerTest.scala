package throttling

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.concurrent.ConductorMethods
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers
import throttling.ThrottlingResult.{FilteredOut, NotFiltered}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps


class AkkaHttpClientThrottlerTest extends AnyFunSpecLike with ConductorMethods with Matchers with MockitoSugar {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher
  val httpRequest = HttpRequest(uri = "testRequest")
  val retryDuration: FiniteDuration = 1 minute
  val throttler: Throttler = mock[Throttler]
  val testedHttpClientThrottler = new AkkaHttpClientThrottler(throttler)

  describe("AkkaHttpClientThrottler Tests") {
    it("requestHandler should return statusCode= 200") {
      when(throttler.shouldThrottle) thenReturn NotFiltered
      val response = testedHttpClientThrottler.requestHandler(httpRequest)
      response.status should be(StatusCodes.OK)
    }

    it("requestHandler should return statusCode= 429") {
      when(throttler.shouldThrottle) thenReturn FilteredOut(retryDuration)
      val response = testedHttpClientThrottler.requestHandler(httpRequest)
      response.status should be(StatusCodes.TooManyRequests)
    }
  }

}
