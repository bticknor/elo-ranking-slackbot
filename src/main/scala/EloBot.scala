import slack.rtm.SlackRtmClient
import akka.actor.ActorSystem

implicit val system = ActorSystem("slack")
implicit val ec = system.dispatcher


val token = "<Your Token Here>"
val client = SlackRtmClient(token)

val selfId = client.state.self.id

client.onMessage { message =>
  val mentionedIds = SlackUtil.extractMentionedIds(message.text)

  if(mentionedIds.contains(selfId)) {
    client.sendMessage(message.channel, s"<@${message.user}>: Hey!")
  }
}

