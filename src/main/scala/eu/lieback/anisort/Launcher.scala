package eu.lieback.anisort

import menu.MainMenu

object Launcher
{
  def main(args: Array[String])
  {
    val menu = new MainMenu
    menu.mainMenu
  }
}
