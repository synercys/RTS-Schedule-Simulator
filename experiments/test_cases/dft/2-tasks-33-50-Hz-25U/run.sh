#!/bin/bash

BIN_FOLDER="../../../../out/bin"
PLOT_SCRIPT_FOLDER="../../../plot_scripts/"
TASKSET="2-task-33hz_50hz.tasksets"
DURATION="100"
FIG="png"

$BIN_FOLDER/rtdft -i $TASKSET -c STFT_SCHEDULEAK_VICTIM_CUMULATIVE -d $DURATION -v -p rm -o rm-v-$DURATION
python3 $PLOT_SCRIPT_FOLDER/PlotSTFTSpectrum.py -i rm-v-$DURATION.rtstft -o rm-v-$DURATION.$FIG -r

$BIN_FOLDER/rtdft -i $TASKSET -c STFT_SCHEDULEAK_VICTIM_CUMULATIVE -d $DURATION -p edf -o edf-v-$DURATION
python3 $PLOT_SCRIPT_FOLDER/PlotSTFTSpectrum.py -i edf-v-$DURATION.rtstft -o edf-v-$DURATION.$FIG -r

$BIN_FOLDER/rtdft -i $TASKSET -c STFT_SCHEDULEAK_VICTIM_CUMULATIVE -d $DURATION -p taskshuffler -o ts-v-$DURATION
python3 $PLOT_SCRIPT_FOLDER/PlotSTFTSpectrum.py -i ts-v-$DURATION.rtstft -o ts-v-$DURATION.$FIG -r

$BIN_FOLDER/rtdft -i $TASKSET -c STFT_SCHEDULEAK_VICTIM_CUMULATIVE -d $DURATION -p reorder -o ro-v-$DURATION
python3 $PLOT_SCRIPT_FOLDER/PlotSTFTSpectrum.py -i ro-v-$DURATION.rtstft -o ro-v-$DURATION.$FIG -r