import slack.rtm.SlackRtmClient
import slack.models.Message
import akka.actor.ActorSystem
import slack.SlackUtil
import com.redis._
import elo._

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

  "report" - report a score...you can only report losing scores, and must @
  the person you lost to in the message

  "leaderboard" - print the top 5 users in terms of Elo performance rating
  """

  // Fetches a user's Elo performance score
  def getUserScore(user: String): Double = {
    val res = redisClient.get(user)
    res match {
      case Some(score) => score.toDouble
      case None => 0.0
    }
  }

  // Main entry point for message logic
  def onMessageAction(message: Message): Unit = {
    val mentionedIds = SlackUtil.extractMentionedIds(message.text)
    // If the bot is mentioned
    if(mentionedIds.contains(selfId)) {
      
      if(message.text.contains("elp")) {
        slackClient.sendMessage(message.channel, helpMessage)
      }
      // val score = getUserScore(message.user)
      
      // redisClient.set(message.user, "69")
      // slackClient.sendMessage(message.channel, s"<${score}>: this yo sco!")
    }
  }

  slackClient.onMessage(onMessageAction)
}
