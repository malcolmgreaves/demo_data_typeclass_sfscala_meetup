package fif.use

abstract class TreeLikeContainer[A: Cmp] extends SortableContainer[A] {

  sealed abstract class Structure extends Serializable {
    val size: Int
    def map[B](f: A => B): Option[B]
  }

  case object Empty extends Structure {
    override val size = 0
    override def map[B](f: A => B): Option[B] = None
  }

  case class Full(left: Structure, item: A, right: Structure) extends Structure {
    override val size = left.size + 1 + right.size
    override def map[B](f: A => B): Option[B] = Some(f(item))
  }

  override val empty: Structure = Empty

}