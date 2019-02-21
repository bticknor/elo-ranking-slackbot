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

  "congrats" - report a loss / congratulate the winner...you must @ the person you
  lost to in the message

  "leaderboard" - print the top 3 users in terms of Elo performance rating
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
    val probChallengerWins = EloRankingSystem.probAbeatsB(
      challenger.score, challengee.score
    )
    s"""
    The gauntlet has been thrown down! <@${challengee.slackUserId}> you have been put on notice!

    <@${challenger.slackUserId}> currently has an Elo rating of ${formatScore(challenger.score)}
    <@${challengee.slackUserId}> currently has an Elo rating of ${formatScore(challengee.score)}
    <@${challenger.slackUserId}> has a ${round(100 * probChallengerWins)}% chance of beating <@${challengee.slackUserId}>
    """
  }

  def fetchLeaderboard(): String = {
    // get all users, unpack from Option value returned
    val allUsersOpt = redisClient.keys[String]().getOrElse(List())
    if(allUsersOpt.length == 0) {
      "No scores yet!"
    } else {
      val users = allUsersOpt.flatten
      // TODO: handle case where no users in DB
      val userScoresStr = users.map(
        id => redisClient.get(id)
      ).flatten
      // cast scores to numerics
      val userScores = userScoresStr.map(score => score.toDouble)
      // zip together users and scores
      val usersAndScores = users zip userScores
      val sortedScoresDesc = usersAndScores.sortBy(_._2).reverse
      // TODO what about when fewer than 3
      s"""
      True Fit top 3 Ping Pongers:

      1. <@${sortedScoresDesc(0)._1}> ${formatScore(sortedScoresDesc(0)._2)}
      2. <@${sortedScoresDesc(1)._1}> ${formatScore(sortedScoresDesc(1)._2)}
      3. <@${sortedScoresDesc(2)._1}> ${formatScore(sortedScoresDesc(2)._2)}
      """
    }
  }

  def reportLoss(loser: Player, winner: Player): String = {
    // compute rating update
    val loserRatingUpdate = EloRankingSystem.ratingUpdateA(
      loser.score, winner.score, 0
    )
    // update both players' ratings
    val loserUpdatedRating = formatScore(loser.score + loserRatingUpdate)
    // zero sum update, a property of Elo
    val winnerUpdatedRating = formatScore(winner.score - loserRatingUpdate)
    // write new ratings to DB
    val reporterScoreWritten = redisClient.set(loser.slackUserId, loserUpdatedRating)
    val opponentScoreWritten = redisClient.set(winner.slackUserId, winnerUpdatedRating)

    val successMessage = s"""
    <@${loser.slackUserId}> has reported a loss to <@${winner.slackUserId}>.  Get 'em next time!

    <@${loser.slackUserId}>'s Elo rating changed from ${formatScore(loser.score)} to ${loserUpdatedRating}.
    <@${winner.slackUserId}>'s Elo rating changed from ${formatScore(winner.score)} to ${winnerUpdatedRating}.
    """
    // check that scores are written successfully
    Seq(reporterScoreWritten, opponentScoreWritten) match {
      case Seq(true, true) => successMessage
      case _ => "Uh oh - issue updating user ratings in the DB!"
    }
  }

  // Main entry point for message logic
  // TODO clean this up
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

      // this will never throw an exception since messages necessarily have a user
      val challengerOpt = PlayerService.playerService.getPlayer(message.user)

      // get first other player mentioned
      val challengeeID = mentionedIds // seq[string]
        .find(_ != selfId) // option[string]
        .getOrElse("nobody")
      val challengeeOpt = PlayerService.playerService.getPlayer(challengeeID)

      // if its a challenge, send a challenge message
      if(message.text.contains("hallenge")) {
        // need valid identities for both players for a challenge
        val chalMessage = (for {
          challenger <- challengerOpt
          challengee <- challengeeOpt
        } yield {
          challengeMessage(challenger, challengee)
        }).getOrElse("Mention a user to challenge them!")
        slackClient.sendMessage(message.channel, chalMessage)
      }

      // if it's a report message, update scores
      if(message.text.contains("ongrats")) {
        // need valid identities for both players for a loss report
        val reportMessage = (for {
          challenger <- challengerOpt
          challengee <- challengeeOpt
        } yield {
          reportLoss(challenger, challengee)
        }).getOrElse("Mention a user to report a loss to them!")
        slackClient.sendMessage(message.channel, reportMessage)
      }
    }
  }
  // Listen in on Slack via RTM API
  slackClient.onMessage(onMessageAction)
}
