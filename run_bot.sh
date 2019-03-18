#!/bin/bash

# ==========================================
# hardcoded stuff

# port to find local redis server at
redis_port="6380"

# file with Slack bot user token
slack_token_file="tf_slack_bot_token.txt"

# jar file of randy
randy="randy-1.2.jar"

# ==========================================

# start redis server at specified port, disown
redis-server --port $redis_port &
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

