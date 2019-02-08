package io.scalac.slack.example

import io.scalac.slack.MessageEventBus
import io.scalac.slack.bots.AbstractBot
import io.scalac.slack.common.{BaseMessage, Command, OutboundMessage}
import com.redis._
import scala.math.pow

class RandyDaytona(override val bus: MessageEventBus) extends AbstractBot {

  // Connects to a locally run redis instance on default port
  val rClient = new RedisClient("localhost", 6379)

  // TODO
  def getUserRating(user: String): String = "TODO"

  // Expected probability that player A beats player B
  def expProbAbeatsB(RatingA: String, RatingB: String): Double = {
    // get rating difference
    val rating_diff = RatingA.toDouble - RatingB.toDouble
    val denominator = 1 + pow(10, rating_diff / 400.0)
    pow(denominator, -1)
  }

  // TODO update this
  override def help(channel: String): OutboundMessage =
    OutboundMessage(channel,
      s"$name will help you to solve difficult math problems \\n" +
      "Usage: $calc {operation} {arguments separated by space}")

  val possibleOperations = Map(
    "+" -> ((x: Double, y: Double) => x+y),
    "-" -> ((x: Double, y: Double) => x-y),
    "*" -> ((x: Double, y: Double) => x*y),
    "/" -> ((x: Double, y: Double) => x/y)
  )

  override def act: Receive = {
    case Command("calc", operation :: args, message) if args.length >= 1 =>
      val op = possibleOperations.get(operation)

      val response = op.map(f => {
        val result = args.map(_.toDouble).reduceLeft( f(_,_) )
        OutboundMessage(message.channel, s"Results is: $result")
      }).getOrElse(OutboundMessage(message.channel, s"No operation $operation"))

      publish(response)

    case Command("calc", _, message) =>
      publish(OutboundMessage(message.channel, s"No arguments specified!"))
  }
}
