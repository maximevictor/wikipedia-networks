package wikicycles.analysis

import java.io.File

import wikicycles.model.PageInfo
import wikicycles.util.LoggingUtil

/**
 * Created by mg on 11.12.14.
 */
abstract class AnalysisBase extends LoggingUtil {

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
