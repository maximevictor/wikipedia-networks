package wikicycles.parser

import org.apache.commons.io.IOUtils
import org.scalatest.FunSuite
import wikicycles.model.PageLinks

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
    assert(FirstLinkExtractor.extractFirstLinks("Hey [[Hallo#Super|Jo]] dishfisd [[Second Link]]") === Some(PageLinks("Hallo", "Second Link")))
    assert(FirstLinkExtractor.extractFirstLinks("ihfdsj [[Yes]] idshfids [[Second Link|A Link]]") === Some(PageLinks("Yes", "Second Link")))
    assert(FirstLinkExtractor.extractFirstLinks("[[Yes|Good]] idshfids") === Some(PageLinks("Yes")))
    assert(FirstLinkExtractor.extractFirstLinks("[[Yes#Good]]") === Some(PageLinks("Yes")))
    assert(FirstLinkExtractor.extractFirstLinks("[[James Maxwell (Offizier)]]") === Some(PageLinks("James Maxwell (Offizier)")))
  }

  test("Extract first link from article") {
    check("IKV", Some(PageLinks("Institut für Kunststoffverarbeitung", "Internationale Kartographische Vereinigung")))
    check("Darmstadt-Dieburg", Some(PageLinks("Landkreis Darmstadt-Dieburg")))
    check("Science", Some(PageLinks("Vrai", "Méthode scientifique")))
    check("Phenylpropene", Some(PageLinks("Phénylpropanoïde", "Eugénol")))
    check("Lebensstil", Some(PageLinks("Umgangssprache", "Lebensführung")))
    check("Gamet", Some(PageLinks("Haploidie", "Zelle (Biologie)")))
    check("Philologie", Some(PageLinks("Sprachwissenschaft", "Literaturwissenschaft")))
    check("Jazz", Some(PageLinks("Jazz")))
    check("Schäl Sick", Some(PageLinks("Rheinland", "Rhein")))
    check("www", Some(PageLinks("World Wide Web", "Third-Level-Domain")))
    check("Kanton Graubuenden", Some(PageLinks("Kanton (Schweiz)", "Schweiz")))
    check("Scala", Some(PageLinks("Funktionale Programmiersprache", "Objektorientierte Programmiersprache")))
    check("Quelltext", Some(PageLinks("Informatik", "Menschenlesbar")))
    check("Source Code", Some(PageLinks("Science fiction film", "Duncan Jones")))
    check("Biologie_fr", Some(PageLinks("Science", "Vie")))
    check("Ricarda Roggan", Some(PageLinks("Hochschule für Grafik und Buchkunst Leipzig", "Timm Rautert")))
    check("Eid", Some(PageLinks("Wahrheit", "Gericht")))
    check("Sparparadoxon", Some(PageLinks("Konkurrenzparadoxon", "John Maynard Keynes")))
    check("Mister-Lady", None)
    check("Linards Zemelis", Some(PageLinks("Biathlon", "Olympische Jugend-Winterspiele 2012")))
    check("James Maxwell", Some(PageLinks("James Maxwell (Offizier)", "James Maxwell (Schauspieler)")))
    check("Carl Barks", Some(PageLinks("Vereinigte Staaten", "Comic")))
    //check("12. Oberste Volksversammlung", Some("Nordkorea")) // --> does not work, badly nested tags
  }

  private def check(file: String, expectedResult: Option[PageLinks]): Unit = {
    val extracted = FirstLinkExtractor.extractFirstLinksFromArticle(wikiText(file))
    assert(extracted.map(_._1) === expectedResult, "First links of article " + file + " should be " + expectedResult)
  }

  private def wikiText(name: String): String = {
    val stream = getClass.getResourceAsStream("/" + name + ".txt")
    IOUtils.toString(stream, "UTF-8")
  }

}