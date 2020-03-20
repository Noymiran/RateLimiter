package throttling

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ConductorMethods
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers
import throttling.ThrottlingResult.NotFiltered

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

class SimpleRateLimiterTest extends AnyFunSpecLike with BeforeAndAfter with ConductorMethods with Matchers {
  var maxPermits: Int = _
  val duration = 2 minute
  var testedRateLimiter: SimpleRateLimiter = _

NotFiltered
  before {
    maxPermits = Random.between(1, 10) * 2
    testedRateLimiter = SimpleRateLimiter(maxPermits, duration)
  }

  describe("SimpleRateLimiter Tests") {
    it("parallel requests: could not acquire the maxPermits request") {
      threadNamed("t1") {
        for (_ <- 1 to maxPermits / 2)
          testedRateLimiter.tryAcquire should be(true)
      }

      threadNamed("t2") {
        for (_ <- 1 to maxPermits / 2)
          testedRateLimiter.tryAcquire should be(true)
      }

      whenFinished {
        testedRateLimiter.tryAcquire should be(false)
      }
    }

    it("serial requests: could not acquire the maxPermits request") {
      for (_ <- 1 to maxPermits) {
        testedRateLimiter.tryAcquire should be(true)

      }
      testedRateLimiter.tryAcquire should be(false)
    }

    it("retryInterval: 1 minute") {
      maxPermits = 1
      testedRateLimiter = SimpleRateLimiter(maxPermits, duration)
      Thread.sleep(6000)
      testedRateLimiter.retryInterval.toMinutes should be(1)
    }
  }

}
