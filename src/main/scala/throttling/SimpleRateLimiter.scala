package throttling

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import akka.actor.ActorSystem

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Deadline, Duration, FiniteDuration}


class SimpleRateLimiter(override val maxPermits: Int, override val duration: FiniteDuration)(implicit system: ActorSystem, ec: ExecutionContext) extends GenericRateLimiter with Logging {
  val counter: AtomicInteger = new AtomicInteger(0)
  val time: AtomicReference[Deadline] = new AtomicReference(Deadline.now)
  val runningScheduler = system.scheduler.scheduleAtFixedRate(Duration.Zero, duration)(runnable = () => {
    time.set(duration.fromNow)
    counter.set(0)
  })


  override def tryAcquire: Boolean =
    if (counter.incrementAndGet() < maxPermits + 1) true
    else false

  override def retryInterval: FiniteDuration = time.get().timeLeft

  override def copyRateLimiter: GenericRateLimiter = new SimpleRateLimiter(maxPermits, duration)
}

