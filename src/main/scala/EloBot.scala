import slack.rtm.SlackRtmClient
import akka.actor.ActorSystem
import slack.SlackUtil


object PingPongBot extends App {

  implicit val system = ActorSystem("slack")
  implicit val ec = system.dispatcher


  val token = "xoxp-78086072006-78121176342-546690664693-2a53ec8bf21066395d730701bc78d5f0"
  val client = SlackRtmClient(token)

  val selfId = client.state.self.id

  client.onMessage { message =>
  val mentionedIds = SlackUtil.extractMentionedIds(message.text)

	if(mentionedIds.contains(selfId)) {
	  client.sendMessage(message.channel, s"<@${message.user}>: Hey!")
	}
  }
}

