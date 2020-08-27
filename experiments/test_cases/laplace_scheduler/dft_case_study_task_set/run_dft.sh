#!/bin/bash

BIN_FOLDER="../../../../out/bin"
PLOT_SCRIPT_FOLDER="../../../plot_scripts/"
TASKSET="/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/tasksets/rtas15_aGeneralizedModel_caseStudy.tasksets"
DURATION="50000"
FIG="png"

## declare an array variable
declare -a sched=("edf" "reorder4" "taskshuffler4" "laplace10" "laplace1000")
declare -a schedshort=("edf" "ro4" "ts4" "lp10" "lp1000")

# get length of an array
arraylength=${#sched[@]}

# use for loop to read all values and indexes
for (( i=1; i<${arraylength}+1; i++ ));
do
  $BIN_FOLDER/rtdft -i $TASKSET -d $DURATION -v -p ${sched[$i-1]} -o ${schedshort[$i-1]}-v-d$DURATION-dft
  python3 $PLOT_SCRIPT_FOLDER/PlotDFTSpectrum.py -i ${schedshort[$i-1]}-v-d$DURATION-dft.rtdft -o $FIG 
done
