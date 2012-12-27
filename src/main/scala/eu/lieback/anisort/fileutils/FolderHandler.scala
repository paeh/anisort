package eu.lieback.anisort.fileutils

import java.io.{InputStreamReader, BufferedReader, File}
import tools.nsc.io.Directory
import collection.mutable.ListBuffer
import org.slf4j.LoggerFactory

object FolderHandler
{
  private val log = LoggerFactory.getLogger(this.getClass)

  def getAllLeafFoldersFromPath(path: String): List[String] =
  {
    log.debug("Searching for leaf folders. Start is: " + path)

    var rootFolder = new Directory(new File(path))

    val result = getLeafFolders(rootFolder)

    log.debug(result.size + " leaf folder(s) found")

    result
  }

  private def getLeafFolders(directory: Directory): List[String] =
  {
    var result = ListBuffer[String]()

    if (directory.dirs.size > 0) {

      for (dir <- directory.dirs) {

        result ++= getLeafFolders(dir)
      }
    }
    else
    {
      result += directory.path
    }

    result.toList
  }

  def getLastFolderNameOfPath(path: String): String =
  {
    val splittedDirs = path.split("/")
    splittedDirs.takeRight(1).head
  }

  def moveFolder(source: String, destination: String)
  {
    val runtime = Runtime.getRuntime
    val rootDestinationFolder = destination.replace(FolderHandler.getLastFolderNameOfPath(destination), "")

    runtime.exec(List("mkdir", "-p", rootDestinationFolder).toArray)

    val stream = runtime.exec(List("mv", source, rootDestinationFolder).toArray).getErrorStream

    val reader = new BufferedReader(new InputStreamReader(stream))
    var line = reader.readLine()
    while(line != null)
    {
      println(line)
      line = reader.readLine()
    }
  }
}
