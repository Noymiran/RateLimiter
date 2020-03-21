package throttling

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ConductorMethods
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class SimpleRateLimiterTest extends AnyFunSpecLike with BeforeAndAfter with ConductorMethods with Matchers {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  val maxPermits: Int = 12
  val duration = 2 minute
  var testedRateLimiter: SimpleRateLimiter = _

  before {
    testedRateLimiter = SimpleRateLimiter(maxPermits, duration)
  }

  describe("SimpleRateLimiter Tests") {
    Utils.forall { i =>
      it(s"parallel requests: could not acquire the maxPermits request $i") {
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
    }
    Utils.forall { i =>
      it(s"serial requests: could not acquire the maxPermits request $i") {
        for (_ <- 1 to maxPermits) {
          testedRateLimiter.tryAcquire should be(true)
        }

        testedRateLimiter.tryAcquire should be(false)
      }
    }

    Utils.forall { i =>
      it(s"retryInterval: 1 minute $i") {
        val testedRateLimiterNew = SimpleRateLimiter(0, 1 minute)
        testedRateLimiterNew.tryAcquire should be(false)
        testedRateLimiterNew.retryInterval.toSeconds should be(59)
      }
    }
  }
}
