package throttling

object Utils {
  val defaultRuns = 100

  def forall(block: Int => Unit): Unit = (1 to defaultRuns) foreach block
}
