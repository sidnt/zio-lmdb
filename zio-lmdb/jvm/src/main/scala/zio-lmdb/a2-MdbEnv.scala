package zio.lmdb

import fsHelpers._
import zio._
import zio.lmdb.MdbEnvConfig._
import org.lmdbjava.Env

object MdbEnv {
  type MdbEnv = Has[Service]

  trait Service {
    val mdbEnvHandle: ???
  }

}

// wipland below //
object MdbEnv1 {
  type MdbEnv = Has[Service]

  trait Service {
    /** an mdbEnv:MdbEnv will make an mdbEnvHandle available
      * to any downstream 'service' which might want to depend on it
      * An mdbEnvLayer<:ZLayer can wrap an mdbEnv:MdbEnv, and be
      * injected vertically, by >>>, into any such downstream service
      * .
      * 
      * not sure if the RT here should be a managed #doubt
      * 
      */
    // #doubt not sure if the RT here should be a managed
    // because, in best practice, the e:Env[BB] should be managed
    // val mdbEnvHandle2: ZIO[MdbEnvConfig, Throwable, Env[BB]]
    val mdbEnvHandle: Task[Env[BB]]
  }

  val acq:ZIO[MdbEnvConfig, Throwable, MdbEnv] = for {
    ec <- ZIO.access[MdbEnvConfig](_.get)
    edpf <- createOrGetDirPathFile(ec.envDirPath)
    meh = Env.open(edpf,ec.envMaxMiBs)
    me = new Service { val mdbEnvHandle: Task[Env[zio.lmdb.BB]] = ??? }
  } yield ()
  val m:ZManaged[MdbEnvConfig, Throwable, MdbEnv] =
    ZManaged.make()
  //we want an implementation of service that will provide a managed envhandle
  val mMdbEnvHandleLayer:ZLayer[MdbEnvConfig, Throwable, MdbEnv] = 
    ???

  /* looks like we don't need this accessor as 
   * we'll never use this handle directly in the client logic
   * the env handle needs to be present in the environment 
   * and fed to the default mdb service, which will use the handle 
   * present in its environment to provide its services,
   * accessors make sense for default mdb service */
  // val mdbEnvHandle = ZIO.accessM[MdbEnv](_.get.mdbEnvHandle)
  
  // val mdbEnvHandle2 = ZIO.accessM[MdbEnv](_.get.mdbEnvHandle2).asService
  // val mdbEnvHandle3 = ZIO.accessM[MdbEnv](_.get.mdbEnvHandle2)
  ZLayer.fromService
  val acquireMdbEnvHandle = for {
    envDirPath      <- envDirPath
    envMaxMiBs      <- envMaxMiBs
    envDirPathFile  <- createOrGetDirPathFile(envDirPath)
    env = Env.open(envDirPathFile, envMaxMiBs)
  } yield env

  // val acquireMdbEnvHandle2 = for {
  //   env <- acquireMdbEnvHandle
  //   envs <- new MdbEnv.Service { val mdbEnvHandle }
  // } yield env


  // val mdbEnvHandleLayer: ZLayer[MdbEnvConfig, Throwable, MdbEnv] =
  //   ZLayer.fromFunctionManaged[MdbEnvConfig,MdbEnv.Service]( hmecs => {
  //     val mecs = hmecs.get
      
  //   })

  val mdbEnvHandleMLayer: ZLayer[MdbEnvConfig, Throwable, MdbEnv] =
    ZLayer.fromFunctionManaged[MdbEnvConfig, Throwable, MdbEnv.Service]( hmecs => {
      val mecs = hmecs.get
      ZManaged.make(acquireMdbEnvHandle)(releaseMdbEnvHandle4)
    })
  




  val mdbEnvHandleMLayer2: ZLayer[MdbEnvConfig, Throwable, MdbEnv] =
    ZLayer.fromAcquireRelease()








  val mdbEnvHandleMLayer2: ZLayer[MdbEnvConfig, Throwable, MdbEnv] =
    ZLayer.fromFunctionManaged()

  val acquireMdbEnvHandle2 = acquireMdbEnvHandle.asService
  def releaseMdbEnvHandle(mdbEnvHandle: Env[BB]) = UIO(mdbEnvHandle.close())
  def releaseMdbEnvHandle2(mdbEnvService: MdbEnv.Service) =
    UIO(mdbEnvService.mdbEnvHandle.flatMap(e => e.close()).unit)
  
