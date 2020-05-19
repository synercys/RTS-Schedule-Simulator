#!/bin/bash

BIN_FOLDER="../../../out/bin"
TASKSET_FOLDER="/Users/jjs/Documents/myProject/ScheduLeak-Simulator/experiments/tasksets/rmSchedulable/coverage1to999"
OUTPUT_FOLDER="."

$BIN_FOLDER/rtsched -i $TASKSET_FOLDER/ -o $OUTPUT_FOLDER/rm_10lcm -d 10 -p rm -c  SLEAK_DURATION -v
$BIN_FOLDER/rtsched -i $TASKSET_FOLDER/ -o $OUTPUT_FOLDER/ts3_10lcm  -d 10 -p taskshuffler3 -c  SLEAK_DURATION -v
$BIN_FOLDER/rtsched -i $TASKSET_FOLDER/ -o $OUTPUT_FOLDER/ts4_10lcm  -d 10 -p taskshuffler4 -c  SLEAK_DURATION -v

$BIN_FOLDER/rtsched -i $TASKSET_FOLDER/ -o $OUTPUT_FOLDER/edf_10lcm -d 10 -p edf -c  SLEAK_DURATION -v
$BIN_FOLDER/rtsched -i $TASKSET_FOLDER/ -o $OUTPUT_FOLDER/ro3_10lcm  -d 10 -p reorder3 -c  SLEAK_DURATION -v
$BIN_FOLDER/rtsched -i $TASKSET_FOLDER/ -o $OUTPUT_FOLDER/ro4_10lcm  -d 10 -p reorder4 -c  SLEAK_DURATION -v