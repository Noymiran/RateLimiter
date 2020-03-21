package throttling

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ConductorMethods
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class GuavaRateLimiterTest extends AnyFunSpecLike with BeforeAndAfter with ConductorMethods with Matchers {
  val maxPermits: Int = 1
  val duration = 1 minute
  var testedRateLimiter: GuavaRateLimiter = _


  before {
    testedRateLimiter = GuavaRateLimiter(maxPermits, duration)
  }

  describe("GuavaRateLimiter Tests") {
    it("First request: tryAcquire=true and Second request: tryAcquire=false") {
      testedRateLimiter.tryAcquire should be(true)
      testedRateLimiter.tryAcquire should be(false)
    }

    it("retryInterval: 1 minute") {
      testedRateLimiter.tryAcquire should be(true)
      testedRateLimiter.retryInterval.toSeconds should be(59)
    }
  }

}
