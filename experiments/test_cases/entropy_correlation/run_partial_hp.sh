#!/bin/bash

BIN_FOLDER="../../../out/bin"
TASKSET_FOLDER="../../tasksets/smallHP_rmSchedulable"
OUTPUT_FOLDER="."

$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_shannon_partial_025 -r 1000000 -p taskshuffler -e shannon -c PARTIAL_HP_025
$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_shannon_partial_050 -r 1000000 -p taskshuffler -e shannon -c PARTIAL_HP_050
$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_shannon_partial_075 -r 1000000 -p taskshuffler -e shannon -c PARTIAL_HP_075

$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_uapen_partial_025 -r 1000000 -p taskshuffler -e uapen -c PARTIAL_HP_025
$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_uapen_partial_050 -r 1000000 -p taskshuffler -e uapen -c PARTIAL_HP_050
$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_uapen_partial_075 -r 1000000 -p taskshuffler -e uapen -c PARTIAL_HP_075

#$BIN_FOLDER/rtenc -i $TASKSET_FOLDER/smallHP.tasksets -o $OUTPUT_FOLDER/correlation_taskshuffler_apen -r 10000 -p taskshuffler -e apen -c PARTIAL_HP
