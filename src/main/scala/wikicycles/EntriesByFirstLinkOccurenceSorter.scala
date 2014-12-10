package wikicycles

import java.io.{FileWriter, FileInputStream, File}

/**
 * Created by mg on 10.12.14.
 */
object EntriesByFirstLinkOccurenceSorter {

  def main(args: Array[String]): Unit = {
    val file = new File(args(0))

    val before = System.currentTimeMillis()
    println("Reading file " + file.getName + "...")

    val map = PageInfo.loadFromFile(file)

    println("File read in " + (System.currentTimeMillis() - before) + " ms with " + map.size + " entries.")

    println("Calculating first link counts...")
    val firstLinkCounts = getFirstLinkCountForPages(map)

    println("Sorting results...")
    val sortedCounts = firstLinkCounts.toList.sortBy(item => item._2 * -1)

    println("Writing result to file...")
    val fileName = file.getName.substring(0, file.getName.lastIndexOf(".")) + "-sorted.csv"

    println("Writing result to file " + fileName + "...")
    writeResultToFile(new File(file.getParent, fileName), map, sortedCounts)

    println("Result completely written. Lasted " + (System.currentTimeMillis() - before) + " ms overall.")

  }

  private def getFirstLinkCountForPages(map: Map[String, PageInfo]): Map[String, Integer] = {
    val result = collection.mutable.Map[String, Integer]()

    for (entry <- map) {
      val page = entry._2
      val firstLink = PageInfo.normalizePageName(page.links.firstLink)
      val newValue = result.get(firstLink).getOrElse(0.asInstanceOf[Integer]) + 1
      result.put(firstLink, newValue)
    }

    result.toMap
  }

  private def writeResultToFile(file: File, map: Map[String, PageInfo], counts: List[(String, Integer)]): Unit = {
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
