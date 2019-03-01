import akka.actor.ActorSystem
import com.redis.RedisClient
import slack.rtm.SlackRtmClient

package object elo {

  implicit val system = ActorSystem("slack")
  implicit val ec = system.dispatcher

  // =========================================
  // Internal state of the bot

  // Connection token - TODO catch exception
  val token = sys.env("SLACK_TOKEN")

  // Connection to local redis server
  val redisPort = sys.env("SLACK_BOT_REDIS_PORT").toInt
  val redisClient = new RedisClient("localhost", redisPort)

  // RTM connection to Slack
  val slackClient = SlackRtmClient(token)

  // ID of bot
  val selfId = slackClient.state.self.id

  // =========================================
  // Utility functions

  // formats user score to be rounded to two decimal places
  @deprecated(message = "use Player.formattedScore")
  def formatScore(userScore: Double): String = f"$userScore%1.2f"

  // =========================================
  // Type ALL the things!

  implicit class SlackUserId(userId: String) {
    // test of whether an ID mentioned is a valid player
    // slack user IDs are prefixed with "UD"
    def isValid: Boolean = userId.contains("UD")
  }
}
