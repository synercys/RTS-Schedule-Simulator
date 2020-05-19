#!/bin/bash

BIN_PLOT="../../plot_scripts/PlotExecutionRangeRatio.py"
OUTPUT_FOLDER="."

python3 $BIN_PLOT -i rm_10lcm_sleaklcm.csv -l "RM" -i ts3_10lcm_sleaklcm.csv -l "TaskShuffler" -o $OUTPUT_FOLDER/executionRangeRmVsTaskShuffler.pdf -o $OUTPUT_FOLDER/executionRangeRmVsTaskShuffler.png