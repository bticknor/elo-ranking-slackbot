package elo

case class Player(slackUserId: String, score: Double)

object Player {
  val Nobody = Player("nobody", 0.0)
}
