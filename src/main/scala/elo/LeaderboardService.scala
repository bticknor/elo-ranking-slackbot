package elo

class LeaderboardService(playerService: PlayerService) {

  def getLeaderboard(numTopPlayers: Int = 3): Seq[Player] = {
    playerService.findAllPlayers.sortBy(-_.score).take(numTopPlayers)
  }

  def formatLeaderboard(topPlayers: Seq[Player]): String = {
    if (topPlayers.isEmpty) {
      "No scores yet!"
    } else {
      val formattedTopPlayers = topPlayers
        .zipWithIndex
        .map { case (player, rank) =>
          s"$rank. <@${player.slackUserId}> ${player.formattedScore}"
        }
      s"""
      True Fit top ${topPlayers.size} Ping Pongers:

      $formattedTopPlayers
      """
    }
  }
}

object LeaderboardService {

  val leaderboardService = new LeaderboardService(
    playerService = PlayerService.playerService)
}