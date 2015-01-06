package wikicycles.analysis

import java.io.{FileWriter, File}

import wikicycles.model.{PageLinks, PageInfo, PageInfoMap}

import scala.collection.mutable.ListBuffer

/**
 * Created by mg on 06.01.15.
 */
object FullPathCreator extends AnalysisBase {

  /**
   * Processes the source file and return a file to which the result were written.
   *
   * @param pages page info loaded from source file
   * @param resultFileWithExtension creates the result file from the given extension
   * @return the result file
   */
  override def process(pages: PageInfoMap, resultFileWithExtension: (String) => File): File = {
    log("Calculating paths...")
    val paths = logExecutionTime {
      calculatePaths(pages)
    }(c => "Found " + c.size + " paths (not including redirect pages)")

    val resultFile = resultFileWithExtension("fullpaths")
    log("Writing result to file " + resultFile.getName() + "...")
    writeResultToFile(resultFile, pages, paths)

    resultFile
  }

  def calculatePaths(pages: PageInfoMap): Seq[Seq[PageInfo]] = {
    val result = ListBuffer[Seq[PageInfo]]()

    for (page <- pages.map.values) {
      if (!page.redirectPage) {
        result.append(findPath(page.links, List(page), pages))
      }
    }

    result
  }

  private def findPath(link: PageLinks, path: List[PageInfo], pages: PageInfoMap): Seq[PageInfo] = {
    pages.getFirstOrSecondLink(link) match {
      case Some(page) =>
        if (path.contains(page)) {
          (page :: path).reverse
        } else {
          val newPath = if (page.redirectPage) path else page :: path
          findPath(page.links, newPath, pages)
        }
      case None =>
        path.reverse
    }
  }

  private def writeResultToFile(file: File, map: PageInfoMap, paths: Seq[Seq[PageInfo]]): Unit = {
    val out = new FileWriter(file)
    try {
      for (path <- paths) {
        out.append(path.map(_.pageName).mkString("|"))
        out.append("\n")
      }
    } finally {
      out.flush()
      out.close()
    }
  }

}
