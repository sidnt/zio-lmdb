package zio.lmdb
// import MdbEnv._

import zio._
import org.lmdbjava.Env
// import org.lmdbjava.Txn
// import org.lmdbjava.Env

object AdhocTxns {

  // val mRoTxn2 = for {
  //   mdbEnvHandle <- mdbEnvHandle //pull out whatever handle is there in the environment
  //   mRoTxn = ZManaged.make(UIO(mdbEnvHandle.txnRead()))(txn => UIO(txn.commit()))
  // } yield mRoTxn
  
  val mRwTxn = (env:Env[BB]) => ZManaged.make(UIO(env.txnWrite()))(txn => UIO(txn.commit()))
  val mRoTxn = (env:Env[BB]) => ZManaged.make(UIO(env.txnRead()))(txn => UIO(txn.commit()))

  // def mRoTxn2(inEnv:Env[BB]) = ZIO.bracket(UIO(inEnv.txnRead()))(rotxn => UIO(rotxn.commit()))
  
  // type AdhocTxns = Has[Service]

  // trait Service {
  //   val roTxn: Task[Txn[BB]]
  //   val rwTxn: Task[Txn[BB]]
  // }

  // val mAdhocTxnL: ZLayer[MdbEnv, Throwable, AdhocTxns] =
  //   ZLayer.fromService[MdbEnv.Service, AdhocTxns.Service]( mes => 
  //     new AdhocTxns.Service {
  //       def roTxn: Task[Txn[zio.lmdb.BB]] =
  //         for {
  //           meh <- mes.mdbEnvHandle

  //         }
  //       def rwTxn: Task[Txn[zio.lmdb.BB]] = ???
  //     })
  
  // val t1 = Managed.make(UIO(env.txnRead()))(roTxn => UIO{println("roTxn.commit()");roTxn.commit()})
  // val t2 = Managed.make(UIO(env.txnWrite()))(rwTxn => UIO{println("rwTxn.commit()");rwTxn.commit()})

}
