package zio.lmdb

trait Err

object Err {
  final case class GetFailed(reason:String) extends Err
  final case class PutFailed(reason:String) extends Err
  final case class DbiOpeningFailed(reason:String) extends Err
  final case class EnvOpeningFailed(reason:String) extends Err
  final case class FSError(reason:String) extends Err
}
