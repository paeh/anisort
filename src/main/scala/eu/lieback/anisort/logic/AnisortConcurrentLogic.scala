package eu.lieback.anisort.logic

import eu.lieback.anisort.fileutils.FolderHandler
import java.util.concurrent.{TimeUnit, ThreadPoolExecutor, LinkedBlockingQueue, Executors}
import eu.lieback.anisort.anisearch.{AnimeMetaInformation, AniSearchParser, AnimeMetaInformationWrapper}
import org.slf4j.LoggerFactory

class AnisortConcurrentLogic(searchRoot: String, destination: String)
{
  private val log = LoggerFactory.getLogger(this.getClass)
  private val feedbackQueue= new LinkedBlockingQueue[AnimeMetaInformationWrapper]()
  //private val threadPool = Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
  private val threadPool = Executors.newFixedThreadPool(2)
  private var isQueueEmpty = false

  def startSorting
  {
    log.debug("Sorting started...")

    val dirs = FolderHandler.getAllLeafFoldersFromPath(searchRoot)

    for(dir <- dirs)
    {
      log.debug("Job submitted to the threadpool: " + dir)

      threadPool.submit(SortingWorkItem(dir))
    }
  }

  private case class SortingWorkItem(animeSourceDirectory: String) extends Runnable
  {
    def run()
    {
      log.debug("Work begun: %s".format(animeSourceDirectory))

      val metas = AniSearchParser.searchForMetaInformationByAnimeTitle(FolderHandler.getLastFolderNameOfPath(animeSourceDirectory))


      if (metas.size > 0)
      {
        // if there is only one result bypass user feedback
        if(metas.size == 1)
        {
          log.debug("There was only one meta result, we use it for the following")
          loadGenreAndMove(AnimeMetaInformationWrapper(List(metas.head), animeSourceDirectory))
        }
        else if (!checkIfThereAreCompletedMetasAndHandleThem(metas, animeSourceDirectory))
        {
          // feedback needed, put meta into feedback queue
          log.debug("Put meta into feedback queue")
          putWork(AnimeMetaInformationWrapper(metas, animeSourceDirectory))
        }
      }
    }
  }

  private def checkIfThereAreCompletedMetasAndHandleThem(metas: List[AnimeMetaInformation], animeSourceDirectory: String): Boolean =
  {
    // search for completed items and move the related folder directly
    val completedMetas = metas.filter(animeMetaInformation => animeMetaInformation.complete)

    if (completedMetas.size > 0)
    {
      log.debug("Meta already completed we use it for moving")
      move(animeSourceDirectory, completedMetas.head)
      true
    }
    else
    {
      false
    }
  }

  private def putWork(work: AnimeMetaInformationWrapper)
  {
    feedbackQueue.put(work)
    updateQueueStatus
  }

  //TODO threaded
  def loadGenreAndMove(data: AnimeMetaInformationWrapper, choice: Int = 0)
  {
    log.debug("Loading genre and moving folder: " + data.animeSourcePath)

    var completedMeta = AniSearchParser.getGenreForAnimeMetaInformation(data.metas(choice))

    if(!completedMeta.complete)
    {
      log.debug("No genre was found, we use the error genre")
      completedMeta = AnimeMetaInformation(completedMeta.name, completedMeta.forwardId, "error", completedMeta.rawData)
    }

    move(data.animeSourcePath, completedMeta)
  }

  def getWork: AnimeMetaInformationWrapper =
  {
    val work = feedbackQueue.poll(1000, TimeUnit.MILLISECONDS)
    updateQueueStatus
    work
  }

  def updateQueueStatus
  {
    isQueueEmpty = feedbackQueue.isEmpty
  }

  def isWorkAvailable: Boolean =
  {
    !isQueueEmpty
  }

  def isWorkInProgress: Boolean =
  {
    threadPool.asInstanceOf[ThreadPoolExecutor].getQueue.size() > 0
  }

  def shutdown
  {
    if (threadPool != null)
    {
      threadPool.shutdown()
    }
  }

  def move(source: String, meta: AnimeMetaInformation)
  {
    val targetDir = destination + "/" + meta.genre + "/" + FolderHandler.getLastFolderNameOfPath(source)
    val sourceDir = source

    log.debug("Move: " + meta.name + " from " + sourceDir + " to " + targetDir)

    FolderHandler.moveFolder(sourceDir, targetDir)
  }
}
