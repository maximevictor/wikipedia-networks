package wikicycles.analysis

import java.io.File

import wikicycles.model.PageInfo
import wikicycles.util.LoggingUtil

/**
 * Created by mg on 11.12.14.
 */
abstract class AnalysisBase extends LoggingUtil {

  def main(args: Array[String]): Unit = {
    val file = new File(args(0))
    loadAndProcess(file)
  }

  def loadAndProcess(file: File): File = {
    logExecutionTime {
      val map = loadSourceFile(file)
      process(map, extension => extractResultFile(file, extension))
    }(f => "Processing finished.")
  }

  /**
   * Processes the source file and return a file to which the result were written.
   *
   * @param pages page info loaded from source file
   * @param resultFileWithExtension creates the result file from the given extension
   * @return the result file
   */
  def process(pages: Map[String, PageInfo], resultFileWithExtension: String => File): File

  protected def loadSourceFile(file: File): Map[String, PageInfo] = {
    log("Reading file " + file.getName + "...")
    logExecutionTime {
      PageInfo.loadFromFile(file)
    }(m => "File with " + m.size + " entries read.")
  }

  protected def extractResultFile(sourceFile: File, extension: String): File = {
    val fileName = sourceFile.getName.substring(0,
      sourceFile.getName.lastIndexOf(".")) + "-" + extension + ".csv"
    new File(sourceFile.getParent, fileName)
  }

}
