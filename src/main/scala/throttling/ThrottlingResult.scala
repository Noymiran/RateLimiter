package throttling

import scala.concurrent.duration.FiniteDuration

sealed trait ThrottlingResult


object ThrottlingResult {

  case class FilteredOut(retryInterval: FiniteDuration) extends ThrottlingResult

  case object NotFiltered extends ThrottlingResult

}
