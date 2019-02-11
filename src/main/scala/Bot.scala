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
  Hi there! TODO
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
    if(mentionedIds.contains(selfId)) {
      val score = getUserScore(message.user)

      // slackClient.sendMessage(message.channel, s"<@${message.user}>: Sup dawg!")
      // redisClient.set(message.user, "69")
      slackClient.sendMessage(message.channel, s"<${score}>: this yo sco!")
    }
  }

  slackClient.onMessage(onMessageAction)
}
