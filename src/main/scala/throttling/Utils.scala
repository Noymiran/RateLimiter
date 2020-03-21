package throttling

object Utils {
  val defaultRuns = 50

  def forall(block: Int => Unit): Unit = (1 to defaultRuns) foreach block
}
