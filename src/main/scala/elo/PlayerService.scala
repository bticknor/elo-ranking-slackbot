package elo

import com.redis.serialization.Parse.Implicits.parseDouble

class PlayerService {

  def getPlayer(slackUserId: String): Player = {
    if (slackUserId.toLowerCase == "nobody") {
      Player.Nobody
    } else {
      // If a user doesn't have a score in the DB, assign them the default
      val userScore = redisClient.get[Double](slackUserId).getOrElse(EloRankingSystem.initialScore)
      Player(slackUserId, userScore)
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
