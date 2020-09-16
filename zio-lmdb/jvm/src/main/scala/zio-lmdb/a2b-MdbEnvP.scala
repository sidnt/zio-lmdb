package zio.lmdb
import fsHelpers._
// import MdbEnvConfig._

import zio._
// import blocking._
import org.lmdbjava.Env

object MdbEnvP {
  
  trait Service {
    val mdbEnvHandle: Env[BB]
  }
  val mdbEnvHandle = ZIO.access[MdbEnvP](_.get.mdbEnvHandle)

  def managedMdbEnvPService(from:MdbEnvConfig.Service) = {
    // val from = from0.get
    val acquireMdbEnvPService = for {
      envDirPathFile <- createOrGetDirPathFile(from.envDirPath)
    } yield new MdbEnvP.Service {
        val mdbEnvHandle = Env.open(envDirPathFile,from.envMaxMiBs)
      }

    val releaseMdbEnvPService = (meps: MdbEnvP.Service) => UIO(meps.mdbEnvHandle.close())

    ZManaged.make(acquireMdbEnvPService)(releaseMdbEnvPService)
  }

  // val managedMdbEnvPServiceLayer:ZLayer[zio.blocking.Blocking with MdbEnvConfig, Throwable, MdbEnvP] = 
  //   ZLayer.fromFunctionManaged(managedMdbEnvPService)


  val managedMdbEnvPServiceLayer2 =
    ZLayer.fromServiceManaged(managedMdbEnvPService)

  // val managedMdbEnvPServiceLayer3: ZLayer[Blocking with MdbEnvConfig, Throwable, MdbEnvP] = ???
}
