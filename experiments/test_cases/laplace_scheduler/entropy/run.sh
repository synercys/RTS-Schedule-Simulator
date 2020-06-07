#!/bin/bash

BIN_FOLDER="../../../../out/bin"
TASKSET="/Users/jjs/Documents/myProject/ScheduLeak-Simulator/experiments/tasksets/edfScheduLeak/coverage1to999"
DURATION="20000"
CASE="DURATION"
OUTPUT_FOLDER="."

$BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/d$DURATION -d $DURATION -r 1000000 -v -p edf -e uapenmeanslot -c $CASE
