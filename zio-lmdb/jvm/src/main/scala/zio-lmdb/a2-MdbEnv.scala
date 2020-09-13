package zio.lmdb

import zio._
import org.lmdbjava.Env

object MdbEnv {
  type MdbEnv = Has[Service]

  trait Service {
    val mdbEnvHandle: ???
  }

}
