package throttling

import com.revinate.guava.util.concurrent.RateLimiter

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

class GuavaRateLimiter(override val maxPermits: Int, override val duration: FiniteDuration) extends GenericRateLimiter {
  val rateLimiter: RateLimiter = RateLimiter.create(maxPermits.toDouble / duration.toSeconds)

  override def tryAcquire: Boolean = rateLimiter.tryAcquire

  override def retryInterval: FiniteDuration = duration.fromNow.timeLeft

  override def copyRateLimiter: GenericRateLimiter = new GuavaRateLimiter(maxPermits, duration)
}