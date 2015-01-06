package wikicycles.parser

import java.util.regex.Pattern

import wikicycles.model.{PageLinks, PageInfo}

/**
 * Created by mg on 07.12.14.
 */
object FirstLinkExtractor {

  def extractFirstLinksFromArticle(articleSource: String): Option[PageLinks] = {
    val sections = splitIntoSections(articleSource)
    val model = new WikiModelWithoutTemplates()

    var found: Option[PageLinks] = None

    for ((section, index) <- sections.zipWithIndex) {
      // Replace "[[File:" with "[[Datei:", so that special file handling by parser is suppressed
      val wikiText = section.replaceAllLiterally("[[File:", "[[Datei:").replaceAllLiterally("[[Image:", "[[Datei:")

      var parsed = model.render(new WikiToTextWithLinksConverter(), wikiText)
      if (parsed.isEmpty()) {
        // The parser does not return redirects - use the unparsed version instead
        parsed = section
      }

      found match {
        case None =>
          for (extracted <- extractFirstLinks(parsed)) {
            if (extracted.secondLink.isDefined) {
              return Some(extracted)
            } else {
              found = Some(extracted)
            }
          }
        case Some(firstLink) =>
          for (second <- extractSecondLink(parsed)) {
            return Some(firstLink.copy(secondLink = Some(second)))
          }
      }
    }

    found
  }

  val linkPattern = Pattern.compile("(?m)(?s)\\[\\[([^|#\\]]*).*?\\]\\]")
  private[wikicycles] def extractFirstLinks(source: String): Option[PageLinks] = {
    val matcher = linkPattern.matcher(source)
    var firstLink: Option[String] = None

    while (matcher.find()) {
      firstLink match {
        case None =>
          if (!isWithinParentheses(source, matcher.start())) {
            firstLink = Some(normalize(matcher.group(1)))
          }
        case Some(link) =>
          return Some(PageLinks(link, Some(normalize(matcher.group(1)))))
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

  private[wikicycles] def extractSecondLink(source: String): Option[String] = {
    val matcher = linkPattern.matcher(source)
    if (matcher.find()) {
      Some(normalize(matcher.group(1)))
    } else {
      None
    }
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
