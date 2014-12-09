package wikicycles

import java.io.{FileWriter, FileOutputStream, FileInputStream, InputStream}
import java.util.concurrent.atomic.{AtomicInteger, AtomicBoolean}
import java.util.concurrent.{TimeUnit, ConcurrentHashMap, Executors}
import scala.concurrent._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal
import java.io.File

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

    val map = convertFile(stream, limit)
    writeResultToFile(sourceFile, map, limit)
  }

  def convertFile(stream: InputStream, maxArticles: Option[Int]): java.util.Map[String, String] = {
    val threadPool = Executors.newFixedThreadPool(numThreads)
    implicit val executionContext = ExecutionContext.fromExecutorService(threadPool)

    val before = System.currentTimeMillis()
    val result = new ConcurrentHashMap[String, String]()
    val processed = new AtomicInteger(0)

    val parser = new WikiXmlParser(stream, maxArticles)

    try {
      val futures = (0 until numThreads).map { i =>
        Future[Unit] {
          val collector = new ResultCollector(result, i, processed, before, true)
          fetchArticlesAndConvert(parser, collector)
        }
      }

      Await.result(Future.sequence(futures), Duration.Inf)

      println("Processing of " + processed.get() + " articles finished in " + (System.currentTimeMillis() - before) +
        " ms. Result contains " + result.size() + " pages with first links.")

      result
    } finally {
      println("Parser shutdown")
      parser.shutdown()
      println("ExecutionContext shutdown")
      executionContext.shutdown()
      executionContext.awaitTermination(1, TimeUnit.SECONDS)
      println("ExecutionContext terminated: " + executionContext.isTerminated + ", shutdown: " + executionContext.isShutdown())
    }
  }

  def writeResultToFile(sourceFile: File, result: java.util.Map[String, String], limit: Option[Int]): Unit = {

    val fileName = sourceFile.getName.substring(0, sourceFile.getName.lastIndexOf(".")) +
      limit.fold("")(l => "-" + l) + "-firstlinks.csv"

    println("Writing result to file " + fileName + "...")

    val out = new FileWriter(new File(sourceFile.getParent, fileName))
    try {
      val it = result.entrySet().iterator()
      while (it.hasNext) {
        val entry = it.next()
        out.append(entry.getKey + "|" + entry.getValue + "\n")
      }
    } finally {
      out.flush()
      out.close()
      println("Result file written completely.")
    }

  }

  private def fetchArticlesAndConvert(parser: WikiXmlParser,
                                      resultCollector: ResultCollector)(implicit executionContext: ExecutionContext): Unit = {
    var article = parser.nextArticle()

    while (article.isDefined) {
      resultCollector.articleRead(article.get)

      try {
        resultCollector.addResult(article.get.title,
          FirstLinkExtractor.extractFirstLinkFromArticle(article.get.wikiSource))
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

  case class ResultCollector(result: ConcurrentHashMap[String, String],
                             threadNumber: Int, processed: AtomicInteger,
                             processStarted: Long, writeDebugLog: Boolean) {

    val out = if (writeDebugLog) {
      Some(new FileWriter("/tmp/thread-" + threadNumber + ".log"))
    } else None

    def articleRead(article: WikiArticle): Unit = {
      for (o <- out) {
        o.append(article.title + "\n")
        o.flush()
      }
    }

    def addResult(title: String, optFirstLink: Option[String]): Unit = {
      for (firstLink <- optFirstLink) {
        result.put(title, firstLink)
      }
      if (optFirstLink.isEmpty) {
        result.put(title, "[Kein Link]")
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


  }

}
