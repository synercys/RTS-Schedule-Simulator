import argparse
import json
import pandas as pd
from io import StringIO
import LogLoader
import matplotlib.ticker as ticker
import matplotlib.pyplot as plt
import seaborn as sns


def findIndexOfFrequency(freqList, freqTarget):
    indexCount = 0
    for freq in freqList:
        if float(freq) >= freqTarget:
            return indexCount
        indexCount += 1
    return indexCount

if __name__ == '__main__':
    ''' Command line arguments '''
    parser = argparse.ArgumentParser()
    #parser.add_argument('--example', nargs='?', const=1, type=int, help="")
    parser.add_argument('-i', "--in", required=True, help="A schedule DFT analysis report file.")
    parser.add_argument('-o', "--out", required=False, nargs="*", help="Output file name and format determined by its extension name.")
    parser.add_argument('-p', "--plot", action='store_true', help="Display the resulting DFT plot.")

    args = vars(parser.parse_args())
    # args = vars(parser.parse_args("--in /Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/dft/a5v.rtstft -p".split()))

    # Argument variables
    showPlot = args["plot"]
    inFileName = args["in"]
    outFileNames = args["out"] if args["out"] is not None else []

    # Load .rtstft json
    try:
        jsonRoot = json.load(open(inFileName, 'r'))
    except:
        print("Failed to load the rtstft file: {}".format(inFileName))
        exit()

    # Load task set
    taskSet = LogLoader.loadSingleTaskSetFromJsonObject(jsonRoot)
    print(taskSet.toString())

    # Time unit
    nsPerTick = jsonRoot['data']['tickUnitInNs']


    # plt.rcParams['figure.figsize'] = 7, 2.5
    plt.rcParams['figure.figsize'] = 6, 3     # suggested settings for paper format

    plt.rcParams["font.family"] = "Arial"
    plt.rcParams['font.size'] = 15
    plt.rcParams['legend.fontsize'] = 13
    plt.rcParams['axes.titlesize'] = 15
    plt.rcParams['ytick.labelsize'] = 10
    plt.rcParams['xtick.labelsize'] = 10

    # plt.rcParams['axes.linewidth'] = 1
    # plt.rcParams['axes.edgecolor'] = "0.8"

    # suggested settings for paper format
    # plt.rcParams['font.size'] = 19
    # plt.rcParams['legend.fontsize'] = 19
    # plt.rcParams['axes.titlesize'] = 19
    # plt.rcParams['ytick.labelsize'] = 15
    # plt.rcParams['xtick.labelsize'] = 15

    ''' Load CSV data to Pandas data frame '''
    df = pd.read_csv(StringIO(jsonRoot['data']['spectrumCSV']), skiprows=0, header=0, index_col=0).T#, usecols=range(0, 10))
    df = (df-df.min())/(df.max()-df.min())  # min-max normalization
    # df.index.values are frequencies
    # df.columns.values are timestamps

    # draw the heatmap
    ax = sns.heatmap(df, cmap='Blues', cbar_kws={'label': 'Magnitude\n(Normalized)'})


    ''' X-axis Settings '''
    # print(df.column.values)
    plt.xlabel('Time ($LCM(T_o, T_v)$)')
    ax.set_xticklabels([x*0.5 for x in range(1, len(df.columns.values)+1)])
    ''' End of X-axis Settings '''


    ''' Y-axis Settings '''
    Y_AXIS_TICK_INTERVAL = 10

    # Set Y-axis limit based on the largest frequency of all tasks
    freq_upper_limit = 100 * (int(taskSet.getLargestFreq() / 100) + 1)
    idx_of_freq_upper_limit = findIndexOfFrequency(df.index.values, freq_upper_limit)
    # print(df.index.values[idx_of_freq_upper_limit])
    ax.set_ylim(-1, idx_of_freq_upper_limit+1)    # start with -1 since index=0 (freq=0) was removed beforehand

    # ax.yaxis.labelpad = 3
    # ax.xaxis.labelpad = 3

    # Create a layer for Y-axis ticks and labels
    ax2 = ax.twinx()
    ax2.set_ylabel(ylabel='Frequency (Hz)')
    ax2.yaxis.set_major_locator(ticker.MultipleLocator(Y_AXIS_TICK_INTERVAL))
    ax2.set_ylim(-0.5, float(df.index.values[idx_of_freq_upper_limit])+0.5)
    ax2.yaxis.tick_left()   # the same as ax2.yaxis.set_ticks_position('left')
    ax2.yaxis.set_label_position("left")

    # ax2.set_frame_on(True)
    # for spine in ax2.spines.values():
        # spine.set_edgecolor('green')
        # spine.set_linewidth(0.5)
        # spine.set_visible(True)

    # Disable Y-axis ticks and labels
    # ax.yaxis.set_ticks_position('none')
    ax.tick_params(top=False, bottom=True, left=False, right=False, labelleft=False, labelbottom=True)
    ''' End of Y-axis Settings '''


    '''Add ground truth lines'''
    # if taskSet is not None:
    #     for task in taskSet.tasks:
    #         plt.plot([task.frequency, task.frequency], [0, yaxis_limit], color='maroon', alpha=0.8, linestyle='--', linewidth=1.5)
    #         ax.arrow(task.frequency, 1, 0, -0.05, head_width=3, head_length=0.03, color='maroon')

    plt.tight_layout()

    ''' Save the plot to files with the specified format '''
    for outFileName in outFileNames:
        outputFormat = outFileName.split('.')[-1]
        if outputFormat == "pdf" or outputFormat == "png":
            plt.savefig(outFileName, pad_inches=0.015, bbox_inches='tight')
            print('Save the plot to "{}".'.format(outFileName))

    # Output to console if output is not specified
    # if len(outFileNames) == 0:
    #     for i in range(len(yMag)):
    #         if xFreq[i] < 0:
    #             break   # only print the top half part since the bottom half is symmetric
    #         print(str(xFreq[i]) + " " + str(abs(yMag[i])**2))

    if showPlot:
        plt.show()
