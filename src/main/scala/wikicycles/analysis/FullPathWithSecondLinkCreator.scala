package wikicycles.analysis

import wikicycles.model.{PageLinks, PageInfo, PageInfoMap}

import scala.collection.mutable.ListBuffer

/**
 * Created by mg on 13.01.15.
 */
object FullPathWithSecondLinkCreator extends BaseFullPathCreator {

  override val fileExtension = "fullpaths-with-second-links"

  override def calculatePaths(pages: PageInfoMap): Seq[Seq[PageInfo]] = {
    val result = ListBuffer[Seq[PageInfo]]()

    for (page <- pages.map.values) {
      if (!page.redirectPage) {
        result.append(findPath(page.links, List(page), false, pages))
      }
    }

    result
  }

  private def findPath(link: PageLinks, path: List[PageInfo], firstCycleFound: Boolean, pages: PageInfoMap): Seq[PageInfo] = {
    pages.getFirstOrSecondLink(link) match {
      case Some(page) =>
        val newPath = if (page.redirectPage) path else page :: path

        if (path.contains(page)) {
          val secondLink = pages.getSecondLinkIfDifferentFromFirstLink(page, link)
          if (firstCycleFound || secondLink.isEmpty) {
            (page :: path).reverse
          } else {
            // This is the first cycle. Try to find a second cycle using the second link here:
            findPath(secondLink.get.links, newPath, true, pages)
          }
        } else {
          findPath(page.links, newPath, firstCycleFound, pages)
        }
      case None =>
        path.reverse
    }
  }



}
