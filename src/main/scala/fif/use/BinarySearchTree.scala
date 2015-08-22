package fif.use

abstract class BinarySearchTree[A: Cmp] extends TreeLikeContainer[A] {

  val maxSize: Option[Int]

  def merge(a: Structure, b: Structure): (Structure, Option[Iterable[A]])

  def insert(item: A)(existing: Structure): (Structure, Option[A])

  def sort(existing: Structure): Iterable[A]

  def delete(item: A)(existing: Structure): Option[Structure]

}