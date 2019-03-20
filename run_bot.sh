#!/bin/bash

# ==========================================
# hardcoded stuff

# port to find local redis server at
redis_port="6380"

# file with Slack bot user token
slack_token_file="tf_slack_bot_token.txt"

# jar file of randy
## TODO parametrize version of randy 
randy="target/scala-2.11/elo-bot-assembly-1.2.jar"

# ==========================================

# log whether we can see a file tracking bids
if [ -f dump.rdb ]; then
  echo "Reading existing Elo data from dump.rdb..."
else
  echo "No existing Elo data found, starting from an empty Elo db..."
fi

# build redis config file
# save every 5 mins if there is at least 1 change to the dataset
echo save 300 1 > redis.conf
echo port $redis_port >> redis.conf

# start redis server at specified port, disown
redis-server redis.conf &
disown

# ping redis server at port
redis_pong=$(redis-cli -p $redis_port ping)

# exit if we can't find redis 
if [ "$redis_pong" != "PONG" ]; then
   echo "Redis server not running at port $redis_port"
   exit 1
fi 

# make sure we can find the file with the slack token
if [ ! -f "$slack_token_file" ]; then
    echo "Could not find file with Slack token!"
    exit 1
fi

# set necessary environment variables
export SLACK_BOT_REDIS_PORT=$redis_port
export SLACK_TOKEN=$(cat tf_slack_bot_token.txt)

# run the slack bot in the background
java -jar $randy &
# disown the job
disown

