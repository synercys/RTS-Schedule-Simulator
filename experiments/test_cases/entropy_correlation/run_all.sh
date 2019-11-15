#!/bin/bash

BIN_FOLDER="../../../out/bin"
TASKSET_FOLDER="../../tasksets/smallHP_rmSchedulable"
OUTPUT_FOLDER="."

#$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_shannon -r 1000000 -p taskshuffler -e shannon -c FULL_HP
#$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_uapen -r 1000000 -p taskshuffler -e uapen -c FULL_HP
$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_apen -r 10000 -p taskshuffler -e apen -c FULL_HP
