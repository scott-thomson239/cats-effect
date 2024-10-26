package cats.effect
package unsafe

sealed trait WorkerTrace

object WorkerTrace {
  case class RunFiber(fiberId: Int, timeElapsed: Long) extends WorkerTrace

}