  def releaseMdbEnvHandle3(mdbEnvService: MdbEnv.Service) =
    mdbEnvService.mdbEnvHandle.flatMap(e => UIO(e.close())).unit
  
  def releaseMdbEnvHandle4(mdbEnvService: MdbEnv.Service) = for {
    mdbEnvHandle  <- mdbEnvService.mdbEnvHandle
    _             =  mdbEnvHandle.close()
  } yield ()

  val defaultMdbEnvHandle = ZLayer.fromAcquireRelease(acquireMdbEnvHandle)(releaseMdbEnvHandle)
  // val defaultMdbEnvHandle2 = ZLayer.fromAcquireRelease(acquireMdbEnvHandle.asService)(releaseMdbEnvHandle)

  val managedMdbEnvHandle = ZManaged.make(acquireMdbEnvHandle)(releaseMdbEnvHandle)
  val mel = ZLayer.fromManaged(managedMdbEnvHandle)
}

object MdbEnvO {

  trait Service {
    // val mEnv: ZManaged[MdbEnvConfig, Throwable, Env[ByteBuffer]]
    val mdbEnv: UIO[Env[BB]]
  }
  val accessme = ZIO.accessM[Service](_.mdbEnv)
  val accessme2 = ZIO.accessM[Has[Service]](_.get.mdbEnv)

    
    // val mdbEnv:ZLayer[MdbEnvConfig,Throwable,MdbEnv] =
    //   ZLayer.fromAcquireRelease[MdbEnvConfig,Throwable,MdbEnv]
    //     ()
    //     ( (mes:MdbEnv.Service) => mes.mdbEnv)




  // ZLayer.fromaMan
  def acquireMdbEnv(atPath:String) = for {
    envDirFilePath <- fsHelpers.createOrGetDirPathFile(atPath)
    env = Env.open(envDirFilePath,10)
  } yield env
  def releaseMdbEnv(env:Env[BB]) = UIO(env.close())

  val z1 = for {
    mep <- envDirPath
    mes <- envMaxMiBs
    edp <- fsHelpers.createOrGetDirPathFile(mep)
    env = Env.open(edp,mes)
  } yield env

  val x1 = ZManaged.make(z1)(e => UIO(e.close()))
  val x3 = ZManaged.make(z1)(releaseMdbEnv)
  // val x2 = ZManaged.make(z1)(UIO(_.close()))
  val mel = ZLayer.fromManaged(x1)
  val mel2 = ZLayer.fromManaged(x3)
  // val x2: ZManaged[blocking.Blocking with MdbEnvConfig,Throwable,Env[BB]] = ZManaged.make(z1)(_.close())

  // val x:ZManaged[MdbEnvConfig,Throwable,Env[BB]] =
  //   ZManaged.make(z1)(???)

  // def makeManagedEnv(atPath:String):ZManaged[Nothing,Throwable, Env[BB]] = for {
  //   edfp <- fsHelpers.createOrGetDirPathFile(atPath)
  //   menv = Managed.make(UIO(Env.open(edfp,10)))(e => UIO(e.close()))
  // } yield menv

  // val mEnvLayer = ZLayer.fromManaged(???)
  // val mEnvLayer: ZLayer[MdbEnvConfig, Throwable, MdbEnv] = 
  //   ZLayer.fromManaged[MdbEnvConfig,Throwable,MdbEnv] { (mec:MdbEnvConfig) =>
  //     val envPath = mec.get.envPath
  //     ZManaged.make(acquireMdbEnv(envPath))(e=>releaseMdbEnv(e))
  //   }

  // val mEnvLayer: ZLayer[MdbEnvConfig, Throwable, MdbEnv] = 
  //   ZLayer.fromAcquireRelease( (mec:MdbEnvConfig) => {
  //     val envPath = mec.get.envPath
  //     acquireMdbEnv(envPath)
  //   })(env => releaseMdbEnv(env))
  // val defaultMdbEnv: ZLayer[MdbEnvConfig, Nothing, MdbEnv] = 
  //   ZLayer.fromAcquireRelease(???)

}
