package fif.use

import fif.{ Data, DataOps }

import scala.language.higherKinds
import scala.reflect.ClassTag

object Sum extends Serializable {

  import DataOps.syntax._

  def apply[N: Numeric: ClassTag, D[_]: Data](data: D[N]): N = {
    val add = implicitly[Numeric[N]].plus _
    data.aggregate(implicitly[Numeric[N]].zero)(add, add)
  }

}