#!/bin/bash

BIN_PLOT="../../plot_scripts/PlotContextSwitchIncreaseRatio.py"
OUTPUT_FOLDER="."

python3 $BIN_PLOT -i rm_10lcm_sleaklcm.csv -i ts3_10lcm_sleaklcm.csv -o $OUTPUT_FOLDER/contextSwitchRatioTaskShufflerToRm.pdf -o $OUTPUT_FOLDER/contextSwitchRatioTaskShufflerToRm.png