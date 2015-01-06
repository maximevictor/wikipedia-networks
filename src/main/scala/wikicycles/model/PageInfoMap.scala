package wikicycles.model

/**
 * Created by mg on 06.01.15.
 */
case class PageInfoMap(map: Map[String, PageInfo]) {

  def get(pageName: String): Option[PageInfo] = map.get(pageName)
  def get(optPageName: Option[String]): Option[PageInfo] = optPageName.flatMap(p => get(p))

  /**
   * Returns the page for the first link, or the page for the second link,
   * if the first link does not exist.
   *
   * @param pageLinks
   * @return
   */
  def getFirstOrSecondLink(pageLinks: PageLinks): Option[PageInfo] = {
    get(pageLinks.firstLink).orElse(pageLinks.secondLink.flatMap(l => get(l)))
  }

}
