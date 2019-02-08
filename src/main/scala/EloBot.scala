import slack.rtm.SlackRtmClient
import akka.actor.ActorSystem
import slack.SlackUtil


object PingPongBot extends App {

  implicit val system = ActorSystem("slack")
  implicit val ec = system.dispatcher


  val token = "xoxb-151175863378-ckhDjYJDrGDM3Wpu40ijKwLp"
  val client = SlackRtmClient(token)

  val selfId = client.state.self.id

  client.onMessage { message =>
  val mentionedIds = SlackUtil.extractMentionedIds(message.text)

	if(mentionedIds.contains(selfId)) {
	  client.sendMessage(message.channel, s"<@${message.user}>: Hey!")
	}
  }
}

