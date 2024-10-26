package cats.effect
package unsafe

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

private[unsafe] final class WorkerTraceBuffer(externalQueueTicks: Int, bufferCapacity: Int) {
  private val inProgress: Array[WorkerTrace] = new Array(externalQueueTicks)
  private val fiberRuns: Array[Int] = new Array(externalQueueTicks)
  private var fiberRunsIdx: Int = 0
  private var fiberRunsStartTimestamp: Long = -1
  private var inProgressIdx: Int = 0
  private val traceBuffer: ConcurrentLinkedQueue[WorkerTrace] =
    new ConcurrentLinkedQueue() // possibly temp solution
  private val curBufferSize: AtomicInteger = new AtomicInteger(0)

  def pushTrace(trace: WorkerTrace): Unit = { // TODO: how to make sure doesnt go over 64?
    inProgress(inProgressIdx) = trace
    inProgressIdx += 1
  }

  def pushFiberRun(fiberId: Int, timestamp: Long): Unit = {
    fiberRuns(fiberRunsIdx) = fiberId
    fiberRunsIdx += 1
    if (fiberRunsStartTimestamp == -1)
      fiberRunsStartTimestamp = timestamp
  }

  def complete(now: Long) = {
    val numFiberRuns = fiberRunsIdx + 1
    // var size = curBufferSize.get() // make better later using this line
    val timeElapsed = (now - fiberRunsStartTimestamp) / numFiberRuns
    fiberRunsStartTimestamp = -1

    var i = 0
    while (i < numFiberRuns && curBufferSize.get() <= bufferCapacity) {
      val fiberId = fiberRuns(i)
      i += 1
      val trace = WorkerTrace.RunFiber(fiberId, timeElapsed)
      traceBuffer.add(trace)
    }

    while (inProgressIdx > 0 && curBufferSize.get() <= bufferCapacity) {
      val trace = inProgress(inProgressIdx)
      inProgressIdx -= 1
      traceBuffer.add(trace)
      curBufferSize.getAndIncrement()
    }
    inProgressIdx = 0
    fiberRunsIdx = 0
  }

  def pop(): Option[WorkerTrace] = {
    val trace = traceBuffer.poll()
    if (trace != null) {
      curBufferSize.getAndDecrement()
      Some(trace)
    } else
      None
  }

}
