package wikicycles

import java.io.InputStream

import scala.io.Source
import scala.util.control.NonFatal
import scala.xml.pull._

/**
 * Created by mg on 07.12.14.
 */
class WikiXmlParser(val file: InputStream, val maxArticles: Option[Int]) {

  private val reader = new XMLEventReader(Source.fromInputStream(file, "UTF-8"))

  private var articlesRead = 0

  def nextArticle(): Option[WikiArticle] = {
    synchronized {

      if (maxArticles.exists(max => articlesRead >= max)) {
        return None
      }

      var inPage = false
      var inTitle = false
      var inText = false
      var title: Option[String] = None
      val wikiSource = new StringBuilder()

      try {

        while (reader.hasNext) {
          reader.next() match {
            case EvElemStart(_, "page", _, _) =>
              inPage = true
            case EvElemEnd(_, "page") =>
              inPage = false
              title = None
              wikiSource.clear()
            case EvElemStart(_, "title", _, _) =>
              if (inPage)
                inTitle = true
            case EvElemEnd(_, "title") =>
              if (inPage)
                inTitle = false
            case EvElemStart(_, "text", _, _) =>
              if (inPage)
                inText = true
            case EvElemEnd(_, "text") =>
              if (inPage) {
                inText = false
                if (!title.isEmpty && isValidArticle(title.get)) {
                  articlesRead += 1
                  return Some(WikiArticle(title.get, wikiSource.toString()))
                }
              }
            case EvEntityRef(entity) =>
              if (inText) {
                entity match {
                  case "gt" => wikiSource.append(">")
                  case "lt" => wikiSource.append("<")
                  case "amp" => wikiSource.append("&")
                  case "quot" => wikiSource.append("\"")
                  case _ => throw new IllegalArgumentException("Found unknown entity: " + entity)
                }
              }

            case EvText(text) =>
              if (inText) {
                wikiSource.append(text)
              } else if (inTitle) {
                title = Some(text)
              }

            case _ => // Nothing to do
          }
        }

      } catch {
        case NonFatal(e) =>
          e.printStackTrace()
      }

      None
    }
  }

  def shutdown(): Unit = {
    synchronized {
      //reader.stop()
      file.close()
    }
  }

  private def isValidArticle(title: String): Boolean = {
    !title.contains(":") && title != "12. Oberste Volksversammlung"
  }

}
