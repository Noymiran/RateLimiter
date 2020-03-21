package throttling

object Utils {
  val defaultRuns = 1000

  def forall(block: Int => Unit): Unit = (1 to defaultRuns) foreach block
}
