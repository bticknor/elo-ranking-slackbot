package elo
import scala.util.Try

class LeaderboardService(playerService: PlayerService) {

  val DefaultLeaderboardSize = 3

  // TODO move this into a dedicated message parsing class/module
  def getLeaderboardSize(messageText: String): Int = {
    // get position of token after first "leaderboard" invocation
    val splitCommand = messageText.split(" ")
    val posNumPlayers = splitCommand
      .map(x => x.contains("eaderboard"))
      .indexOf(true) + 1
    // get the token after the "leaderboard" token in the message
    // try to convert that to int, if we can't return the default
    // if "leaderboard" is the last token in the message return the default
    (for {
      numPlayersStr <- splitCommand.lift(posNumPlayers) if posNumPlayers != splitCommand.length
      numPlayers <- Try(numPlayersStr.toInt).toOption
    } yield {
      numPlayers
    }).getOrElse(DefaultLeaderboardSize)
  }

  def getLeaderboard(numTopPlayers: Int): Seq[Player] = {
    playerService.findAllPlayers
      .sortBy(-_.score)
      .take(numTopPlayers)
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
