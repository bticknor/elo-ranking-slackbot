# Ping Pong Ranking Slack Bot

This is really a general purpose ranking slackbot, which uses the Elo ranking system (https://en.wikipedia.org/wiki/Elo_rating_system).  The Elo system uses assigns performance ratings to players, and uses the difference in player rating values to predice the outcome of matches, updating ratings based on match outcomes.

## Usage

The bot currently supports four commands:

- "help" 
- "challenge"
- "congrats"
- "leaderboard"

The help command prints this usage message, which describes the other commands:

```
Hi there! I'm {APP_NAME}, the ping pong ranking bot.

Commands I support:

"help" - print this message

"challenge <player>" - notify another user that you'd like to throw down, and compute
 the probability that you will beat them...you must @ them in the message

"congrats <player>" - report a loss / congratulate the winner...you must @ the person you lost to in the message

"leaderboard [n]" - print the top n users in terms of Elo performance rating, with
 3 being the default
```

## Installation / Deployment

Installation of the bot installs registering a Slack app (https://api.slack.com/apps) for your workspace and retrieving a Bot User OAuth Access Token, which can be found in `Oauth & Permissions`.

Deployment is done by following these steps:

- Clone this repo
- Go to the root directory of the repo and run `sbt assembly` to creat a "fat jar" file
- Run the `run_bot.sh` shell script, which requires that your bot user token be in the same directory in the `tf_slack_bot_token.txt` file

This script essentially runs a redis server and then executes the fat jar executable.  A Dockerfile is also included in this repo that can be used to build a Docker image containing the code and all dependencies.
