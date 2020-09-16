package zio.lmdb
import MdbEnv._
// import AdhocTxns._

import zio._
import org.lmdbjava.Dbi
import org.lmdbjava.DbiFlags.MDB_CREATE

object DefaultDbi {
  
  trait Service {
    // https://discordapp.com/channels/629491597070827530/630498701860929559/755386897240686654
    val defaultDbiHandle: Task[Dbi[BB]]
    //looks like it should be Task[Ref[Dbi[BB]]] because Dbis need to be opened only once then shared
  }

  val defaultDbiL: ZLayer[MdbEnv, Throwable, DefaultDbi] =
    ZLayer.fromService[MdbEnv.Service, DefaultDbi.Service]( mes =>
      new DefaultDbi.Service {
        val defaultDbiHandle: Task[Dbi[zio.lmdb.BB]] = for {
          mdbEnvHandle      <- mes.mdbEnvHandle
          defaultDbiHandle  <- Task(mdbEnvHandle.openDbi(null:String,MDB_CREATE))
        } yield defaultDbiHandle
      }
    )

/*
  def fx(mes:MdbEnv.Service):DefaultDbi.Service =
    new DefaultDbi.Service {
      val defaultDbi: Task[Dbi[zio.lmdb.BB]] = for {
        mdbEnvHandle      <- mes.mdbEnvHandle
        defaultDbiHandle  <- Task(mdbEnvHandle.openDbi("",MDB_CREATE))
      } yield defaultDbiHandle
    }

  val defaultDbiL2: ZLayer[MdbEnv, Throwable, DefaultDbi] =
    ZLayer.fromService[MdbEnv.Service, DefaultDbi.Service](fx)
*/    

}
