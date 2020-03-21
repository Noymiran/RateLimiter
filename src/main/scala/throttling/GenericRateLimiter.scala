package throttling
import scala.concurrent.duration._

trait GenericRateLimiter {
  def duration:FiniteDuration
  def maxPermits:Int
  def tryAcquire:Boolean
  def retryInterval:FiniteDuration
  def copyRateLimiter:GenericRateLimiter

}
