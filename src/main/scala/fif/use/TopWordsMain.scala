package fif.use

import java.io.File

import algebra.Semigroup
import fif.{ TravData, Data }

import scala.io.Source
import scala.language.higherKinds

object TopWordsMain extends App {

  val dir = new File("./src/test/resources/")

  val documents: Traversable[TopWords.Document] =
    dir
      .listFiles().toSeq
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

  implicit val x = TravData

  println("wordcount top 25 words")
  TopWords(documents, 25, tfidf = false)
    .foreach(println)
  println("")

  println("TFIDF top 25 words")
  TopWords(documents, 25, tfidf = true)
    .foreach(println)

}