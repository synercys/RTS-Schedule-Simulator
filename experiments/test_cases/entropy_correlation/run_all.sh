#!/bin/bash

BIN_FOLDER="../../../out/bin"
TASKSET_FOLDER="../../tasksets/smallHP_rmSchedulable"
OUTPUT_FOLDER="."

$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_rm_shannon -r 1000000 -p taskshuffler -e shannon -c FULL_HP
$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_rm_apen -r 1000000 -p taskshuffler -e uapen -c FULL_HP
