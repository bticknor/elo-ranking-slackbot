package elo

case class Player(slackUserId: SlackUserId, score: Double) {

  def formattedScore: String = f"$score%1.2f"
}

object Player {
  val Nobody = Player("nobody", 0.0)
}

case class SlackUserId(userId: String) {
  // test of whether an ID mentioned is a valid player
  // slack user IDs are prefixed with "UD"
  def isValid: Boolean = userId.contains("UD")

  override def toString: String = userId
}