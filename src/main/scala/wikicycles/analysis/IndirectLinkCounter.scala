package wikicycles.analysis

import java.io.{FileWriter, File}

import wikicycles.model.{PageLinks, PageInfo}

/**
 * Created by mg on 11.12.14.
 */
object IndirectLinkCounter extends AnalysisBase {

  val minIndirectLinksCount = 100

  override def process(pages: Map[String, PageInfo], resultFileWithExtension: (String) => File): File = {

    log("Counting indirect links...")
    val result = logExecutionTime {
      countIndirectLinks(pages)
    }(r => "Found " + r.size + " pages with more than " + minIndirectLinksCount + " indirect links.")

    val resultFile = resultFileWithExtension("indirect-links")
    writeResultToFile(resultFile, result)

    resultFile
  }

  class IndirectLinks(var count: Int, var depthSum: Int) {
    def averageDepth: Double = depthSum.toDouble / count
  }

  private def countIndirectLinks(pages: Map[String, PageInfo]): List[(String, IndirectLinks)] = {
    val result = collection.mutable.Map[String, IndirectLinks]()

    for ((pageName, page) <- pages) {
      followLinksAndCount(page.links, Set.empty, pages, { (name, depth) =>
        result.get(name) match {
          case Some(count) =>
            count.count += 1
            count.depthSum += depth
          case None =>
            result.put(name, new IndirectLinks(1, depth))
        }
      })
    }

    result.toList.filter(entry => entry._2.count >= minIndirectLinksCount).sortBy(_._2.count * -1)
  }

  private def followLinksAndCount(link: PageLinks, visitedPages: Set[String], pages: Map[String, PageInfo], incrementCount: (String, Int) => Unit) {
    for (page <- pages.get(link.firstLink)) {
      if (!visitedPages.contains(page.pageName)) {
        incrementCount(page.pageName, visitedPages.size)
        followLinksAndCount(page.links, visitedPages + page.pageName, pages, incrementCount)
      }
    }
  }

  private def writeResultToFile(file: File, result: List[(String, IndirectLinks)]): Unit = {
    val out = new FileWriter(file)
    try {
      for ((pageName, links) <- result) {
        out.append(pageName + "|" + links.count + "|" + links.averageDepth + "\n")
      }
    } finally {
      out.flush()
      out.close()
    }
  }

}
