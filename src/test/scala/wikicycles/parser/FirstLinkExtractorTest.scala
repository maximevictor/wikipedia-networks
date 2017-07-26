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

  test("Extract first link") {
    assert(FirstLinkExtractor.extractFirstLinks("Hey [[Hallo#Super|Jo]] dishfisd [[Second Link]]", "") === Some(PageLinks("Hallo", "Second Link")))
    assert(FirstLinkExtractor.extractFirstLinks("ihfdsj [[Yes]] idshfids [[Second Link|A Link]]", "Yes") === Some(PageLinks("Second Link")))
    assert(FirstLinkExtractor.extractFirstLinks("[[Yes|Good]] idshfids", "No") === Some(PageLinks("Yes")))
    assert(FirstLinkExtractor.extractFirstLinks("[[Yes#Good]]", "No") === Some(PageLinks("Yes")))
    assert(FirstLinkExtractor.extractFirstLinks("[[James Maxwell (Offizier)]]", "") === Some(PageLinks("James Maxwell (Offizier)")))
  }

  test("Extract first link from article") {
    check("Strahlenflosser_cn", Some(PageLinks("鰭", "放射狀")))
    check("Quality", Some(PageLinks("Philosophy", "Statistical process control")))
    check("Planet_en", Some(PageLinks("Astronomical body", "Orbit")))
    check("Biology", Some(PageLinks("Natural science", "Life")))
    check("GerardChouchan", "Gérard Chouchan", Some(PageLinks("IDHEC", "Jean Prat")))
    check("IKV", Some(PageLinks("Institut für Kunststoffverarbeitung", "Internationale Kartographische Vereinigung")))
    check("Darmstadt-Dieburg", "", Some(PageLinks("Landkreis Darmstadt-Dieburg")), Some(true))
    check("Science", Some(PageLinks("Vrai", "Méthode scientifique")))
    check("Phenylpropene", Some(PageLinks("Phénylpropanoïde", "Eugénol")))
    check("Lebensstil", Some(PageLinks("Umgangssprache", "Lebensführung")))
    check("Gamet", Some(PageLinks("Haploidie", "Zelle (Biologie)")))
    check("Philologie", Some(PageLinks("Sprachwissenschaft", "Literaturwissenschaft")))
    check("Jazz", "", Some(PageLinks("Jazz")), Some(true))
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
    check("Mister-Lady", "", None, None)
    check("Linards Zemelis", Some(PageLinks("Biathlon", "Olympische Jugend-Winterspiele 2012")))
    check("James Maxwell", Some(PageLinks("James Maxwell (Offizier)", "James Maxwell (Schauspieler)")))
    check("Carl Barks", Some(PageLinks("Vereinigte Staaten", "Comic")))
    //check("12. Oberste Volksversammlung", Some("Nordkorea")) // --> does not work, badly nested tags
  }

  private def check(file: String, expectedResult: Option[PageLinks]): Unit = {
    check(file, "", expectedResult)
  }

  private def check(file: String, articleName: String, expectedResult: Option[PageLinks], redirectPage: Option[Boolean] = Some(false)): Unit = {
    val extracted = FirstLinkExtractor.extractFirstLinksFromArticle(wikiText(file), articleName)
    assert(extracted.map(_._1) === expectedResult, "First links of article " + file + " should be " + expectedResult)
    assert(extracted.map(_._2) === redirectPage, "Article " + file + " redirect page state is wrong.")
  }

  private def wikiText(name: String): String = {
    val stream = getClass.getResourceAsStream("/" + name + ".txt")
    IOUtils.toString(stream, "UTF-8")
  }

}
