package wikicycles.analysis

import java.io.{File, FileWriter}
import wikicycles.model.{PageInfo, PageLinks}
import wikicycles.util.LoggingUtil

import scala.collection.mutable.ListBuffer

/**
 * Created by mg on 10.12.14.
 */
object CycleFinder extends AnalysisBase {

  val minIncomingLinks = 10

  override def process(pages: Map[String, PageInfo], resultFileWithExtension: String => File): File = {
    log("Calculating cycles...")
    val cycles = logExecutionTime {
      calculateCycles(pages)
    }(c => "Found " + c.size + " cycles with more than " + minIncomingLinks + " incoming links.")

    val resultFile = resultFileWithExtension("cycles")
    log("Writing result to file " + resultFile.getName() + "...")
    writeResultToFile(resultFile, pages, cycles)

    resultFile
  }

  class CycleMember(val page: PageInfo, var incomingLinks: Int)
  class Cycle(val members: List[CycleMember], var incomingLinks: Int)

  private def calculateCycles(map: Map[String, PageInfo]): Seq[Cycle] = {
    val cycles = ListBuffer[Cycle]()
    val cyclesByNode = collection.mutable.Map[String, Cycle]()

    for (entry <- map) {
      for ((page, cycle, oldCycle) <- findCycle(entry._2.links, Nil, map, cyclesByNode)) {
        if (oldCycle) {
          cycle.incomingLinks += 1
          for (member <- cycle.members.find(_.page eq page)) {
            member.incomingLinks += 1
          }
        } else {
          cycles += cycle
          for (member <- cycle.members) {
            cyclesByNode.put(member.page.pageName, cycle)
          }
        }
      }
    }

    cycles.filter(_.incomingLinks >= minIncomingLinks).sortBy(_.incomingLinks * -1)
  }

  private def findCycle(link: PageLinks, path: List[PageInfo], map: Map[String, PageInfo], cyclesBefore: collection.mutable.Map[String, Cycle]): Option[(PageInfo, Cycle, Boolean)] = {
    map.get(link.firstLink).flatMap { page =>
      cyclesBefore.get(page.pageName) match {
        case Some(cycle) =>
          // This node was already found as part of a cycle before
          Some((page, cycle, true))
        case None =>
          if (path.contains(page)) {
            val members = new CycleMember(page, 1) :: path.takeWhile(_ ne page).map(pi => new CycleMember(pi, 0))
            Some((page, new Cycle(members, 1), false))
          } else {
            findCycle(page.links, page :: path, map, cyclesBefore)
          }
      }
    }
  }

  private def writeResultToFile(file: File, map: Map[String, PageInfo], cycles: Seq[Cycle]): Unit = {
    val out = new FileWriter(file)
    try {
      for (cycle <- cycles) {
        out.append(cycle.incomingLinks.toString)
        for (elem <- cycle.members) {
          out.append("|" + elem.page.pageName + "|" + elem.incomingLinks)
        }
        out.append("\n")
      }
    } finally {
      out.flush()
      out.close()
    }
  }
}
