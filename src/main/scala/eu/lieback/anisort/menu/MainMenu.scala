package eu.lieback.anisort.menu

import eu.lieback.anisort.logic.AnisortConcurrentLogic
import eu.lieback.anisort.fileutils.FolderHandler

//TODO: better name handling for user feedback (not lowercase)
//TODO: better menu (with print(\cr))
//TODO: some titles kill the regexing of scala?
//TODO: better menu shutdown


class MainMenu
{
  private val GREETING =  "*****************************************************************\n" +
                          "***************  ANISORT tec-2 v0.1 loaded  *********************\n" +
                          "************************************************ by paeh ********\n"

  private var sourceFolder = "/media/haruka/anime/_einsortieren"//home/paeh/workspace/own_prj/anisort/testing/in"
  //private var sourceFolder = "/home/paeh/workspace/own_prj/anisort/testing/in"
  private var destinationFolder = "/media/haruka/anime/sorting"//home/paeh/workspace/own_prj/anisort/testing/out"
  //private var destinationFolder = "/home/paeh/workspace/own_prj/anisort/testing/out"

  private var logic: AnisortConcurrentLogic = null

  def mainMenu: Boolean =
  {
    println(GREETING)

    //TODO dest/source folder regex pattern matching
    //val destPattern = "dest [a-zA-Z/]*".r
    //val srcPattern = "source [a-zA-Z/]*".r
    var menuLoop = true

    var command = ""
    while(menuLoop)
    {
      print("> ")
      command = readLine()

      command match
      {
        case "quit" => menuLoop = false
        case "help" => println("Before you start, destination and source directories must be set.\n" +
                               "dest - sets the destination folder\n" +
                               "src - sets the source folder\n" +
                               "start - starts the sorting progress")
        case "start" =>
        {
          handleStartCommand
          menuLoop = false
          handleFeedbackResults()
        }
        case _ =>
        {
          handleComplexCommands(command)
        }
      }
    }

    println("* Sorting done. Please wait for shutdown...")
    //TODO: better solution
    logic.shutdown
    Thread.sleep(2500)
    println("Shutdown done. Press any key to exit!")
    readLine()

    true
  }

  private def handleComplexCommands(command: String)
  {
    if (command.startsWith("dest "))
    {
      destinationFolder = command.replaceFirst("dest ", "")
      println("* Destination folder set!")
    }
    else if (command.startsWith("src "))
    {
      sourceFolder = command.replaceFirst("src ", "")
      println("* Source folder set!")
    }
    else
    {
      println("* Command unknown: " + command)
    }
  }

  private def handleStartCommand {
    if (sourceFolder != "" && destinationFolder != "")
    {
      println("* Sorting started!\n!!! DO NOT EXIT THE PROGRAM WITHOUT PERMITION, OTHERWISE DATA LOSS IS POSSIBLE !!!")
      logic = new AnisortConcurrentLogic(sourceFolder, destinationFolder)
      logic.startSorting
    }
    else
    {
      println("* Please specify source and destination folder!")
    }
  }

  private def handleFeedbackResults()
  {
    //logic.isWorkAvailable || logic.isWorkInProgress
    while(true)
    {
      val data = logic.getWork

      if (data != null)
      {
        var optionCounter = 1

        println("* Please select a item [1]:")
        println("Original title: %s\n".format(FolderHandler.getLastFolderNameOfPath(data.animeSourcePath)))
        for(item <- data.metas)
        {
          println("{%d} ".format(optionCounter) + item.name)
          optionCounter += 1
        }
        print("> ")

        val choice: Int = secureReadIntFromConsole
        logic.loadGenreAndMove(data, (choice-1))

        println()
      }
    }
  }


  private def secureReadIntFromConsole: Int =
  {
    var choice = 1

    try
    {
      choice = Console.readInt()
    }
    catch
    {
      case e: Exception => choice = 1
    }

    choice
  }
}
