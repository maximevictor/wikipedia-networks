package wikicycles

import java.io.StringReader
import java.util
import java.util.List
import java.util.regex.Pattern

import info.bliki.htmlcleaner.{Utils, ContentToken, TagNode}
import info.bliki.wiki.filter._
import info.bliki.wiki.model.{Configuration, IWikiModel, ImageFormat, WikiModel}
import info.bliki.wiki.tags.{WPATag, ATag, HTMLTag}
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

    assert(FirstLinkExtractor.removeSpecialLinks("[[Datei:US President Barack Obama taking his Oath of Office - 2009Jan20.jpg|miniatur|[[Barack Obama]] bei der Ablegung des Eides zum " +
      "Amtsantritt als 44. US-Präsident am 20. Januar 2009<!-- noch als [[Senator (Vereinigte Staaten)|Senator-->]]") === "")

    val text = "Er verpflichtet zur [[Wahrheit]] (z.&nbsp;B. in [[Gericht]]sverfahren) und zum Tragen der " +
      "Konsequenzen (z.&nbsp;B. beim [[Fahneneid]]) der Eidaussage. Der Eid wird oft als bedingte Selbstverfluchung " +
      "bezeichnet, da bei einem Eid mit religiöser Beteuerung eine Gottheit als [[Eideshelfer]] und als Rächer der " +
      "Unwahrheit angerufen wird. Eide gibt es nicht nur in der europäischen Rechtstradition (z.&nbsp;B. " +
      "bei den [[Griechen]], [[Römisches Reich|Römern]] und [[Kelten]]), sondern auch in [[Kaiserreich China|China]], im alten " +
      "Israel und bei zahlreichen ethnologisch untersuchten [[Indigene Völker|indigenen Völkern]]. Der altgriechische [[Eid des Hippokrates]] " +
      "verpflichtete [[Arzt|Ärzte]] zur Einhaltung ihrer Berufspflichten und ethischer Prinzipien (u.&nbsp;a. die Kranken vor " +
      "Schaden bewahren, die Pflicht zur Verschwiegenheit beachten)."

    assert(FirstLinkExtractor.removeSpecialLinks(text) === text)

  }


  test("Extract first link") {
    assert(FirstLinkExtractor.extractFirstLink("Hey [[Hallo#Super|Jo]] dishfisd [[Second Link]]") === Some("Hallo"))
    assert(FirstLinkExtractor.extractFirstLink("ihfdsj [[Yes]] idshfids [[Second Link|A Link]]") === Some("Yes"))
    assert(FirstLinkExtractor.extractFirstLink("[[Yes|Good]] idshfids") === Some("Yes"))
    assert(FirstLinkExtractor.extractFirstLink("[[Yes#Good]]") === Some("Yes"))
    assert(FirstLinkExtractor.extractFirstLink("[[James Maxwell (Offizier)]]") === Some("James Maxwell (Offizier)"))
  }

  test("Extract first link from article") {
    check("Jazz", Some("Jazz"))
    check("Schäl Sick", Some("Rheinland"))
    check("www", Some("World Wide Web"))
    check("Kanton Graubuenden", Some("Kanton (Schweiz)"))
    check("Scala", Some("Funktionale Programmiersprache"))
    check("Quelltext", Some("Informatik"))
    check("Source Code", Some("science fiction film"))
    check("Biologie_fr", Some("science"))
    check("Ricarda Roggan", Some("Hochschule für Grafik und Buchkunst Leipzig"))
    check("Eid", Some("Wahrheit"))
    check("Sparparadoxon", Some("Konkurrenzparadoxon"))
    check("Mister-Lady", None)
    check("Linards Zemelis", Some("Biathlon"))
    check("James Maxwell", Some("James Maxwell (Offizier)"))
    check("Carl Barks", Some("Vereinigte Staaten"))
    //check("12. Oberste Volksversammlung", Some("Nordkorea")) // --> does not work, badly nested tags
  }

  private def check(file: String, expectedResult: Option[String]): Unit = {
    assert(FirstLinkExtractor.extractFirstLinkFromArticle(wikiText(file)) === expectedResult, "First link of article " + file + " should be " + expectedResult)
  }

  private def wikiText(name: String): String = {
    val stream = getClass.getResourceAsStream("/" + name + ".txt")
    IOUtils.toString(stream, "UTF-8")
  }

}
