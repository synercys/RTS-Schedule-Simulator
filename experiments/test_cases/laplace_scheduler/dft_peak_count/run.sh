#!/bin/bash

BIN_FOLDER="../../../../out/bin"
PLOT_SCRIPT_FOLDER="../../../plot_scripts/"
TASKSET="/Users/jjs/Documents/myProject/ScheduLeak-Simulator/experiments/tasksets/edfScheduLeak/coverage1to999_200ms"
DURATION="50000"
FIG="png"
CASE="DFT_DURATION"
SUFFIX="_dftDuration"

# $BIN_FOLDER/rtdft -i $TASKSET -c $CASE -d $DURATION -v -p edf -o edf-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p reorder4 -o reorder4-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p reorder3 -o reorder3-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace10000 -o laplace-e10000-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace1000 -o laplace-e1000-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace100 -o laplace-e100-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace10 -o laplace-e10-d$DURATION
# $BIN_FOLDER/rtdft -i $TASKSET -c  $CASE -d $DURATION -v -p laplace1 -o laplace-e1-d$DURATION


# python3 $PLOT_SCRIPT_FOLDER/Plot_LaplaceSched_PeakCount_DeadlineMiss.py \
# python3 $PLOT_SCRIPT_FOLDER/Plot_LaplaceSched_PeakCount.py \
# -l "Vanilla EDF" -i edf-d$DURATION$SUFFIX.csv  \
# -l "REORDER" -i reorder3-d$DURATION$SUFFIX.csv \
# -l "$\epsilon\mathrm{-}Sched(10^3)$" -i laplace-e1000-d$DURATION$SUFFIX.csv \
# -l "$\epsilon\mathrm{-}Sched(10)$" -i laplace-e10-d$DURATION$SUFFIX.csv \
# -o peak_count_d$DURATION.png

# python3 $PLOT_SCRIPT_FOLDER/Plot_LaplaceSched_FrequencyPerformance.py \
# python3 $PLOT_SCRIPT_FOLDER/Plot_LaplaceSched_FrequencyPerformance_DeadlineMiss.py \
# -l "$\epsilon\mathrm{-}Sched(10^3)$" -i laplace-e1000-d$DURATION$SUFFIX.csv \
# -l "$\epsilon\mathrm{-}Sched(10)$" -i laplace-e10-d$DURATION$SUFFIX.csv \
# -o frequencyErrorRatio_d$DURATION.$FIG

# python3 $PLOT_SCRIPT_FOLDER/Plot_LaplaceSched_UnderPerformance_Mean.py \
# -l "$\epsilon\mathrm{-}Sched(10^3)$" -i laplace-e1000-d$DURATION$SUFFIX.csv \
# -l "$\epsilon\mathrm{-}Sched(10)$" -i laplace-e10-d$DURATION$SUFFIX.csv \
# -o underPerformanceRatio_d$DURATION.$FIG

python3 $PLOT_SCRIPT_FOLDER/Plot_LaplaceSched_UnderPerformance_Worst.py \
-l "$\epsilon\mathrm{-}Sched(10^3)$" -i laplace-e1000-d$DURATION$SUFFIX.csv \
-l "$\epsilon\mathrm{-}Sched(10)$" -i laplace-e10-d$DURATION$SUFFIX.csv \
-o underPerformanceRatioWorst_d$DURATION.$FIG
