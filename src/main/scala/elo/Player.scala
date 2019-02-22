package elo

case class Player(slackUserId: String, score: Double) {

  def formattedScore: String = f"$score%1.2f"
}

object Player {
  val Nobody = Player("nobody", 0.0)
}
