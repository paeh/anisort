package eu.lieback.anisort.anisearch

import java.net.URL
import java.io.{BufferedReader, InputStreamReader}
import collection.mutable.ListBuffer
import org.slf4j.LoggerFactory

object AniSearchParser
{
  //TODO better regexes (:ascii:)

  private val log = LoggerFactory.getLogger(this.getClass)
  private val ANISEARCH_BASE_URL = "http://anisearch.de/"
  private val ANISEARCH_SEARCH_URL = ANISEARCH_BASE_URL + "?page=suche&mode=auswahl&qsearch=%s"
  private val ANISEARCH_FORWARD_URL = ANISEARCH_BASE_URL + "index.php?page=anime&id=%s&hentai=yes"
  //private val CATCH_ALL = "[a-zA-Z0-9</> =\"_.\\?\'\\-:]"
  private val CATCH_ALL = "[!a-zA-Z0-9</> =\"_.\\?'\\-:&#;()]"

  private def loadWebPageSource(pageAddress: String):String = {
    log.debug("Load page source from: " + pageAddress)

    var reader:BufferedReader = null
    val content = new StringBuilder()

    try
    {
      val url = new URL(pageAddress)
      reader = new BufferedReader(new InputStreamReader(url.openStream()))

      var line = reader.readLine()
      while(line != null)
      {
        content.append(line + "\n")
        line = reader.readLine()
      }
    }
    finally
    {
      if (reader != null)
      {
        reader.close()
      }
    }

    content.toString
  }

  private def removeUnusedInformationFromAnimeTitle(title: String): String =
  {
    val tagFreeTitle = "[<\\[(][a-zA-Z0-9\\? =.,_!-\\#]*[)\\]>]".r.replaceAllIn(title, "")
    val numberFreeTitle = "[0-9]+[-_][0-9]+".r.replaceAllIn(tagFreeTitle, "")
    val fillerCharFree = "[_~-]".r.replaceAllIn(numberFreeTitle, " ")

    fillerCharFree.trim
  }

  def searchForMetaInformationByAnimeTitle(title: String): List[AnimeMetaInformation] =
  {
    log.debug("Searching meta information from anisearch.de: " + title)

    val goodTitle = removeUnusedInformationFromAnimeTitle(title)
    val content = loadWebPageSource(ANISEARCH_SEARCH_URL.format(goodTitle.replace(" ", "%20")))

    // sometimes we get a direct search hit: let's check for this by try parsing the genre
    val possibleGenre = getGenreForPossibleDirectSearchHit(goodTitle, content)

    if(!possibleGenre.isEmpty)
    {
      // direct hit :)
      log.debug("Direct hit. Bypassing user feedback.")
      List(AnimeMetaInformation(goodTitle, "", possibleGenre, "", true))
    }
    else
    {
      // no direct hit :(
      parseTheSearchSelectionSite(content, goodTitle)
    }
  }

  private def parseTheSearchSelectionSite(content: String, title: String): List[AnimeMetaInformation] =
  {
    val result = new ListBuffer[AnimeMetaInformation]()

    log.debug("Selection site reached, parsing this page now.")

    val relevantLines = getRelevantSourceLinesFromPageSource(content, title)

    if (relevantLines.size == 0)
    {
      log.debug("No relevant source lines found, obviously the parsing has failed. Skipping.")
      println("skip")
    }

    for (line <- relevantLines)
    {
      log.debug("Parsing line for %s".format(title))
      parseRelevantSourceLine(line, title) match
      {
        case Some(parsed) => result += parsed
        case _ => //noting
      }
    }

    result.toList
  }

  private def getGenreForPossibleDirectSearchHit(title: String, content: String): String =
  {
    getGenreforAnimeMetaInformation(AnimeMetaInformation(title, "", "", ""), content).genre
  }

