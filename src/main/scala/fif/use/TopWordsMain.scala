package fif.use

import java.io.File

import algebra.Semigroup
import fif.Data

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.language.higherKinds
import scala.reflect.ClassTag

object TopWordsMain extends App {

  val dir = new File("./src/test/resources/")

  val documents: Seq[TopWords.Document] =
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


}