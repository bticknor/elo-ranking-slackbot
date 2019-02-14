package elo

import com.redis.serialization.Parse.Implicits.parseDouble
import slack.SlackUtil
import slack.models.Message

import scala.math.round

object PingPongBot extends App {

  // Help message
  val helpMessage = """
  Hi there! I'm Randy Daytona, True Fit's very own ping pong ranking bot.

  Commands I support:

  "help" - print this message

  "challenge" - notify another user that you'd like to throw down, and compute
  the probability that you will beat them...you must @ them in the message

  "report" - report a loss...you must @ the person you lost to in the message

  "leaderboard" - print the top 5 users in terms of Elo performance rating
  """

  // Fetches a user's Elo performance score
  def getUserScore(user: String): Double = {
    val res = redisClient.get(user)
    // If a user doesn't have a score in the DB, assign them the default
    res match {
      case Some(score) => score.toDouble
      case None => EloRankingSystem.initialScore
    }
  }

  // Build challenge message
  def challengeMessage(challenger: Player, challengee: Player): String = {
    if(challengee == Player.Nobody) {
      "Mention a user to challenge them!" 
    } else {
            // TODO: handle case when either doesn't have a score
      val probChallengerWins = EloRankingSystem.probAbeatsB(
        challenger.score, challengee.score
      )
      s"""
      The gauntlet has been thrown down! <@${challengee}> you have been put on notice!

      <@$challenger> currently has an Elo rating of ${challenger.score}
      <@$challengee> currently has an Elo rating of ${challengee.score}
      <@$challenger> has a ${round(100 * probChallengerWins)}% chance of beating <@$challengee>
      """
    }
  }

  def fetchLeaderboard(): String = {
    // get all users, unpack from Option value returned
    val allUsers = redisClient.keys() match {
      case Some(userlist) => userlist
      case _ => List()
    }
    if(allUsers.length == 0) {
      "No scores yet!"
    } else {
      val usersUnpacked = allUsers.map(userid => userid.get)
      // TODO: handle case where no users in DB
      val allUserScores = usersUnpacked.map(
        id => redisClient.get(id).get.toDouble
      )
      // zip together users and scores
      val usersAndScores = usersUnpacked zip allUserScores
      val sortedScoresDesc = usersAndScores.sortBy(_._2).reverse
      // TODO what about when fewer than 3
      s"""
      True Fit top 3 Ping Pongers:

      1. <@${sortedScoresDesc(0)._1}> ${sortedScoresDesc(0)._2}
      2. <@${sortedScoresDesc(1)._1}> ${sortedScoresDesc(1)._2}
      3. <@${sortedScoresDesc(2)._1}> ${sortedScoresDesc(2)._2}
      """
    }
  }

  def reportLoss(loser: Player, winner: Player): String = {
    // compute rating update
    val loserRatingUpdate = EloRankingSystem.ratingUpdateA(
      loser.score, winner.score, 0
    )
    // update both players' ratings
    val loserUpdatedRating = loser.score + loserRatingUpdate
    // zero sum update, a property of Elo
    val winnerUpdatedRating = winner.score - loserRatingUpdate
    // write new ratings to DB
    val reporterScoreWritten = redisClient.set(loser.slackUserId, loserUpdatedRating.toString)
    val opponentScoreWritten = redisClient.set(winner.slackUserId, winnerUpdatedRating.toString)

    val successMessage = s"""
    <@${reporter}> has reported a loss to <@${opponent}>.  Get 'em next time!

    <@${reporter}>'s Elo rating changed from ${reporterRating} to ${reporterUpdatedRating}.
    <@${opponent}>'s Elo rating changed from ${opponentRating} to ${opponentUpdatedRating}.
    """
    // check that scores are written successfully
    Seq(reporterScoreWritten, opponentScoreWritten) match {
      case Seq(true, true) => successMessage
      case _ => "Uh oh - issue updating user ratings in the DB!"
    }
  }

  // Main entry point for message logic
  def onMessageAction(message: Message): Unit = {
    val mentionedIds = SlackUtil.extractMentionedIds(message.text)
    // check if the bot is mentioned
    if(mentionedIds.contains(selfId)) {

      // print help message if prompted
      if(message.text.contains("elp")) {
        slackClient.sendMessage(message.channel, helpMessage)
      }

      // print leaderboard if prompted
      if(message.text.contains("eaderboard")) {
        val leaderboard = fetchLeaderboard
        slackClient.sendMessage(message.channel, leaderboard)
      }

      // get ID of first other user mentioned
      val challengee = mentionedIds
        .find(_ != selfId) // retrieves first element for which the find condition is true
        .map(PlayerService.playerService.getPlayer)
        .getOrElse(Player.Nobody)

      // if its a challenge, send a challenge message
      if(message.text.contains("hallenge")) {
        val challenger = PlayerService.playerService.getPlayer(message.user)
        val chalMessage = challengeMessage(challenger, challengee)
        slackClient.sendMessage(message.channel, chalMessage)
      }

      // if it's a report message, update scores
      if(message.text.contains("eport")) {
        // TODO the concept of a "nobody" user should be replaced with an Option of a user
        val reportMessage = if(challengee == "nobody") {
          "Mention a user to report a loss to them!"
        } else {
          reportLoss(message.user, challengee)
        }
        slackClient.sendMessage(message.channel, reportMessage)
      }
    }
  }
  // Listen in on Slack via RTM API
  slackClient.onMessage(onMessageAction)
}


