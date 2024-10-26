package cats.effect.unsafe
package metrics

class WorkerThreadTraceSampler(threadPool: WorkStealingThreadPool[_]) {

  def getWorkerThreadTraces(): Map[Int, WorkerThreadTracePoller] =
    threadPool.traceBuffers().map {
      case (idx, traceBuffer) =>
        (idx, WorkerThreadTracePoller(traceBuffer))
    }
}
