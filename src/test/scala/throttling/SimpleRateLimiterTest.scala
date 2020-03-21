package throttling

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ConductorMethods
import org.scalatest.funspec.AnyFunSpecLike
import org.scalatest.matchers.should.Matchers
import throttling.ThrottlingResult.NotFiltered

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.util.Random

class SimpleRateLimiterTest extends AnyFunSpecLike with BeforeAndAfter with ConductorMethods with Matchers {
  implicit val system = ActorSystem()
  implicit val executionContext = system.dispatcher

  var maxPermits: Int = _
  val duration = 2 minute
  var testedRateLimiter: SimpleRateLimiter = _

  NotFiltered
  before {
    maxPermits = Random.between(1, 10) * 2
    testedRateLimiter = new SimpleRateLimiter(maxPermits, duration)
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

    it(s"retryInterval: 1 minute") {
      maxPermits = 1
      testedRateLimiter = new SimpleRateLimiter(maxPermits, duration)
      Thread.sleep(6000)
      testedRateLimiter.retryInterval.toMinutes should be(1)
    }
  }
}
