package wikicycles.parser

import java.util

import info.bliki.wiki.model.WikiModel

/**
 * Created by mg on 08.12.14.
 */
class WikiModelWithoutTemplates extends WikiModel("/", "/") {
  override def substituteTemplateCall(templateName: String, parameterMap: util.Map[String, String], writer: Appendable): Unit = {
    // Do nothing, ignore templates!
  }
}