  private def parseRelevantSourceLine(line: String, title: String): Option[AnimeMetaInformation] =
  {
      val forwardId = getForwardId(line)
      val completeTitle = getCompleteTitle(line)

      if (completeTitle == "" || forwardId == "")
      {
        log.debug("Nothing reasonable found for: " + title + " parsed title: " + completeTitle + " parsed forwardId: " + forwardId)

        None
      }
      else
      {
        Some(AnimeMetaInformation(completeTitle, forwardId, "<genre>", line))
      }
  }

  private def getCompleteTitle(line: String): String =
  {
    log.debug("Parsing complete title from: " + line)

    // ([<][a-zA-Z  ="0-9/.?_-]*[>])*
    val pattern = "[<][a-zA-Z =\"0-9/.\\?_-]*[>]".r

    pattern.replaceAllIn(line, "").trim
  }

  private def getForwardId(line: String): String =
  {
    log.debug("Parsing forward forwardId from: " + line)

    // [a-z=?.]+[0-9]{4}
    val pattern = "[a-z=\\?.]+[0-9]{4}".r

    // forward.php?id=7551
    pattern.findFirstIn(line) match
    {
      case Some(item) => { item.split("=").takeRight(1).head }
      case _ => ""
    }
  }

  private def getRelevantSourceLinesFromPageSource(content: String, title: String): List[String] =
  {
    log.debug("Parsing relevant source lines. Searching for: " + title)

    val preparedTitle = title.toLowerCase.replace("!", "").replace(" ", "-").replace("-", CATCH_ALL+"*")
    val basicPattern = ((CATCH_ALL + "*href=\"forward.php\\?id=[0-9]{4}" + CATCH_ALL + "*%s" + CATCH_ALL).format(preparedTitle)).r
    val pattern = (basicPattern + "*original-titel[\" />]{4}").r

    var found = pattern.findAllIn(content.toLowerCase).toList

    if (found.size == 0)
    {
        //sometimes "original-title" is not there
        found = basicPattern.findAllIn(content.toLowerCase).toList
    }

    found
  }

  private def loadGenrePageSource(meta: AnimeMetaInformation): String =
  {
    val content = loadWebPageSource(ANISEARCH_FORWARD_URL.format(meta.forwardId))

    content
  }

  def getGenreForAnimeMetaInformation(meta: AnimeMetaInformation): AnimeMetaInformation =
  {
    getGenreforAnimeMetaInformation(meta, loadGenrePageSource(meta))
  }

  private def prepareContent(content:String): String =
  {
    content.replace("\n", "").replace("\t", "")
  }

  //TODO umlaute doesn't work at the moment
  private def getGenreforAnimeMetaInformation(meta: AnimeMetaInformation, content: String): AnimeMetaInformation =
  {
    log.debug("Loading genre for " + meta.name + " from " + meta.forwardId)

    val preparedContent = prepareContent(content)

    // Genre[</>a-z = "0-9]*genre_hint[" ]{2}[a-zA-Z0-9 -=üäöÜÄÖ]*
    val pattern = "Genre[</>a-z = \"0-9]*genre_hint[\" ]{2}[a-zA-Z0-9 -=üäöÜÄÖ]*".r
    val found = pattern.findAllIn(preparedContent)
    val relevantLines = found.toList

    if(relevantLines.size > 0)
    {
      // Genre</td><td class="acontent2"><span class="genre_hint" title="Liebesdrama
      val genre = fixUmlaute(relevantLines.head.split("\"")(5))

      AnimeMetaInformation(meta.name, meta.forwardId, genre, meta.rawData, true)
    }
    else
    {
      log.debug("No genre could be parsed/found")

      meta
    }
  }

  private def fixUmlaute(text: String): String =
  {
    var fixedText = text.replace("&ouml;", "ö")
    fixedText = fixedText.replace("&auml;", "ä")
    fixedText = fixedText.replace("&uuml;", "ü")

    fixedText
  }
}
