#!/bin/bash

BIN_FOLDER="../../../out/bin"
TASKSET_FOLDER="/Users/jjs/Documents/myProject/ScheduLeak-Simulator/experiments/tasksets/edfScheduLeak/backup_tasksests_schedulable_by_rm/coverage1to999"
OUTPUT_FOLDER="./raw"
ROUNDS=10000
NUM_OF_TASKS="13"

mkdir -p $OUTPUT_FOLDER

for i in {0..9}
do
    $BIN_FOLDER/rtenc -i $TASKSET_FOLDER/"$NUM_OF_TASKS"_"$i".tasksets -o $OUTPUT_FOLDER/"$NUM_OF_TASKS"_"$i"_taskshuffler_shannon -r $ROUNDS -p taskshuffler -e shannon -c LCM -v -n 10
done
