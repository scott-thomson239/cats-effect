package cats.effect
package unsafe

private[unsafe] trait WorkerThreadTracePoller {
  def poll(): Option[WorkerTrace]
}

object WorkerThreadTracePoller {
  def apply(traceBuffer: WorkerTraceBuffer): WorkerThreadTracePoller =
    new WorkerThreadTracePoller {
      def poll(): Option[WorkerTrace] =
        traceBuffer.pop()
    }
}
