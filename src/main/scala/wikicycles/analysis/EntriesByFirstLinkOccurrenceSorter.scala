package wikicycles.analysis

import java.io.{File, FileWriter}
import wikicycles.model.{PageInfoMap, PageInfo}
import wikicycles.util.LoggingUtil

/**
 * Created by mg on 10.12.14.
 */
object EntriesByFirstLinkOccurrenceSorter extends AnalysisBase {

  def process(pages: PageInfoMap, resultFileWithExtension: String => File): File = {
    log("Calculating first link counts...")
    val firstLinkCounts = getFirstLinkCountForPages(pages)

    log("Sorting results...")
    val sortedCounts = firstLinkCounts.toList.sortBy(item => item._2 * -1)

    val resultFile = resultFileWithExtension("sorted")
    log("Writing result to file " + resultFile.getName + "...")
    writeResultToFile(resultFile, pages, sortedCounts)

    resultFile
  }


  private def getFirstLinkCountForPages(map: PageInfoMap): Map[String, Int] = {
    val result = collection.mutable.Map[String, Int]()

    for (entry <- map.map) {
      val page = entry._2
      val firstLink = page.links.firstLink
      val newValue = result.get(firstLink).orElse(page.links.secondLink.flatMap(l => result.get(l))).getOrElse(0) + 1
      result.put(firstLink, newValue)
    }

    result.toMap
  }

  private def writeResultToFile(file: File, map: PageInfoMap, counts: List[(String, Int)]): Unit = {
    val out = new FileWriter(file)
    try {
      for (item <- counts) {
        for (info <- map.get(item._1)) {
          val firstLinkNonExistent = map.get(info.links.firstLink).isEmpty
          if (!firstLinkNonExistent || map.get(info.links.secondLink).isDefined) {
            val firstLink = if (firstLinkNonExistent) info.links.secondLink.get else info.links.firstLink

            out.append(info.pageName + "|" + firstLink + "|" + item._2)
            if (!firstLinkNonExistent) {
              for (second <- info.links.secondLink) {
                out.append("|" + second)
              }
            }
            out.append("\n")
          }
        }
      }
    } finally {
      out.flush()
      out.close()
    }
  }

}
