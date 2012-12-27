package anisearch

import org.scalatest._
import matchers.ShouldMatchers
import eu.lieback.anisort.anisearch.AniSearchParser


/*
  A Channel/
Afro.Samurai.Resurrection.2009.German.DTS.DL.1080p.BluRay.x264-LeetHD/
Amagami SS Plus/
Black Lagoon Robertas Blood Trail OVA 01-05 komplett Xvid DVDRip ger-jap-dub ger-sub [AST4u]/
ChäoS;HEAd/
Clannad ~After Story~/
CODE-E/
Cube_x_Cursed_x_Curious/
Denpa Onna to Seishun Otoko/
Die.Legende.von.Korra/
Disciples.III.Resurrection.GERMAN-0x0007/
[EAF]Hanasaku.Iroha_Ep01-26_(ger.sub-x264)/
Final.Fantasy.VII.Remake-RELOADED/
Galaxy_Angel/
Kaempfer/
Kenran.Butoh.Sai.-.The.Mars.Daybreak_01-26/
[kK]Minami-ke_01-13/
Love_Selection/
Mobile Police Patlabor OVA/
[No]Kimi_to_Boku_-_01-13_(Hi10P)/
[OtakuKingdomSubs]Amagami_SS_plus_1-13[BD][720p][Hi10P]/
[OtakuKingdomSubs]Amagami_SS_plus_Special_1-7[BD][720p][Hi10P]/
Queens Blade/
Sora no Otoshimono/
Sumomomo Momomo - Chijou Saikyou no Yome/
Tamayura ~hitotose~/
Tamayura_Hitotose/
[TnF]Bleach_220-366_HD[GerSub]/
Tsubasa.Chronicle.01-26.Ger-Jap-Frz-Dub.BDRip.1080p.x264/
Xenosaga - The Animation/
  */


class AniSearchParserTest extends FlatSpec
{

  /*"A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    stack.pop() should equal (2)
    stack.pop() should equal (1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[String]
    evaluating { emptyStack.pop() } should produce [NoSuchElementException]
  }*/

  "the parser" should "parse tamayura" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("tamayura")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Alltagsdrama", "there should be a genre")
    }

  "the parser" should "parse mirai nikki" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("mirai nikki")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Horror", "there should be a genre")
    }

  "the parser" should "parse chu bra!!" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("chu bra!!")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Komödie", "there should be a genre")
    }

  "the parser" should "parse tamayura_hitotose" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("tamayura_hitotose")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Alltagsdrama", "there should be a genre")
    }

  "the parser" should "parse tamayura ~hitotose~" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("tamayura ~hitotose~")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Alltagsdrama", "there should be a genre")
    }

  "the parser" should "parse Clannad ~After Story~" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("Clannad ~After Story~")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Sentimentales Drama", "there should be a genre")
    }

  "the parser" should "parse Cube_x_Cursed_x_Curious" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("Cube_x_Cursed_x_Curious")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Romantische Komödie", "there should be a genre")
    }

  /*"the parser" should "parse Sumomomo Momomo - Chijou Saikyou no Yome" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("Sumomomo Momomo - Chijou Saikyou no Yome")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Romantische Komödie", "there should be a genre")
    }*/

  "the parser" should "parse Galaxy_Angel" in
    {
      val meta = AniSearchParser.searchForMetaInformationByAnimeTitle("Galaxy_Angel")
      assert(meta.size >= 1, "there should be metas")

      val genre = AniSearchParser.getGenreForAnimeMetaInformation(meta.head)
      assert(genre.genre == "Nonsense-Komödie", "there should be a genre")
    }



}


