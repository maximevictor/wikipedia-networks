package wikicycles.analysis

import wikicycles.model.{PageLinks, PageInfo, PageInfoMap}

import scala.collection.mutable.ListBuffer

/**
 * Created by mg on 13.01.15.
 */
object FullPathWithSecondLinkCreator extends BaseFullPathCreator {

  override val fileExtension = "fullpaths-with-second-links"

  override def calculatePaths(pages: PageInfoMap): Seq[Seq[PageInfo]] = {
    log("Calculating fundamental cycle...")

    val fundamentalCycle = logExecutionTime {
      CycleFinder.calculateCycles(pages).head.members.map(_.page).toSet
    }(cycle => "Found fundamental cycle: " + cycle.map(_.pageName).mkString(" -> "))

    val result = ListBuffer[Seq[PageInfo]]()

    for (page <- pages.map.values) {
      if (!page.redirectPage) {
        result.append(findPath(pages.getFirstOrSecondLink(page.links), List(page), Set.empty, pages, fundamentalCycle))
      }
    }

    result
  }

  private def findPath(optPage: Option[PageInfo], path: List[PageInfo], pagesWithCycle: Set[PageInfo], pages: PageInfoMap, fundamentalCycle: Set[PageInfo]): Seq[PageInfo] = {
    optPage match {
      case Some(page) =>
        val newPath = if (page.redirectPage) path else page :: path

        if (path.contains(page)) {
          val secondLink = pages.getSecondLinkIfDifferentFromFirstLink(page.links)
          if (fundamentalCycle.contains(page) || pagesWithCycle.contains(page) || secondLink.isEmpty) {
            (page :: path).reverse
          } else {
            // This is a cycle that can possibly be avoided using the second link. Try it:
            findPath(secondLink, newPath, pagesWithCycle + page, pages, fundamentalCycle)
          }
        } else {
          findPath(pages.getFirstOrSecondLink(page.links), newPath, pagesWithCycle, pages, fundamentalCycle)
        }
      case None =>
        path.reverse
    }
  }



}
