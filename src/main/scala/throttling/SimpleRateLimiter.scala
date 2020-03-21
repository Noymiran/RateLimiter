package throttling

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import akka.actor.{ActorSystem, Scheduler}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.{Deadline, Duration, FiniteDuration}


case class SimpleRateLimiter(override val maxPermits: Int, override val duration: FiniteDuration) extends GenericRateLimiter {
  val actorSystem: ActorSystem = ActorSystem("RateLimiter")
  val scheduler: Scheduler = actorSystem.scheduler
  implicit val executor: ExecutionContextExecutor = actorSystem.dispatcher
  val counter: AtomicInteger = new AtomicInteger(0)
  val time: AtomicReference[Deadline] = new AtomicReference(Deadline.now)

  scheduler.scheduleAtFixedRate(Duration.Zero, duration)(() => {
    time.set(duration.fromNow)
    counter.set(0)
  }
  )

  override def tryAcquire: Boolean = {
    if (counter.get() < maxPermits) {
      println(counter.incrementAndGet())
      true
    }
    else false
  }

  override def retryInterval: FiniteDuration = time.get().timeLeft

  override def copyRateLimiter: GenericRateLimiter =this.copy(maxPermits, duration)
}

