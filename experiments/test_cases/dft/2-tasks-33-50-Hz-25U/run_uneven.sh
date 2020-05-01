#!/bin/bash

BIN_FOLDER="../../../../out/bin"
PLOT_SCRIPT_FOLDER="../../../plot_scripts/"
TASKSET="2-task-33hz_50hz.tasksets"
DURATION="50"
FIG="png"

$BIN_FOLDER/rtdft -i $TASKSET -c STFT_SCHEDULEAK_VICTIM_CUMULATIVE_UNEVEN -d $DURATION -v -p rm -o rm-v-uneven-$DURATION
python3 $PLOT_SCRIPT_FOLDER/PlotSTFTUnevenSpectrum.py -i rm-v-uneven-$DURATION.rtstft -o rm-v-uneven-$DURATION.$FIG -r

$BIN_FOLDER/rtdft -i $TASKSET -c STFT_SCHEDULEAK_VICTIM_CUMULATIVE_UNEVEN -d $DURATION -v -p edf -o edf-v-uneven-$DURATION
python3 $PLOT_SCRIPT_FOLDER/PlotSTFTUnevenSpectrum.py -i edf-v-uneven-$DURATION.rtstft -o edf-v-uneven-$DURATION.$FIG -r

$BIN_FOLDER/rtdft -i $TASKSET -c STFT_SCHEDULEAK_VICTIM_CUMULATIVE_UNEVEN -d $DURATION -v -p taskshuffler -o ts-v-uneven-$DURATION
python3 $PLOT_SCRIPT_FOLDER/PlotSTFTUnevenSpectrum.py -i ts-v-uneven-$DURATION.rtstft -o ts-v-uneven-$DURATION.$FIG -r

$BIN_FOLDER/rtdft -i $TASKSET -c STFT_SCHEDULEAK_VICTIM_CUMULATIVE_UNEVEN -d $DURATION -v -p reorder -o ro-v-uneven-$DURATION
python3 $PLOT_SCRIPT_FOLDER/PlotSTFTUnevenSpectrum.py -i ro-v-uneven-$DURATION.rtstft -o ro-v-uneven-$DURATION.$FIG -r