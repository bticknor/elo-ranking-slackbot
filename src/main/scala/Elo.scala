package elo

import scala.math.pow

// Namespace for Elo ranking logic
object EloRankingSystem {

  // k parameter
  val k = 32

  // Prior probability that player A beats player B, given their ratings
  def probAbeatsB(A_rating: Double, B_rating: Double): Double = {
    val denom = 1 + pow(10, (B_rating - A_rating) / 400)
    pow(denom, -1)
  }

  // Value to add to player A's score to update it after playing player B
  // A_win is encoded as a 1 if A wins, 0 otherwise
  def ratingUpdateA(A_rating: Double, B_rating: Double, A_win: Int): Double = {
    val expected = probAbeatsB(A_rating, B_rating)
    (A_win.toDouble - expected) * k
  }
}
