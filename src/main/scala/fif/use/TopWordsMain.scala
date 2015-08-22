package fif.use

import java.io.File

import fif._
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.spark.{ SparkConf, SparkContext }

import scala.io.Source
import scala.language.higherKinds
import scala.reflect.ClassTag

object TopWordsMain extends App with Serializable {

  val dir = new File("./src/test/resources/")

  val documents: Traversable[TopWords.Document] =
    dir
      .listFiles().toSeq
      .filter(_.getName.contains("article"))
      .zipWithIndex
      .map {
        case (fi, index) =>
          val text: Traversable[String] =
            Source.fromFile(fi)
              .getLines()
              .flatMap(_.split(" "))
              .toIndexedSeq

          (index.toLong, text)
      }

  val top = 30

  println(s"[Local] TFIDF top $top words")
  println("==================")
  implicit val x = TravData
  TopWords(documents, top, tfidf = true)
    .foreach(println)
  println("\n\n")

  val sc = new SparkContext(new SparkConf().setMaster("local[2]").setAppName("demo"))
  try {
    val documentsRdd = sc.parallelize(documents.toSeq)

    println(s"[RDD] TFIDF top $top words")
    println("==================")
    implicit val r = RddData
    TopWords(documentsRdd, top, tfidf = true)
      .foreach(println)
    println("\n\n")

  } finally {
    sc.stop()
  }

  //  implicit val xxx: TypeInformation[(fif.use.TopWords.Id, fif.use.TopWords.Text)] =
  //    FlinkHelper.typeInfo(ClassTag(classOf[(fif.use.TopWords.Id, fif.use.TopWords.Text)]))
  //
  //  val documentsFlink =
  //    ExecutionEnvironment.createLocalEnvironment(2).fromCollection(documents.toArray)
  //
  //  println("[Flink] TFIDF top 25 words")
  //  println("==================")
  //  implicit val r = FlinkData
  //  TopWords(documentsFlink, 25, tfidf = true)
  //    .foreach(println)

}