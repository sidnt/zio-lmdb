package zio.lmdb
import MdbEnv._

import zio._

object DefaultMdb {
  type DefaultMdb = Has[Service]

  /** meet the DefaultMdb.Service
    * at present, it can only try getting a key from the DefaultMdb
    * getting a key from the db is an effect as it can fail, eg if value wasn't found
    * .
    * but it will require an mdbEnvHandle to execute these effects
    * how do we declare/make get(key) depend-on/access an env handle?
    * 1 we can make get take a value parameter env:Env[BB]
    * 2 we can declare the RT to depend on an MdbEnv.Service
    *   so the RT becomes: ZIO[MdbEnv, Throwable, BB]
    * 3 or we can construct 1 ZLayer[MdbEnv, Throwable, DefaultMdb] implementation
    *   that uses the mdbEnvHandle from the provided MdbEnv layer instance
    *   in the implementation of DefaultMdb
    */
  trait Service {
    def get(key:BB): Task[BB]
  }

  val defaultMdbL: ZLayer[MdbEnv, Throwable, DefaultMdb] = ???

}
