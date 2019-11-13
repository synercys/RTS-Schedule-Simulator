#!/bin/bash

BIN_FOLDER="../../../out/bin"
TASKSET_SETTING_FOLDER="."
OUTPUT_FOLDER="."

$BIN_FOLDER/rttaskgen -s -i $TASKSET_SETTING_FOLDER/smallHP.rttaskgen -o $OUTPUT_FOLDER/smallHP
#$BIN_FOLDER/rttaskgen -s -i $TASKSET_SETTING_FOLDER/smallHP_3.rttaskgen -o $OUTPUT_FOLDER/smallHP_3
#$BIN_FOLDER/rttaskgen -s -i $TASKSET_SETTING_FOLDER/smallHP_4.rttaskgen -o $OUTPUT_FOLDER/smallHP_4