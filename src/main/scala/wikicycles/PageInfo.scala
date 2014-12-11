package wikicycles

import java.io.{BufferedReader, FileReader, FileWriter, File}
import java.util.Locale
import java.util.regex.Pattern

import org.apache.commons.lang3.StringUtils

/**
 * Created by mg on 10.12.14.
 */
case class PageInfo(pageName: String, links: PageLinks) {

}

object PageInfo {

  private val replaceWhitespacePattern = Pattern.compile("\\s+")

  def normalizePageName(pageName: String): String = {
    val lower = StringUtils.capitalize(pageName)
    replaceWhitespacePattern.matcher(lower).replaceAll(" ")
  }

  def writeToFile(list: Seq[PageInfo], file: File): Unit = {
    val out = new FileWriter(file)
    try {
      for (info <- list) {
        out.append(info.pageName + "|" + info.links.firstLink)
        for (second <- info.links.secondLink) {
          out.append("|" + second)
        }
        out.append("\n")
      }
    } finally {
      out.flush()
      out.close()
    }
  }

  def loadFromFile(file: File): Map[String, PageInfo] = {
    val in = new BufferedReader(new FileReader(file))
    val result = collection.mutable.Map[String, PageInfo]()

    var line = in.readLine()
    while (line ne null) {
      val elems = line.split('|')
      if (elems.length >= 2) {
        val pageName = elems(0)
        val firstLink = elems(1)
        val secondLink = if (elems.length >= 3) Some(elems(2)) else None
        val pageInfo = PageInfo(pageName, PageLinks(firstLink, secondLink))
        result.put(pageName, pageInfo)
      }

      line = in.readLine()
    }

    result.toMap
  }


}
