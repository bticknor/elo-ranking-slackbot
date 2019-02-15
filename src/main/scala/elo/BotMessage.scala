package elo

import scala.math.round

sealed trait BotMessage

object HelpMessage extends BotMessage {
  override def toString = """
    Hi there! I'm Randy Daytona, True Fit's very own ping pong ranking bot.

    Commands I support:

    "help" - print this message

    "challenge" - notify another user that you'd like to throw down, and compute
    the probability that you will beat them...you must @ them in the message

    "report" - report a loss...you must @ the person you lost to in the message

    "leaderboard" - print the top 5 users in terms of Elo performance rating
    """
}

case class ChallengeMessage(challenger: Player, challengee: Player, probChallengerWins: Double) extends BotMessage {
  override def toString = s"""
    The gauntlet has been thrown down! <@${challengee.slackUserId}> you have been put on notice!

    <@${challenger.slackUserId}> currently has an Elo rating of ${challenger.score}
    <@${challengee.slackUserId}> currently has an Elo rating of ${challengee.score}
    <@${challenger.slackUserId}> has a ${round(100 * probChallengerWins)}% chance of beating <@${challengee.slackUserId}>
    """
}

object NoUserMentionedMessage extends BotMessage {
  override def toString = "Mention a user to challenge them!"
}