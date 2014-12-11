package wikicycles.analysis

import java.io.{File, FileWriter}
import wikicycles.model.PageInfo
import wikicycles.util.LoggingUtil

/**
 * Created by mg on 10.12.14.
 */
object EntriesByFirstLinkOccurrenceSorter extends AnalysisBase {

  def main(args: Array[String]): Unit = {
    val file = new File(args(0))
    sortEntriesByFirstLinkOccurrence(file)
  }

  def sortEntriesByFirstLinkOccurrence(file: File): File = {
    val before = System.currentTimeMillis()

    val map = loadSourceFile(file)

    log("Calculating first link counts...")
    val firstLinkCounts = getFirstLinkCountForPages(map)

    log("Sorting results...")
    val sortedCounts = firstLinkCounts.toList.sortBy(item => item._2 * -1)

    val resultFile = extractResultFile(file, "sorted")
    log("Writing result to file " + resultFile.getName + "...")
    writeResultToFile(resultFile, map, sortedCounts)

    log("Result completely written. Lasted " + (System.currentTimeMillis() - before) + " ms overall.")

    resultFile
  }


  private def getFirstLinkCountForPages(map: Map[String, PageInfo]): Map[String, Int] = {
    val result = collection.mutable.Map[String, Int]()

    for (entry <- map) {
      val page = entry._2
      val firstLink = page.links.firstLink
      val newValue = result.get(firstLink).getOrElse(0) + 1
      result.put(firstLink, newValue)
    }

    result.toMap
  }

  private def writeResultToFile(file: File, map: Map[String, PageInfo], counts: List[(String, Int)]): Unit = {
    val out = new FileWriter(file)
    try {
      for (item <- counts) {
        for (info <- map.get(item._1)) {
          out.append(info.pageName + "|" + info.links.firstLink + "|" + item._2)
          for (second <- info.links.secondLink) {
            out.append("|" + second)
          }
          out.append("\n")
        }
      }
    } finally {
      out.flush()
      out.close()
    }
  }

}
