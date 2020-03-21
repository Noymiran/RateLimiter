package throttling

trait Throttler {
  def shouldThrottle(key: Key): Option[ThrottlingResult]
}

