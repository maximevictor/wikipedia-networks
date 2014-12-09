package wikicycles

/**
 * Created by mg on 09.12.14.
 */
case class PageLinks(firstLink: String, secondLink: Option[String]) {

}

object PageLinks {

  def apply(firstLink: String): PageLinks = PageLinks(firstLink, None)
  def apply(firstLink: String, secondLink: String): PageLinks = PageLinks(firstLink, Some(secondLink))

}
