package wikicycles

/**
 * Created by mg on 09.12.14.
 */
case class PageLinks(firstLink: String, secondLink: Option[String]) {

  lazy val firstLinkNormalized = PageInfo.normalizePageName(firstLink)
  lazy val secondLinkNormalized = secondLink.map(l => PageInfo.normalizePageName(l))

}

object PageLinks {

  def apply(firstLink: String): PageLinks = PageLinks(firstLink, None)
  def apply(firstLink: String, secondLink: String): PageLinks = PageLinks(firstLink, Some(secondLink))

}
