import slack.rtm.SlackRtmClient
import slack.models.Message
import akka.actor.ActorSystem
import slack.SlackUtil
import com.redis._


object PingPongBot extends App {

  implicit val system = ActorSystem("slack")
  implicit val ec = system.dispatcher

  // Connection token - TODO catch exception
  val token = sys.env("SLACK_TOKEN")
  
  // Connection to local redis server
  val redisClient = new RedisClient("localhost", 6379)

  // RTM connection to Slack
  val slackClient = SlackRtmClient(token)

  // ID of bot
  val selfId = slackClient.state.self.id

  // Action to take 
  def onMessageAction(message: Message): Unit = {
    val mentionedIds = SlackUtil.extractMentionedIds(message.text)
    if(mentionedIds.contains(selfId)) {
      slackClient.sendMessage(message.channel, s"<@${message.user}>: Sup dawg!")
    }
  }

  slackClient.onMessage(onMessageAction)
}

