package wikicycles

import java.util.regex.Pattern

import org.apache.commons.io.IOUtils
import org.scalatest.FunSuite

/**
 * Created by mg on 07.12.14.
 */
class FirstLinkExtractorTest extends FunSuite {

  test("Split into sections") {
    val sections = FirstLinkExtractor.splitIntoSections(wikiText("Scala"))
    assert(sections.length === 30)
  }

  test("Remove info boxes") {
    assert(FirstLinkExtractor.removeInfoBoxes("test{{dfhdsfhd\nshfdsiufh}}\nsecond") === "test\nsecond")
  }

  test("Remove special links") {
    assert(FirstLinkExtractor.removeSpecialLinks("Hey! [[Datei:Geany_0.14_de.png|mini|hochkant=1.3|Screenshot der [[Integrierte Entwicklungsumgebung|IDE]] [[Geany]] mit [[Python (Programmiersprache)|Python]]-Quelltext]] [[Informatik]]")
      === "Hey!  [[Informatik]]")
  }

  test("Remove parentheses") {
    assert(FirstLinkExtractor.removeParentheses("Hey (yes) good") === "Hey  good")
  }

  test("Extract first link") {
    assert(FirstLinkExtractor.extractFirstLink("Hey [[Hallo#Super|Jo]] dishfisd [[Second Link]]") === Some("Hallo"))
    assert(FirstLinkExtractor.extractFirstLink("ihfdsj [[Yes]] idshfids [[Second Link|A Link]]") === Some("Yes"))
    assert(FirstLinkExtractor.extractFirstLink("[[Yes|Good]] idshfids") === Some("Yes"))
    assert(FirstLinkExtractor.extractFirstLink("[[Yes#Good]]") === Some("Yes"))
  }

  test("Extract first link from article") {
    check("Scala", Some("Funktionale Programmiersprache"))
    check("Quelltext", Some("Informatik"))
    check("Source Code", Some("science fiction film"))
    check("Schäl Sick", Some("Rheinland"))
    check("Biologie_fr", Some("science"))
  }

  private def check(file: String, expectedResult: Option[String]): Unit = {
    assert(FirstLinkExtractor.extractFirstLinkFromArticle(wikiText(file)) === expectedResult, "First link of article " + file + " should be " + expectedResult)
  }

  private def wikiText(name: String): String = {
    val stream = getClass.getResourceAsStream("/" + name + ".txt")
    IOUtils.toString(stream, "UTF-8")
  }

}
