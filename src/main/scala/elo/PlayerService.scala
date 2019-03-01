package elo

import com.redis.serialization.Parse.Implicits.parseDouble

class PlayerService {

  def getPlayer(slackUserId: SlackUserId): Option[Player] = {
    // check to see if it's a valid player id
    // if so get the score or default
    if(slackUserId.isValid) {
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
      .flatMap(getPlayer(_)) // build user objects for each key
  }
}

object PlayerService {
  val playerService = new PlayerService()
}
