package throttling

import org.mockito.scalatest.MockitoSugar
import org.scalatest.concurrent.ConductorMethods
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.language.postfixOps


class ThrottlingServiceTest extends AnyFunSpecLike with ConductorMethods with Matchers with MockitoSugar {
  val throttler: Throttler = mock[Throttler]
  val rateLimiter: GenericRateLimiter = mock[GenericRateLimiter]
  val testedThrottlerService = new ThrottlerService(rateLimiter)

  describe("ThrottlingService Tests") {
    it("shouldThrottle: NotFiltered") {
      when(rateLimiter.tryAcquire).thenReturn(true)
      testedThrottlerService.shouldThrottle should be(ThrottlingResult.NotFiltered)
    }

    it("shouldThrottle: FilteredOut") {
      val retryInterval: FiniteDuration = 1 minute

      when(rateLimiter.tryAcquire).thenReturn(false)
      when(rateLimiter.retryInterval).thenReturn(retryInterval)
      testedThrottlerService.shouldThrottle should be(ThrottlingResult.FilteredOut(retryInterval))
    }
  }

}
