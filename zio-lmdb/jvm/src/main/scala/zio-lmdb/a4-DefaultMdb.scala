package zio.lmdb
// import DefaultDbi._
import MdbEnv._
// import DefaultDbiP._
// import MdbEnvP._

import zio._
import org.lmdbjava.Txn
import org.lmdbjava.Dbi

object DefaultMdb {
  type DefaultMdb =   Has[Service]
  /** meet the DefaultMdb.Service
    * at present, it can only try getting a key from the DefaultMdb
    * getting a key from the db is an effect as it can fail, eg if value wasn't found
    * .
    * but it will require an mdbDbiHandle to execute these effects
    * how do we declare/make get(key) depend-on/access a dbi handle?
    * 1 we can make get take a value parameter dbi:Dbi[BB]
    * 2 we can declare the RT to depend on an DefaultDbi.Service
    *   so it becomes: def get(key): ZIO[DefaultDbi, Throwable, BB]
    * 3 or we can construct 1 ZLayer[DefaultDbi, Throwable, DefaultMdb] implementation
    *   that uses the mdbDbiHandle from the provided DefaultDbi layer instance
    *   in the implementation of DefaultMdb
    */
  trait Service {
    def put(key:BB, value:BB):Task[Boolean]
    def get(key:BB): Task[BB]
  }

  val defaultMdb = ZIO.access[DefaultMdb](identity)

  val defaultMdbLEffectful: ZLayer[DefaultDbi with MdbEnv, Throwable, DefaultMdb] =
    ZLayer.fromFunction[DefaultDbi with MdbEnv, DefaultMdb.Service]( ddswmes => {
      val dds = ddswmes.get[DefaultDbi.Service]
      val mes = ddswmes.get[MdbEnv.Service]
      new DefaultMdb.Service {

        def putP(key:BB,value:BB,txn:Txn[BB],dbi:Dbi[BB]) = UIO(dbi.put(txn,key,value))
        // def usage(tx:ZManaged[Any,Nothing,Txn[BB]], key:BB,value:BB,dbi:Dbi[BB]) = tx.use(txu =>putP(key,value,txu,dbi))
        def put(key: zio.lmdb.BB, value: zio.lmdb.BB): Task[Boolean] = for {
          dbi     <- dds.defaultDbiHandle
          meh     <- mes.mdbEnvHandle
          mRwTxn  = AdhocTxns.mRwTxn(meh)
          b       <- mRwTxn.use(txn => putP(key,value,txn,dbi))
          // b <- be
        } yield b

        def get(key: zio.lmdb.BB): Task[zio.lmdb.BB] = for {
          dbi     <- dds.defaultDbiHandle
          meh     <- mes.mdbEnvHandle
          mRoTxn  = AdhocTxns.mRoTxn(meh)
          vbb     <- mRoTxn.use(txn => UIO(dbi.get(txn,key)))
        } yield vbb
      }
    })

  val defaultMdbLPure: ZLayer[MdbEnvP with DefaultDbiP, Throwable, DefaultMdb] =
    ZLayer.fromFunction[MdbEnvP with DefaultDbiP, DefaultMdb.Service] (meps_ddps => {
      val meh = meps_ddps.get[MdbEnvP.Service].mdbEnvHandle
      val dbi = meps_ddps.get[DefaultDbiP.Service].defaultDbiHandle
      new DefaultMdb.Service {
        def put(key: zio.lmdb.BB, value: zio.lmdb.BB): Task[Boolean] = for {
          b <- AdhocTxns.mRwTxn(meh).use(txn => UIO(dbi.put(txn,key,value)))
        } yield b
        
        def get(key: zio.lmdb.BB): Task[zio.lmdb.BB] = for {
          vbb <- AdhocTxns.mRoTxn(meh).use(txn=>UIO(dbi.get(txn,key)))
        } yield vbb
        
      }
    })

}
