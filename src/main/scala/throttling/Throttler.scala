package throttling

trait Throttler[K] {
  def shouldThrottle(key:K): Option[ThrottlingResult]
}

