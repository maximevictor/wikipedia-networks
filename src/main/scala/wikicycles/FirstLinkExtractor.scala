package wikicycles

import java.util
import java.util.regex.Pattern

import info.bliki.wiki.model.WikiModel

/**
 * Created by mg on 07.12.14.
 */
object FirstLinkExtractor {

  def extractFirstLinkFromArticle(articleSource: String): Option[String] = {
    val sections = splitIntoSections(articleSource)
    val model = new WikiModelWithoutTemplates()

    for ((section, index) <- sections.zipWithIndex) {
      var parsed = model.render(new WikiToTextWithLinksConverter(), section)
      if (parsed.isEmpty()) {
        // The parser does not return redirects - use the unparsed version instead
        parsed = section
      }
      for (link <- extractFirstLink(parsed)) {
        return Some(link)
      }
    }

    None
  }

  val linkPattern = Pattern.compile("(?m)(?s)\\[\\[([^|#\\]]*).*?\\]\\]")
  private[wikicycles] def extractFirstLink(source: String): Option[String] = {
    val matcher = linkPattern.matcher(source)
    while (matcher.find()) {
      if (!isWithinParentheses(source, matcher.start(), matcher.end())) {
        return Some(matcher.group(1))
      }
    }

    None
  }

  private def isWithinParentheses(source: String, start: Int, end: Int): Boolean = {
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
