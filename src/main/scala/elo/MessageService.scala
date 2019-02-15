package elo

import slack.SlackUtil
import slack.models.Message

class MessageService {

  private type MessageFactory = Message => Option[BotMessage]
  val messageFactories: Seq[MessageFactory] = Seq(helpMessage, challengeMessage)

  def helpMessage(message: Message): Option[BotMessage] = {
    if(message.text.contains("elp")) {
      Some(HelpMessage)
    } else {
      None
    }
  }

  def challengeMessage(message: Message): Option[BotMessage] = {
    // if its a challenge, send a challenge message
    if (message.text.contains("hallenge")) {
      // need valid identities for both players for a challenge
      val players = extractPlayersFromMessage(message)
      val challengeMsg = players.map { case (challenger, challengee) =>
        val probChallengerWins = EloRankingSystem.probAbeatsB(challenger.score, challengee.score)
        ChallengeMessage(challenger, challengee, probChallengerWins)
      }.getOrElse(NoUserMentionedMessage)
      Some(challengeMsg)
    } else {
      None
    }
  }

  def buildResponseMessages(message: Message): Seq[BotMessage] = {
    messageFactories.flatMap(_(message))
  }

  def extractPlayersFromMessage(message: Message): Option[(Player, Player)] = {
    val mentionedIds = SlackUtil.extractMentionedIds(message.text)
    // this will never throw an exception since messages necessarily have a user
    val challengerOpt = PlayerService.playerService.getPlayer(message.user)

    // get first other player mentioned
    val challengeeOpt = mentionedIds // seq[string]
      .find(_ != selfId) // option[string]
      .flatMap(PlayerService.playerService.getPlayer)

    for {
      challenger <- challengerOpt
      challengee <- challengeeOpt
    } yield {
      (challenger, challengee)
    }
  }
}
