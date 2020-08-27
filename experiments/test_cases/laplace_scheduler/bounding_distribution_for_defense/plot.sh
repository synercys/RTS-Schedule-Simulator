#!/bin/bash

BIN_PLOT="../../../plot_scripts"
OUTPUT_FOLDER="."

python3 $BIN_PLOT/Plot_LaplaceSched_LaplaceDistribution_Bounding_Simulation.py -i x -o bounding_laplace_distribution.png 
python3 $BIN_PLOT/Plot_LaplaceSched_LaplaceDistribution_Truncation_Simulation.py -i x -o truncating_laplace_distribution.png 