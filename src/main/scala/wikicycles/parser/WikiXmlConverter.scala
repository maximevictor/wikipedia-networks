package wikicycles.parser

import java.io.{File, FileInputStream, FileWriter, InputStream}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Executors, TimeUnit}

import wikicycles.model.{PageLinks, PageInfo}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, _}
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

/**
 * Created by mg on 07.12.14.
 */
object WikiXmlConverter {

  val numThreads = Runtime.getRuntime.availableProcessors()

  def main(args: Array[String]): Unit = {
    val sourceFile = new File(args(0))
    val stream = new FileInputStream(sourceFile)
    val limit = if (args.length > 1) {
      Some(args(1).toInt)
    } else None

    val map = convertFile(stream, sourceFile.getName.endsWith(".bz2"), limit)
    writeResultToFile(sourceFile, map, limit)
  }

  def convertFile(stream: InputStream, compressed: Boolean, maxArticles: Option[Int]): Seq[PageInfo] = {
    val threadPool = Executors.newFixedThreadPool(numThreads)
    implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)

    val before = System.currentTimeMillis()
    val processed = new AtomicInteger(0)
    val secondLinks = new AtomicInteger(0)

    val parser = new WikiXmlParser(stream, compressed, maxArticles)

    try {
      val futures = (0 until numThreads).map { i =>
        Future[List[PageInfo]] {
          val collector = new ResultCollector(i, processed, before, secondLinks, false)
          fetchArticlesAndConvert(parser, collector)
          collector.getResult()
        }
      }

      val result = Await.result(Future.sequence(futures), Duration.Inf).flatten

      println("Processing of " + processed.get() + " articles finished in " + (System.currentTimeMillis() - before) +
        " ms. Result contains " + result.size + " pages with first links and " + secondLinks.get() + " pages with second links.")

      result
    } finally {
      //println("Parser shutdown")
      parser.shutdown()
      //println("ExecutionContext shutdown")
      executionContext.shutdown()
      executionContext.awaitTermination(1, TimeUnit.SECONDS)
      //println("ExecutionContext terminated: " + executionContext.isTerminated + ", shutdown: " + executionContext.isShutdown())
    }
  }

  def writeResultToFile(sourceFile: File, result: Seq[PageInfo], limit: Option[Int]): Unit = {

    val fileName = sourceFile.getName.substring(0, sourceFile.getName.indexOf(".")) +
      limit.fold("")(l => "-" + l) + "-pagelinks.csv"

    println("Writing result to file " + fileName + "...")
    PageInfo.writeToFile(result, new File(sourceFile.getParent, fileName))
    println("Result file written completely.")

  }

  private def fetchArticlesAndConvert(parser: WikiXmlParser,
                                      resultCollector: ResultCollector)(implicit executionContext: ExecutionContext): Unit = {
    var article = parser.nextArticle()

    while (article.isDefined) {
      resultCollector.articleRead(article.get)

      try {
        resultCollector.addResult(article.get.title,
          FirstLinkExtractor.extractFirstLinksFromArticle(article.get.wikiSource))
      } catch {
        case e: StackOverflowError =>
          e.printStackTrace()
          println("StackOverflow at article: " + article.get.title + " with source: " + article.get.wikiSource)
        case NonFatal(t) =>
          t.printStackTrace()
      }

      article = parser.nextArticle()

    }

    resultCollector.noMoreArticles()
  }

  case class ResultCollector(threadNumber: Int, processed: AtomicInteger,
                             processStarted: Long, secondLinks: AtomicInteger, writeDebugLog: Boolean) {

    private val result = ListBuffer[PageInfo]()

    val out = if (writeDebugLog) {
      Some(new FileWriter("/tmp/thread-" + threadNumber + ".log"))
    } else None

    def articleRead(article: WikiArticle): Unit = {
      for (o <- out) {
        o.append(article.title + "\n")
        o.flush()
      }
    }

    def addResult(title: String, optPageLinks: Option[(PageLinks, Boolean)]): Unit = {
      for (links <- optPageLinks) {
        result += PageInfo(PageInfo.normalizePageName(title), links._2, links._1)

        if (links._1.secondLink.isDefined) {
          secondLinks.incrementAndGet()
        }
      }

      val currentProcessed = processed.incrementAndGet()
      if (currentProcessed % 1000 == 0) {
        println("Processed " + currentProcessed + " articles in " + (System.currentTimeMillis() - processStarted) + " ms.")
      }
    }

    def noMoreArticles(): Unit = {
      for (o <- out) {
        o.append("-- No more articles --")
        //println("Thread " + threadNumber + " finished.")

        o.close()
      }
    }

    def getResult() = result.toList


  }

}
