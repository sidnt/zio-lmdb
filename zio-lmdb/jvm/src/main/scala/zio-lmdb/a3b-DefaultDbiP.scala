package zio.lmdb
// import MdbEnvP._

import org.lmdbjava.Dbi
import org.lmdbjava.DbiFlags.MDB_CREATE
import zio._

object DefaultDbiP {
  trait Service {
    val defaultDbiHandle: Dbi[BB]
  }

  val defaultDbiL: ZLayer[MdbEnvP,Throwable, DefaultDbiP] = 
    ZLayer.fromService[MdbEnvP.Service,DefaultDbiP.Service]( meps => new DefaultDbiP.Service {
      val defaultDbiHandle: Dbi[zio.lmdb.BB] = meps.mdbEnvHandle.openDbi("",MDB_CREATE)
    })
}
