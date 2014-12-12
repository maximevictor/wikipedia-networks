package wikicycles.parser

import org.apache.commons.io.IOUtils
import org.scalatest.FunSuite

import scala.collection.mutable.ListBuffer

/**
 * Created by mg on 07.12.14.
 */
class WikiXmlParserTest extends FunSuite {

  private def testFile = getClass().getResourceAsStream("/dewiki_10000.xml")

  test("Parse test XML") {
    val parser = new WikiXmlParser(testFile, false, None)

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

  test("Parse XML and extract first links") {
    val result = WikiXmlConverter.convertFile(testFile, false, None)

    //println(result.asScala)

    assert(result.size === 36)
  }


}
