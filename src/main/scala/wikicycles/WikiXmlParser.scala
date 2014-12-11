package wikicycles

import java.io.InputStream
import javax.xml.stream.XMLInputFactory

import scala.util.control.NonFatal
import javax.xml.stream.{XMLStreamConstants => C}

/**
 * Created by mg on 07.12.14.
 */
class WikiXmlParser(val file: InputStream, val maxArticles: Option[Int]) {

  val factory = XMLInputFactory.newInstance()
  val reader = factory.createXMLStreamReader(file)

  private var articlesRead = 0

  def nextArticle(): Option[WikiArticle] = {
    synchronized {

      if (maxArticles.exists(max => articlesRead >= max)) {
        return None
      }

      var inPage = false
      var inTitle = false
      var inText = false
      var title = new StringBuilder()
      val wikiSource = new StringBuilder()

      try {

        while (reader.hasNext) {
          reader.next() match {
            case C.START_ELEMENT =>
              reader.getLocalName match {
                case "page" => inPage = true
                case "title" => if (inPage) inTitle = true
                case "text" => if (inPage) inText = true
                case _ => // ignore
              }
            case C.END_ELEMENT =>
              reader.getLocalName match {
                case "page" =>
                  inPage = false
                  title.clear()
                  wikiSource.clear()
                case "title" =>
                  if (inPage)
                    inTitle = false
                case "text" =>
                  if (inPage) {
                    inText = false
                    if (!title.isEmpty && isValidArticle(title.toString)) {
                      articlesRead += 1
                      return Some(WikiArticle(title.toString, wikiSource.toString()))
                    }
                  }
                case _ => // ignore
              }
            case C.ENTITY_REFERENCE =>
              if (inText) {
                wikiSource.append(reader.getText)
              } else if (inTitle) {
                title.append(reader.getText)
              }
            case C.CHARACTERS =>
              if (inText) {
                wikiSource.append(reader.getText())
              } else if (inTitle) {
                title.append(reader.getText())
              }
            case _ => // ignore
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
      reader.close()
      file.close()
    }
  }

  private def isValidArticle(title: String): Boolean = {
    !title.contains(":") && title != "12. Oberste Volksversammlung"
  }

}
