#!/bin/bash

BIN_PLOT="../../../plot_scripts/Plot_LaplaceSched_FlattenedLaplaceDistribution_Simulation.py"
OUTPUT_FOLDER="."

python3 $BIN_PLOT -i x -o flattened_distribution_by_sim.png -o flattened_distribution_by_sim.pdf
# python3 $BIN_PLOT -i x -o flattened_distribution_by_sim.png -o flattened_distribution_by_sim_dist_only.png