package throttling


class ThrottlerService(rateLimiter: GenericRateLimiter) extends Throttler {
  override def shouldThrottle: ThrottlingResult =
    if (rateLimiter.tryAcquire) ThrottlingResult.NotFiltered
    else ThrottlingResult.FilteredOut(rateLimiter.retryInterval)
}
