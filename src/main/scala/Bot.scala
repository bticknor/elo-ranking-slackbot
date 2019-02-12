import slack.rtm.SlackRtmClient
import slack.models.Message
import akka.actor.ActorSystem
import slack.SlackUtil
import com.redis._
import elo._
import scala.math.round

object PingPongBot extends App {

  implicit val system = ActorSystem("slack")
  implicit val ec = system.dispatcher

  // =========================================
  // Internal state of the bot

  // Connection token - TODO catch exception
  val token = sys.env("SLACK_TOKEN")

  // Connection to local redis server
  val redisClient = new RedisClient("localhost", 6379)

  // RTM connection to Slack
  val slackClient = SlackRtmClient(token)

  // ID of bot
  val selfId = slackClient.state.self.id

  // =========================================

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
  def challengeMessage(challenger: String, challengee: String): String = {
    if(challengee == "nobody") {
      "Mention a user to challenge them!" 
    } else {
      val challengerRating = getUserScore(challenger)
      val challengeeRating = getUserScore(challengee)
      // TODO: handle case when either doesn't have a score
      val probChallengerWins = EloRankingSystem.probAbeatsB(
        challengerRating, challengeeRating
      )
      s"""
      The gauntlet has been thrown down! <@${challengee}> you have been put on notice!

      <@${challenger}> currently has an Elo rating of ${challengerRating.toString}
      <@${challengee}> currently has an Elo rating of ${challengeeRating.toString}
      <@${challenger}> has a ${round(100 * probChallengerWins).toString}% chance of beating <@${challengee}>
      """
    }
  }

  def fetchLeaderboard(): String = {
    // TODO!
    "leaderboard"
  }

  def reportLoss(reporter: String, opponent: String): String = {
    // hit DB for the current scores
    val reporterRating = getUserScore(reporter)
    val opponentRating = getUserScore(opponent)
    // compute rating updates
    val reporterRatingUpdate = EloRankingSystem.ratingUpdateA(
      reporterRating, opponentRating, 0
    )
    // update both players' ratings
    val reporterUpdatedRating = reporterRating + reporterRatingUpdate
    val opponentUpdatedRating = opponentRating - reporterRatingUpdate
    // write new ratings to DB
    val reporterScoreWritten = redisClient.set(reporter, reporterUpdatedRating.toString)
    val opponentScoreWritten = redisClient.set(opponent, opponentUpdatedRating.toString)

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
      // TODO this will throw an exception if the Seq[String] is empty
      val othersMentioned = mentionedIds.filter(
        Id => Id != selfId
      )
      // get ID of first other user mentioned
      val challengee = othersMentioned match {
        case Seq() => "nobody"
        case _ => othersMentioned.head
      }




      // if its a challenge, send a challenge message
      if(message.text.contains("hallenge")) {
        val chalMessage = challengeMessage(message.user, challengee)
        slackClient.sendMessage(message.channel, chalMessage)
      }
    }
  }

  slackClient.onMessage(onMessageAction)
}
