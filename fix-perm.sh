#!/bin/bash

find $1 -type d | xargs chmod a+rx
find $1 -type f | xargs chmod a+r
