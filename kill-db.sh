#!/bin/bash

SERVER_PID=`ps aux | grep -e org.h2.tools.Server | grep \`whoami\` | grep -v grep | tr -s " " | cut -d " "  -f2`

if [ "$SERVER_PID" != "" ]
then
    echo "Killing pid ${SERVER_PID}..."
    kill $@ $SERVER_PID
else
    echo "No server found, exiting..."
fi
