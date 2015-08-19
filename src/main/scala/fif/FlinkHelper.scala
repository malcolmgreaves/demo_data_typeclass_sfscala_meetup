package fif

import org.apache.flink.api.common.ExecutionConfig
import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.api.common.typeutils.TypeSerializer
import org.apache.flink.api.java.typeutils.runtime.kryo.KryoSerializer

import scala.language.higherKinds
import scala.reflect.ClassTag

/**
 * Methods, values, and functions that provide some common functionality
 * necessary for interacting with Flink DataSet objects. Most importantly
 * is the typeInfo method that generates a TypeInformation instance from
 * ClassTag evidence.
 */
object FlinkHelper extends Serializable {

  private[absnlp] val productClass: Class[Product] =
    classOf[Product]

  private[absnlp] def countFields(c: Class[_]): Int = {

    val fields = c.getFields
    if (fields.isEmpty)
      1

    else
      fields.foldLeft(0) {
        case (result, field) =>
          result + countFields(field.getClass)
      }
  }

  def mapCombine[B, A](m1: Map[B, Iterator[A]], m2: Map[B, Iterator[A]]): Map[B, Iterator[A]] = {

    val (larger, smaller) =
      if (m1.size > m2.size)
        (m1, m2)
      else
        (m2, m1)

    smaller.foldLeft(larger) {
      case (m, (key, value)) =>
        if (m.contains(key))
          (m - key) + (key -> (m(key) ++ value))
        else
          m + (key -> value)
    }
  }

  private[absnlp] val emptyTypeInfoList: List[TypeInformation[_]] =
    List.empty[TypeInformation[_]]

  private[absnlp] val emptyUnitSeq: Seq[Unit] =
    Seq.empty[Unit]

  val unitTypeInformation: TypeInformation[Unit] =
    typeInfo(ClassTag(classOf[Unit]))

  def typeInfo[A: ClassTag]: TypeInformation[A] = {

    val ct = implicitly[ClassTag[A]]

    new TypeInformation[A] {

      override lazy val isBasicType: Boolean =
        ct.runtimeClass.isPrimitive || ct.equals(ClassTag(classOf[String]))

      override lazy val isTupleType: Boolean =
        productClass.isAssignableFrom(ct.runtimeClass)

      override lazy val getArity: Int =
        ct.runtimeClass.getFields.length

      override lazy val getTotalFields: Int =
        countFields(ct.runtimeClass)

      override lazy val getTypeClass: Class[A] =
        ct.runtimeClass.asInstanceOf[Class[A]]

      override lazy val getGenericParameters: java.util.List[TypeInformation[_]] = {

        import scala.collection.JavaConversions._

        val tVars = ct.getClass.getTypeParameters
        if (tVars.isEmpty)
          emptyTypeInfoList

        else
          tVars
            .map { typeVariable =>
              val genericClass = typeVariable.getGenericDeclaration
              typeInfo(ClassTag(genericClass))
            }
            .toList
      }

      override lazy val isKeyType: Boolean =
        isBasicType

      override lazy val isSortKeyType: Boolean =
        isKeyType

      override def createSerializer(config: ExecutionConfig): TypeSerializer[A] =
        new KryoSerializer[A](getTypeClass, config)
    }
  }

}

