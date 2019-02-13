import PingPongBot.redisClient
import com.redis.serialization.Parse.Implicits.parseDouble
import elo.EloRankingSystem

class UserService {

  def constructUser(userName: String): User = {
    if (userName.toLowerCase == "nobody") {
      User.Nobody
    } else {
      // If a user doesn't have a score in the DB, assign them the default
      val userScore = redisClient.get[Double](userName).getOrElse(EloRankingSystem.initialScore)
      User(userName, userScore)
    }
  }

  def findAllUsers: Seq[User] = {
    redisClient
      .keys[String]()
      .getOrElse(Nil)
      .flatten // get rid of keys that are Nones
      .map(constructUser) // build user objects for each key
  }
}

object UserService {
  val userService = new UserService()
}
