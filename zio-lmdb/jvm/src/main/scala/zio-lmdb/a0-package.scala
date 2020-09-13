package zio

import zio._
import zio.blocking._
import zio.nio.file._
import java.nio.ByteBuffer
import java.io.File

package object lmdb {
  
    type BB = ByteBuffer
    
    object fsHelpers {

        def createOrGetDirPathFile(dirPath: String): ZIO[Blocking, Throwable, File] = for {
            dirPath          <- effectBlocking(FileSystem.default.getPath(dirPath))
            dirPathExists    <- Files.exists(dirPath)
            _                <- if(!dirPathExists) Files.createDirectory(dirPath) else IO.unit
            dirPathFile      =  dirPath.toFile
        } yield dirPathFile
  
    }
    
}
