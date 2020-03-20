package throttling


trait HttpClientThrottler[Request, Response] extends HttpClient[Request, Response] {
  def throttler: Throttler
}