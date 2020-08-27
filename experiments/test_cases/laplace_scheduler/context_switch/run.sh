#!/bin/bash

BIN_FOLDER="../../../../out/bin"
BIN_PLOT="../../../plot_scripts/Plot_LaplaceSched_ContextSwitchIncreaseRatio.py"
TASKSET="/Users/jjs/Documents/myProject/ScheduLeak-Simulator/experiments/tasksets/edfScheduLeak/coverage1to999_200ms"
DURATION="50000"
SUFFIX="_duration"
DATA_OUTPUT_FOLDER="./data"
PLOT_OUTPUT_FOLDER="./data"

# $BIN_FOLDER/rtsched -i $TASKSET -o $DATA_OUTPUT_FOLDER/edf_d$DURATION -d $DURATION -v -p edf -c  DURATION
# $BIN_FOLDER/rtsched -i $TASKSET -o $DATA_OUTPUT_FOLDER/ro3_d$DURATION -d $DURATION -v -p reorder3 -c  DURATION
# $BIN_FOLDER/rtsched -i $TASKSET -o $DATA_OUTPUT_FOLDER/ro4_d$DURATION -d $DURATION -v -p reorder4 -c  DURATION
# $BIN_FOLDER/rtsched -i $TASKSET -o $DATA_OUTPUT_FOLDER/ts3_d$DURATION -d $DURATION -v -p taskshuffler3 -c  DURATION
# $BIN_FOLDER/rtsched -i $TASKSET -o $DATA_OUTPUT_FOLDER/lp10_d$DURATION -d $DURATION -v -p laplace10 -c  DURATION
# $BIN_FOLDER/rtsched -i $TASKSET -o $DATA_OUTPUT_FOLDER/lp1000_d$DURATION -d $DURATION -v -p laplace1000 -c  DURATION

python3 $BIN_PLOT -i $DATA_OUTPUT_FOLDER/edf_d$DURATION$SUFFIX.csv -l "TaskShuffler EDF" -i $DATA_OUTPUT_FOLDER/ro3_d$DURATION$SUFFIX.csv -l "$\epsilon-Sched(10^3)$" -i $DATA_OUTPUT_FOLDER/lp1000_d$DURATION$SUFFIX.csv -l "$\epsilon-Sched(10)$" -i $DATA_OUTPUT_FOLDER/lp10_d$DURATION$SUFFIX.csv -o $PLOT_OUTPUT_FOLDER/contextSwitchDiffRatio.png