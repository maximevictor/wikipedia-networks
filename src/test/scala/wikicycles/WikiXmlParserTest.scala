package wikicycles

import org.apache.commons.io.IOUtils
import org.scalatest.FunSuite

import scala.collection.mutable.ListBuffer

/**
 * Created by mg on 07.12.14.
 */
class WikiXmlParserTest extends FunSuite {

  test("Parse test XML") {
    val parser = new WikiXmlParser(getClass().getResourceAsStream("/dewiki_10000.xml"))

    val articles = ListBuffer[WikiArticle]()

    var article = parser.nextArticle()
    while (!article.isEmpty) {
      articles += article.get

      article = parser.nextArticle()
    }

    assert(articles.size === 36)
    assert(articles(0).title === "Alan Smithee")
    assert(articles(35).title === "Vorsätze für Maßeinheiten")

    assert(articles(0).wikiSource === IOUtils.toString(getClass().getResourceAsStream("/Alan Smithee.txt"), "UTF-8"))
  }

}
