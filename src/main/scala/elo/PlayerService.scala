package elo

import com.redis.serialization.Parse.Implicits.parseDouble

class PlayerService {

  // test of whether an ID mentioned is a valid player
  // slack user IDs are prefixed with "UD"
  def validPlayerID(id: SlackUserId): Boolean = id.contains("UD")

  def getPlayer(slackUserId: SlackUserId): Option[Player] = {
    // check to see if it's a valid player id
    // if so get the score or default
    if(validPlayerID(slackUserId)) {
      val userScore = redisClient
        .get[Double](slackUserId)
        .getOrElse(EloRankingSystem.initialScore)
      Some(Player(slackUserId, userScore))
    } else {
      None
    }
}

  def findAllPlayers: Seq[Player] = {
    redisClient
      .keys[String]()
      .getOrElse(Nil)
      .flatten // get rid of keys that are Nones
      .flatMap(getPlayer) // build user objects for each key
  }
}

object PlayerService {
  val playerService = new PlayerService()
}
