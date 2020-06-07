#!/bin/bash

BIN_FOLDER="../../../../out/bin"
TASKSET="/Users/jjs/Documents/myProject/ScheduLeak-Simulator/experiments/tasksets/edfScheduLeak/coverage1to999"
DURATION="20000"
CASE="DURATION"
OUTPUT_FOLDER="."

$BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/edf_d$DURATION -d $DURATION -r 1000000 -v -p edf -e uapenmeanslot -c $CASE
$BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/ro3_d$DURATION -d $DURATION -r 1000000 -v -p reorder3 -e uapenmeanslot -c $CASE
$BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/lp10_d$DURATION -d $DURATION -r 1000000 -v -p laplace10 -e uapenmeanslot -c $CASE
$BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/lp1000_d$DURATION -d $DURATION -r 1000000 -v -p laplace1000 -e uapenmeanslot -c $CASE
