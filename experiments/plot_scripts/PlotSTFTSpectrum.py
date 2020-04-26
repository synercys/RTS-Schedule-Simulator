import argparse
import json
import pandas as pd
from io import StringIO
import LogLoader
import numpy as np
import matplotlib.ticker as ticker
import matplotlib.pyplot as plt
import seaborn as sns


def findIndexOfFrequency(freqList, freqTarget):
    indexCount = 0
    for freq in freqList:
        if freq >= freqTarget:
            return indexCount
        indexCount += 1
    return indexCount

if __name__ == '__main__':
    ''' Command line arguments '''
    parser = argparse.ArgumentParser()
    #parser.add_argument('--example', nargs='?', const=1, type=int, help="")
    parser.add_argument('-i', "--in", required=True, help="A schedule DFT analysis report file.")
    # parser.add_argument('-t', "--taskset", required=False, help="A taskset file for the importing DFT analysis result.")
    parser.add_argument('-o', "--out", required=False, nargs="*", help="Output file name and format determined by its extension name.")
    parser.add_argument('-p', "--plot", action='store_true', help="Display the resulting DFT plot.")
    parser.add_argument('-d', "--detail", action='store_true', help="Display all three types of plots.")


    args = vars(parser.parse_args())
    # args = vars(parser.parse_args("--in /Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/dft/aaa_.rtstft -p".split()))

    # Argument variables
    displayDetail = args["detail"]
    showPlot = args["plot"]
    inFileName = args["in"]
    outFileNames = args["out"] if args["out"] is not None else []

    # Load .rtdft json
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


    ''' Plot display config '''
    if displayDetail:
        plt.rcParams['figure.figsize'] = 7, 4.5
    else:
        plt.rcParams['figure.figsize'] = 7, 2.5

        # suggested settings for paper format
        plt.rcParams['figure.figsize'] = 6, 3

    plt.rcParams["font.family"] = "Arial"
    plt.rcParams['font.size'] = 15
    plt.rcParams['legend.fontsize'] = 13
    plt.rcParams['axes.titlesize'] = 15
    plt.rcParams['ytick.labelsize'] = 10
    plt.rcParams['xtick.labelsize'] = 10

    plt.rcParams['axes.linewidth'] = 1.25
    # plt.rcParams['axes.edgecolor'] = "0.8"

    # suggested settings for paper format
    # plt.rcParams['font.size'] = 19
    # plt.rcParams['legend.fontsize'] = 19
    # plt.rcParams['axes.titlesize'] = 19
    # plt.rcParams['ytick.labelsize'] = 15
    # plt.rcParams['xtick.labelsize'] = 15


    ''' Frequency Magnitude Spectrum Plot '''
    if displayDetail:
        ax = plt.subplot(211)
    else:
        ax = plt.subplot(111)


    df = pd.read_csv(StringIO(jsonRoot['data']['spectrumCSV']), skiprows=0, header=0, index_col=0).T#, usecols=range(0, 10))
    df = (df-df.min())/(df.max()-df.min())  # min-max normalization
    sns.heatmap(df, cmap='Blues', cbar_kws={'label': 'Magnitude\n(Normalized)'})




    ''' X-axis Settings '''
    # print(df.column.values)
    plt.xlabel('Time ($LCM(T_o, T_v)$)')
    ax.set_xticklabels([x*0.5 for x in range(1, len(df.columns.values)+1)])




    ''' Y-axis Settings '''
    Y_AXIS_TICK_INTERVAL = 10

    # Set Y-axis limit based on the largest frequency of all tasks
    freq_upper_limit = 100 * (int(taskSet.getLargestFreq() / 100) + 1)
    # freq_upper_limit = 1000

    ax.set_ylabel(ylabel='Frequency (Hz)')
    ax.yaxis.set_major_locator(ticker.MultipleLocator(Y_AXIS_TICK_INTERVAL))
    ax.set_ylim(0, freq_upper_limit)
    ax.set_yticklabels([y*10 for y in range(-1, int(freq_upper_limit/10)+1)])





    # yaxis_limit = 1 # normalized # maxAmplitude if not normalized
    # ax.set_ylim([0, yaxis_limit])

    ax.yaxis.labelpad = 3
    ax.xaxis.labelpad = 3

    # get X and Y axis values (trimmed to the desired display range) to be displayed
    # X
    # xFreq_limitIndx = findIndexOfFrequency(xFreq, X_AXIS_FREQ_LIMIT)
    # displayedFreq = xFreq[:xFreq_limitIndx]
    # Y
    # maxAmplitude = max(yMag[:xFreq_limitIndx])
    # displayedAmplitude = [amp / maxAmplitude for amp in yMag[:xFreq_limitIndx]] # Normalized


    ###

    # Load CSV
    # data_dict_base = np.genfromtxt(StringIO(jsonRoot['data']['spectrumCSV']), delimiter=',')
    # data_dict_base = np.genfromtxt("/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/dft/abc2.csv", delimiter=',')
    # df = pd.read_csv("/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/dft/abc.csv", skiprows=0, header=0, index_col=0, usecols=range(0, 100)).T#, usecols=range(0, 10))
    # df = pd.read_csv(StringIO(jsonRoot['data']['spectrumCSV']), skiprows=0, header=0, index_col=0, usecols=range(0, 100)).T#, usecols=range(0, 10))
    # print(df)


    # freqs = df[0][1:]
    # df.drop(df.index[0], inplace=True)
    # df = df.T
    # times = df
    # print(df)
    # df = df.drop(df.columns[0], axis=1).T

    # plt.figure(figsize=(10, 5))
    # fig, ax = plt.subplots()
    # ax.set_xticklabels(x_labels)
    # ax.set_yticklabels(list(y_labels))


    # heatmap_data = np.zeros((6, 10))
    # sns.heatmap(df, ax=ax, cmap='Blues')
    # sns.heatmap(df, cmap='Blues', cbar_kws={'label': 'Inference Precision $E_v^o$'})

    # ax.set_xticklabels([x*nsPerTick/1000_000_000 for x in x_labels])
    # ax.set_yticklabels(list(y_labels))

    # plt.show()
    # exit(1)

    ###



    # plt.plot(displayedFreq, displayedAmplitude, color='navy', alpha=0.75, linewidth=1.5)
    # plt.grid(linestyle=':')
    # ax.spines['bottom'].set_color('0.5')
    # ax.spines['bottom'].set_edgecolor('green')
    for spine in ax.spines.values():
        # spine.set_edgecolor('green')
        spine.set_linewidth(0.5)
        spine.set_visible(True)
    # plt.gcf().set_facecolor('green')

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
