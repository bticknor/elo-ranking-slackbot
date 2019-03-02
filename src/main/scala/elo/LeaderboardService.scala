package elo
import scala.util.Try

class LeaderboardService(playerService: PlayerService) {

  val DefaultLeaderboardSize = 3

  // TODO move this into a dedicated message parsing class/module
  def getLeaderboardSize(messageText: String): Option[Int] = {
    // get position of token after first "leaderboard" invocation
    val splitCommand = messageText.split(" ")
    val posNumPlayers = splitCommand
      .map(x => x.contains("eaderboard"))
      .indexOf(true) + 1
    // "leaderboard" is last token in message, return None
    // if the token after "leaderboard" cannot be cast to int, return None
    if(posNumPlayers == splitCommand.length) None else{
      val numPlayersShow = splitCommand(posNumPlayers)
      Try(numPlayersShow.toInt).toOption
    }
  }

  def getLeaderboard(numTopPlayers: Option[Int]): Seq[Player] = {
    playerService.findAllPlayers
      .sortBy(-_.score)
      .take(numTopPlayers.getOrElse(DefaultLeaderboardSize))
  }

  def formatLeaderboard(topPlayers: Seq[Player]): String = {
    if (topPlayers.isEmpty) {
      "No scores yet!"
    } else {
      topPlayers
        .zip(1 to topPlayers.size)
        .map { case (player, rank) => s"$rank. <@${player.slackUserId}> ${player.formattedScore}" }
        .foldLeft(s"True Fit top ${topPlayers.size}\n")((concatMsg, playerLine) => concatMsg + "\n" + playerLine)
    }
  }
}

object LeaderboardService {

  val leaderboardService = new LeaderboardService(
    playerService = PlayerService.playerService)
}
