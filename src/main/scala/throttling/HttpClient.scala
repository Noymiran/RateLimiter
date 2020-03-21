package throttling

import scala.concurrent.Future

trait HttpClient[Request, Response] {
  def requestHandler: Request => Future[Response]
}
