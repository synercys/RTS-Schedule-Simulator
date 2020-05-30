#!/bin/bash

BIN_FOLDER="../../../../out/bin"
PLOT_SCRIPT_FOLDER="../../../plot_scripts/"
TASKSET="/Users/jjs/Documents/myProject/ScheduLeak-Simulator/experiments/tasksets/edfScheduLeak/coverage1to999"
DURATION="20000"
FIG="png"
CASE="DFT_DURATION"
SUFFIX="_dftDuration"

# $BIN_FOLDER/rtdft -i $TASKSET -c $CASE -d $DURATION -v -p edf -o edf-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p reorder4 -o reorder4-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p reorder3 -o reorder3-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace1000 -o laplace-e1000-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace100 -o laplace-e100-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace10 -o laplace-e10-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace1 -o laplace-e1-d$DURATION

python3 $PLOT_SCRIPT_FOLDER/Plot_LaplaceSched_PeakCount.py \
-l "EDF" -i edf-d$DURATION$SUFFIX.csv  \
-l "$\epsilon-Scheduler=1000$" -i laplace-e1000-d$DURATION$SUFFIX.csv \
-l "$\epsilon-Scheduler=10$" -i laplace-e10-d$DURATION$SUFFIX.csv 
# -l "REORDER" -i reorder4-d$DURATION$SUFFIX.csv -o peak_count_d$DURATION.$FIG \

