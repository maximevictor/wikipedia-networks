package wikicycles

import org.slf4j.LoggerFactory

/**
 * Created by mg on 10.12.14.
 */
trait LoggingUtil {

  val logger = LoggerFactory.getLogger(getClass.getName())

  def logExecutionTime[T](block: => T)(message: T => String): T = {
    val before = System.currentTimeMillis()
    val result = block
    logger.info(message(result) + " (lasted " + (System.currentTimeMillis() - before) + " ms)")
    result
  }

  def log(message: String) = logger.info(message)

}
