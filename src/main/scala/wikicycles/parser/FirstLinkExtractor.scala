package wikicycles.parser

import java.util.regex.Pattern

import org.apache.commons.lang3.StringUtils
import wikicycles.model.{PageLinks, PageInfo}

/**
 * Created by mg on 07.12.14.
 */
object FirstLinkExtractor {

  def extractFirstLinksFromArticle(articleSource: String, articleName: String): Option[(PageLinks, Boolean)] = {
    val sections = splitIntoSections(articleSource)
    val model = new WikiModelWithoutTemplates()

    var found: Option[PageLinks] = None
    var redirectPage = false

    for ((section, index) <- sections.zipWithIndex) {
      // Replace "[[File:" with "[[Datei:", so that special file handling by parser is suppressed
      val wikiText = section.replaceAllLiterally("[[File:", "[[Datei:").replaceAllLiterally("[[Image:", "[[Datei:")
      if (!StringUtils.isBlank(wikiText)) {
        var parsed = model.render(new WikiToTextWithLinksConverter(), wikiText)
        if (parsed.isEmpty()) {
          // The parser does not return redirects - use the unparsed version instead
          parsed = section
          if (index == 0) {
            redirectPage = true
          }
        }

        found match {
          case None =>
            for (extracted <- extractFirstLinks(parsed, articleName)) {
              if (extracted.secondLink.isDefined) {
                return Some((extracted, redirectPage))
              } else {
                found = Some(extracted)
              }
            }
          case Some(firstLink) =>
            for (second <- extractSecondLink(parsed, articleName)) {
              return Some((firstLink.copy(secondLink = Some(second)), redirectPage))
            }
        }
      }
    }

    found.map(f => (f, redirectPage))
  }

  val linkPattern = Pattern.compile("(?m)(?s)\\[\\[([^|#\\]]*).*?\\]\\]")
  private[wikicycles] def extractFirstLinks(source: String, articleName: String): Option[PageLinks] = {
    val matcher = linkPattern.matcher(source)
    var firstLink: Option[String] = None

    while (matcher.find()) {
      val link = normalize(matcher.group(1))
      if (link != articleName) {
        firstLink match {
          case None =>
            if (!isWithinParentheses(source, matcher.start())) {
              firstLink = Some(link)
            }
          case Some(l) =>
            return Some(PageLinks(l, Some(link)))
        }
      }
    }

    firstLink.map(l => PageLinks(l, None))
  }

  /**
   * Remove newlines from links
   * @param link
   */
  private def normalize(link: String): String = {
    PageInfo.normalizePageName(link)
  }

  private[wikicycles] def extractSecondLink(source: String, articleName: String): Option[String] = {
    val matcher = linkPattern.matcher(source)
    while (matcher.find()) {
      val link = normalize(matcher.group(1))
      if (link != articleName) {
        return Some(link)
      }
    }
    None
  }

  private def isWithinParentheses(source: String, start: Int): Boolean = {
    val before = source.substring(0, start)
    val openParens = before.count(_ == '(')
    val closedParens = before.count(_ == ')')

    openParens > closedParens
  }

  val sectionPattern = Pattern.compile("(?m)^==.*$")
  private[wikicycles] def splitIntoSections(source: String): Array[String] = {
    val result = sectionPattern.split(source)
    //println("Was split into sections")
    result
  }

  val infoBoxPattern = Pattern.compile("(?m)(?s)\\{\\{(.*?)\\}\\}") // ?m for multiline mode, ?s for * matches even linebreaks
  private[wikicycles] def removeInfoBoxes(source: String): String = {
    infoBoxPattern.matcher(source).replaceAll("")
  }

  val removeCommentsPattern = Pattern.compile("(?m)(?s)<\\!--.*?-->")
  private[wikicycles] def removeComments(source: String): String = {
    removeCommentsPattern.matcher(source).replaceAll("")
  }

  val specialLinksPattern = Pattern.compile("(?m)(?s)(\\[\\[[^|\\]]*?:(.*?(\\[\\[.*?\\]\\])?)*?\\]\\])") // There can also be links inside of special links...
  private[wikicycles] def removeSpecialLinks(source: String): String = {
    //println("Removing special links...")// (from: " + source)
    val result = specialLinksPattern.matcher(source).replaceAll("")
    //println("Special links removed.")
    result
  }


}
