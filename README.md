# Ping Pong Ranking Slack Bot

This is really a general purpose ranking slackbot, which uses the Elo ranking system (https://en.wikipedia.org/wiki/Elo_rating_system).  The Elo system uses assigns performance ratings to players, and uses the difference in player rating values to predice the outcome of matches, updating ratings based on match outcomes.

## Usage

The bot currently supports four commands:

- "help" 
- "challenge"
- "report"
- "leaderboard"

The help command prints this usage message, which describes the other commands:

```
Hi there! I'm {APP_NAME}, True Fit's very own ping pong ranking bot.

Commands I support:

"help" - print this message

"challenge" - notify another user that you'd like to throw down, and compute
 the probability that you will beat them...you must @ them in the message

"report" - report a loss...you must @ the person you lost to in the message

"leaderboard" - print the top 5 users in terms of Elo performance rating
```

## Installation / Deployment

TODO finish section

Installation of the bot installs registering a Slack app (https://api.slack.com/apps) for your workspace and retrieving a Bot User OAuth Access Token, which can be found in the `Oauth & Permissions` section on the website.

Right now, deployment of the bot requires that you locally generate the "fat jar" file that includes the compiled bot code and bundled dependencies (this process requires Java and SBT):

- Clone this repo
- Go to the root directory of the repo and run `sbt assembly`
- The "fat jar" file can be found in `target/scala-2.11/elo-bot-assembly-{VERSION}.jar`

To run the bot, follow these steps:

- Run a Redis server on port XXX:

  ` redis-server -p XXX`

- Set the `SLACK_BOT_REDIS_PORT` environment variable to point at that server:

  `export SLACK_BOT_REDIS_PORT=XXX`

- Set the `SLACK_TOKEN` environment variable to the value of the bot OAuth token:

  `export SLACK_TOKEN=MY_TOKEN_VALUE`

- Run the bot "fat jar" file:

  `java -jar elo-bot-assembly-{VERSION}.jar`


