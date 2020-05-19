import argparse
import json
from io import StringIO
import LogLoader
import numpy as np
import matplotlib.ticker as ticker
import matplotlib.pyplot as plt
import PlotUtility


# This plot takes two CSV files as input: 1. RM, 2. TaskShuffler
def plot_execution_range_ratio(show_plot, in_csv_file_list, out_plot_file_list, label_list):

    ''' general plot configurations '''
    PlotUtility.config_plot(plt)
    plt.rcParams['figure.figsize'] = 8,5.5

    # change font to Arial
    plt.rcParams["font.family"] = "Arial"
    plt.rcParams['font.size'] = 22
    plt.rcParams['legend.fontsize'] = 13
    plt.rcParams['axes.titlesize'] = 15
    plt.rcParams['ytick.labelsize'] = 20
    plt.rcParams['xtick.labelsize'] = 20
    plt.rcParams["legend.edgecolor"] = 'black'

    plot_labels = [] if label_list is None else label_list

    ''' Data '''
    data_dict1 = np.genfromtxt(in_csv_file_list[0], delimiter=',', unpack=True, names=True)
    data_dict2 = np.genfromtxt(in_csv_file_list[1], delimiter=',', unpack=True, names=True)
    x = data_dict1['Utilization']
    y1 = data_dict1['Mean_Execution_Range_Ratio_To_Period']
    y2 = data_dict2['Mean_Execution_Range_Ratio_To_Period']


    # plot general config
    # plt.title('Interesting Graph\nCheck it out')
    # plt.legend()
    # plt.grid(True, 'major', 'both', color='0.8', linestyle='--', linewidth=1)

    # x axis config
    plt.xlabel('Task Set Utilization')
    plt.xlim(0, 1)
    plt.xticks([i/10 for i in range(0, 11, 1)])

    # y axis config
    plt.ylabel('Mean Ratio of Task \n Execution Range to Period')
    # ylimMax = (max(y)/5+1)*5
    # plt.ylim([0, ylimMax])
    # plt.yticks([i for i in range(0, int(ylimMax)+1, 2)])
    plt.ylim(0, 1.1)
    plt.yticks([i/10 for i in range(1, 11, 1)])

    # plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    # plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)


    # minor_ticks = [tmp / 10 for tmp in range(0, 11)]
    # plt.axes().set_yticks(minor_ticks, minor=True)

    # data points
    markerSize = 30
    plt.scatter(x, y1, marker='+', color='grey', alpha=0.5, s=[markerSize for i in range(len(y1))], label=plot_labels[0])
    plt.scatter(x, y2, marker='x', color='DodgerBlue', alpha=0.5, s=[markerSize for i in range(len(y2))], label=plot_labels[1])
    # plt.scatter(x, y1, marker='o', facecolors='none', color='grey', alpha=0.5, s=[markerSize for i in range(len(y1))], label=plot_labels[0])
    # plt.scatter(x, y2, marker='o', facecolors='none', color='DodgerBlue', alpha=0.5, s=[markerSize for i in range(len(y2))], label=plot_labels[1])


    # Post plot configurations
    legend = plt.legend(shadow=False, loc='upper right')
    legend.get_frame().set_edgecolor('grey')

    ''' Save the plot to files with the specified format '''
    for outFileName in out_plot_file_list:
        print("Exporting the plot ...")

        # if outFileName == "png" or outFileName == "pdf":
        #     outFileName = "{}.{}".format(inFileName.split('.')[0], outFileName)

        outputFormat = outFileName.split('.')[-1]
        if outputFormat == "pdf" or outputFormat == "png" or True:
            print('Saving the plot to "{}" ...'.format(outFileName), end=" ")
            plt.savefig(outFileName, pad_inches=0.02, bbox_inches='tight')
            print("Done")


    if show_plot:
        plt.show()


if __name__ == '__main__':

    # out_plot_file_list = [""]
    # csv_file_list = [
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/for_sok_paper/rm_10lcm_sleaklcm.csv',
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/for_sok_paper/ts3_10lcm_sleaklcm.csv'
    # ]
    # label_list = ["RM", "TaskShuffler"]
    # show_plot = True

    show_plot, csv_file_list, out_plot_file_list, label_list = PlotUtility.parse_arguments()

    plot_execution_range_ratio(show_plot, csv_file_list, out_plot_file_list, label_list)