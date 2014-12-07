package wikicycles

import java.util.regex.Pattern

/**
 * Created by mg on 07.12.14.
 */
object FirstLinkExtractor {

  def extractFirstLinkFromArticle(articleSource: String): Option[String] = {
    val sections = splitIntoSections(articleSource)

    for (section <- sections) {
      val text = removeParentheses(
        removeSpecialLinks(
        removeInfoBoxes(section)))
      for (link <- extractFirstLink(text)) {
        return Some(link)
      }
    }

    None
  }

  val linkPattern = Pattern.compile("(?m)(?s)\\[\\[([^|#\\]]*).*?\\]\\]")
  private[wikicycles] def extractFirstLink(source: String): Option[String] = {
    val matcher = linkPattern.matcher(source)
    if (matcher.find()) {
      Some(matcher.group(1))
    } else {
      None
    }
  }

  val sectionPattern = Pattern.compile("(?m)^==.*$")
  private[wikicycles] def splitIntoSections(source: String): Array[String] = {
    sectionPattern.split(source)
  }

  val infoBoxPattern = Pattern.compile("(?m)(?s)\\{\\{(.*?)\\}\\}") // ?m for multiline mode, ?s for * matches even linebreaks
  private[wikicycles] def removeInfoBoxes(source: String): String = {
    infoBoxPattern.matcher(source).replaceAll("")
  }

  val specialLinksPattern = Pattern.compile("(?m)(?s)(\\[\\[[^|\\]]*?:(.*?(\\[\\[.*?\\]\\])*)*?\\]\\])") // There can also be links inside of special links...
  private[wikicycles] def removeSpecialLinks(source: String): String = {
    specialLinksPattern.matcher(source).replaceAll("")
  }

  val parenthesesPattern = Pattern.compile("(?m)(?s)\\(.*?\\)")
  private[wikicycles] def removeParentheses(source: String): String = {
    parenthesesPattern.matcher(source).replaceAll("")
  }

}
