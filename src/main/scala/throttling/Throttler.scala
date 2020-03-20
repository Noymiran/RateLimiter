package throttling

trait Throttler {
  def shouldThrottle: ThrottlingResult
}
