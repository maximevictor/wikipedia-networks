package wikicycles

import java.io.{BufferedReader, FileReader, FileWriter, File}
import java.util.Locale

/**
 * Created by mg on 10.12.14.
 */
case class PageInfo(pageName: String, links: PageLinks) {

  lazy val normalizedPageName = PageInfo.normalizePageName(pageName)

}

object PageInfo {

  def normalizePageName(pageName: String): String = {
    val lower = pageName.toLowerCase(Locale.ENGLISH)
    lower.replaceAll("\\s*", " ")
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
        result.put(normalizePageName(pageName), pageInfo)
      }

      line = in.readLine()
    }

    result.toMap
  }


}
