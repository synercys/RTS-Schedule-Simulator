#!/bin/bash

BIN_FOLDER="../../../../out/bin"
PLOT_SCRIPT_FOLDER="../../../plot_scripts/"
TASKSET="/Users/jjs/Documents/myProject/ScheduLeak-Simulator/experiments/tasksets/edfScheduLeak/coverage1to999"
DURATION="20000"
CASE="DURATION"
OUTPUT_FOLDER="."
SUFFIX="_DURATION"
FIG="png"

# $BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/edf_d$DURATION -d $DURATION -r 1000000 -v -p edf -e uapenmeanslot -c $CASE
# $BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/ro3_d$DURATION -d $DURATION -r 1000000 -v -p reorder3 -e uapenmeanslot -c $CASE
# $BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/lp10_d$DURATION -d $DURATION -r 1000000 -v -p laplace10 -e uapenmeanslot -c $CASE
# $BIN_FOLDER/rtenc -i $TASKSET -o $OUTPUT_FOLDER/lp1000_d$DURATION -d $DURATION -r 1000000 -v -p laplace1000 -e uapenmeanslot -c $CASE

python3 $PLOT_SCRIPT_FOLDER/Plot_LaplaceSched_MeanSlotEntropy.py \
-l "Vanilla EDF" -i edf_d$DURATION$SUFFIX.csv  \
-l "TaskShuffler EDF" -i ro3_d$DURATION$SUFFIX.csv  \
-l "$\epsilon\mathrm{-}Sched(10^3)$" -i lp1000_d$DURATION$SUFFIX.csv \
-l "$\epsilon\mathrm{-}Sched(10)$" -i lp10_d$DURATION$SUFFIX.csv \
-o average_slot_entropy_d$DURATION.$FIG
# -l "REORDER" -i reorder4-d$DURATION$SUFFIX.csv -o peak_count_d$DURATION.$FIG \