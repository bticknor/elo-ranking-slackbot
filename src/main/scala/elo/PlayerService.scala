package elo

import com.redis.serialization.Parse.Implicits.parseDouble

class PlayerService {

  // test of whether an ID mentioned is a valid player
  // slack user IDs are prefixed with "UD"
  def validPlayerID(id: String): Boolean = id.contains("UD")

  def getPlayer(slackUserId: String): Option[Player] = {
    // fetch score or default
    val userScore = redisClient.get[Double](slackUserId).getOrElse(EloRankingSystem.initialScore)
    // Checks ID to see if it is valid, then returns player with score or default
    validPlayerID(slackUserId) match {
      case true => Some(Player(slackUserId, userScore))
      case _ => None
    }
  }

  def findAllPlayers: Seq[Player] = {
    redisClient
      .keys[String]()
      .getOrElse(Nil)
      .flatten // get rid of keys that are Nones
      .map(getPlayer) // build user objects for each key
  }
}

object PlayerService {
  val playerService = new PlayerService()
}
