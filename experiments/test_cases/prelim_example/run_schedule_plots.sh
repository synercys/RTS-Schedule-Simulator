#!/bin/bash

BIN_FOLDER="../../../out/bin"
TASKSET_FOLDER="."
OUTPUT_FOLDER="./data"


$BIN_FOLDER/rtsim -i $TASKSET_FOLDER/3tasks_0offset.tasksets -l 30 -p RM -o $OUTPUT_FOLDER/rm.xlsx -d 180
$BIN_FOLDER/rtsim -i $TASKSET_FOLDER/3tasks_0offset.tasksets -l 30 -p TaskShuffler1 -o $OUTPUT_FOLDER/TaskShuffler1.xlsx -d 180
$BIN_FOLDER/rtsim -i $TASKSET_FOLDER/3tasks_0offset.tasksets -l 30 -p TaskShuffler2 -o $OUTPUT_FOLDER/TaskShuffler2.xlsx -d 180
$BIN_FOLDER/rtsim -i $TASKSET_FOLDER/3tasks_0offset.tasksets -l 30 -p TaskShuffler3 -o $OUTPUT_FOLDER/TaskShuffler3.xlsx -d 180
$BIN_FOLDER/rtsim -i $TASKSET_FOLDER/3tasks_0offset.tasksets -l 30 -p TaskShuffler4 -o $OUTPUT_FOLDER/TaskShuffler4.xlsx -d 180
