package elo

class LeaderboardService(playerService: PlayerService) {

  def getLeaderboard(numTopPlayers: Int = 3): Seq[Player] = {
    playerService.findAllPlayers.sortBy(-_.score).take(numTopPlayers)
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