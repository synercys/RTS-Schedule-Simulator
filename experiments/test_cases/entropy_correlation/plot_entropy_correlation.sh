#!/bin/bash

BIN_PLOT="../../plot_scripts/PlotCaseEntropyCorrelation.py"
OUTPUT_FOLDER="."

python $BIN_PLOT -i correlation_taskshuffler_shannon_full_hyperperiod.csv -i correlation_taskshuffler_uapen_full_hyperperiod.csv -o $OUTPUT_FOLDER/entropy_correlation
