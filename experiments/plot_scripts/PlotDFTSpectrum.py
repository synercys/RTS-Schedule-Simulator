import argparse
import json
from io import StringIO
import LogLoader
import numpy as np
import matplotlib.ticker as ticker
import matplotlib.pyplot as plt

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

    # args = vars(parser.parse_args("--in /Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/dft/test.rtdft -p".split()))
    args = vars(parser.parse_args())

    # Argument variables
    displayDetail = args["detail"]
    showPlot = args["plot"]
    inFileName = args["in"]
    outFileNames = args["out"] if args["out"] is not None else []

    # Load .rtdft json
    try:
        jsonRoot = json.load(open(inFileName, 'r'))
    except:
        print("Failed to load the rtdft file: {}".format(inFileName))
        exit()

    # Load task set
    taskSet = LogLoader.loadSingleTaskSetFromJsonObject(jsonRoot)
    print(taskSet.toString())

    # Load CSV
    data_dict_base = np.genfromtxt(StringIO(jsonRoot['data']['spectrumCSV']), delimiter=',', unpack=True, names=True)
    xFreq = data_dict_base['frequency']
    yMag = data_dict_base['magnitude']
    yPhase = data_dict_base['phase']

    ''' Plot display config '''
    if displayDetail:
        plt.rcParams['figure.figsize'] = 7, 4.5
    else:
        plt.rcParams['figure.figsize'] = 7, 2.5

        # suggested settings for paper format
        # plt.rcParams['figure.figsize'] = 6, 3

    plt.rcParams["font.family"] = "Arial"
    plt.rcParams['font.size'] = 15
    plt.rcParams['legend.fontsize'] = 13
    plt.rcParams['axes.titlesize'] = 15
    plt.rcParams['ytick.labelsize'] = 10
    plt.rcParams['xtick.labelsize'] = 10

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

    # X-axis Settings
    plt.xlabel('Frequency (Hz)')

    # Set X-axis limit based on the largest frequency of all tasks
    X_AXIS_FREQ_LIMIT = 100*(int(taskSet.getLargestFreq()/100)+1)

    X_AXIS_TICK_INTERVAL = 10
    #ax.set_xticks(major_ticks)

    ax.xaxis.set_major_locator(ticker.MultipleLocator(X_AXIS_TICK_INTERVAL))
    ax.set_xlim(0, X_AXIS_FREQ_LIMIT)


    # Y-axis Settings
    ax.set_ylabel(ylabel='Magnitude \n(Normalized)')

    yaxis_limit = 1 # normalized # maxAmplitude if not normalized
    ax.set_ylim([0, yaxis_limit])

    ax.yaxis.labelpad = 3
    ax.xaxis.labelpad = 3

    # get X and Y axis values (trimmed to the desired display range) to be displayed
    # X
    xFreq_limitIndx = findIndexOfFrequency(xFreq, X_AXIS_FREQ_LIMIT)
    displayedFreq = xFreq[:xFreq_limitIndx]
    # Y
    maxAmplitude = max(yMag[:xFreq_limitIndx])
    displayedAmplitude = [amp / maxAmplitude for amp in yMag[:xFreq_limitIndx]] # Normalized

    plt.plot(displayedFreq, displayedAmplitude, color='navy', alpha=0.75, linewidth=1.5)
    plt.grid(linestyle=':')

    '''Add ground truth lines'''
    if taskSet is not None:
        for task in taskSet.tasks:
            plt.plot([task.frequency, task.frequency], [0, yaxis_limit], color='maroon', alpha=0.8, linestyle='--', linewidth=1.5)
            ax.arrow(task.frequency, 1, 0, -0.05, head_width=3, head_length=0.03, color='maroon')

    if displayDetail:
        # FFT Phase Plot
        ax = plt.subplot(212)
        #ax.set_xticks(major_ticks)
        ax.xaxis.set_major_locator(ticker.MultipleLocator(X_AXIS_TICK_INTERVAL))
        plt.grid()
        plt.plot(xFreq[:xFreq_limitIndx], np.angle(yPhase[:xFreq_limitIndx]), color='blue', alpha=0.8, )

    plt.tight_layout()

    ''' Save the plot to files with the specified format '''
    for outFileName in outFileNames:
        outputFormat = outFileName.split('.')[-1]
        if outputFormat == "pdf" or outputFormat == "png":
            plt.savefig(outFileName, pad_inches=0.015, bbox_inches='tight')
            print('Save the plot to "{}".'.format(outFileName))

    # Output to console if output is not specified
    if len(outFileNames) == 0:
        for i in range(len(yMag)):
            if xFreq[i] < 0:
                break   # only print the top half part since the bottom half is symmetric
            print(str(xFreq[i]) + " " + str(abs(yMag[i])**2))

    if showPlot:
        plt.show()
