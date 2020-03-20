package throttling

trait HttpClient[Request, Response] {
  def requestHandler: Request => Response
}
