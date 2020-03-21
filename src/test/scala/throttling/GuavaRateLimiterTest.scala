package throttling

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ConductorMethods
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class GuavaRateLimiterTest extends AnyFunSpecLike with BeforeAndAfter with ConductorMethods with Matchers {
  var maxPermits: Int = _
  val duration = 2 minute
  var testedRateLimiter: GuavaRateLimiter = _


  before {
    maxPermits = 1
    testedRateLimiter = new GuavaRateLimiter(maxPermits, duration)
  }

  describe("GuavaRateLimiter Tests") {
    it("First request: tryAcquire=true and Second request: tryAcquire=false") {
      testedRateLimiter.tryAcquire should be(true)
      testedRateLimiter.tryAcquire should be(false)
    }

    it("retryInterval: 1 minute") {
      maxPermits = 1
      testedRateLimiter = new GuavaRateLimiter(maxPermits, duration)
      Thread.sleep(6000)
      testedRateLimiter.retryInterval.toMinutes should be(1)
    }
  }

}
