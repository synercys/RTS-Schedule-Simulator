import argparse
import json
import pandas as pd
from io import StringIO
from matplotlib import gridspec
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
    parser.add_argument('-r', "--ranking", action='store_true', help="Display frequency ranking(s).")

    args = vars(parser.parse_args())
    # args = vars(parser.parse_args("--in /Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/dft/b2.rtstft -p".split()))
    # args = vars(parser.parse_args("--in /Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/dft/1-task-50Hz/ts-v-100.rtstft -p -r".split()))

    # Argument variables
    displayFreqRanking = args["ranking"]
    showPlot = args["plot"]
    inFileName = args["in"]
    outFileNames = args["out"] if args["out"] is not None else []

    # Load .rtstft json
    print("Parsing the json file ...", end =" ")
    try:
        jsonRoot = json.load(open(inFileName, 'r'))
        print("Done")
    except:
        print("Failed to load the rtstft file: {}".format(inFileName))
        exit()

    # Load task set
    print("Loading the task set ...", end =" ")
    taskSet = LogLoader.loadSingleTaskSetFromJsonObject(jsonRoot)
    print("Done")
    print(taskSet.toString())

    # Time unit
    nsPerTick = jsonRoot['data']['tickUnitInNs']
    tickToMsMultiplier = nsPerTick/1000_000_000

    # Load frequency ranking data if required
    if displayFreqRanking:
        taskRankingsObject = jsonRoot['data']['taskFreqRanking']


    ''' Figure global configurations '''
    plt.rcParams["font.family"] = "Arial"
    plt.rcParams['font.size'] = 15
    plt.rcParams['legend.fontsize'] = 11
    plt.rcParams['axes.titlesize'] = 15
    plt.rcParams['ytick.labelsize'] = 11
    plt.rcParams['xtick.labelsize'] = 11

    plt.rcParams['axes.linewidth'] = 0.5
    # plt.rcParams['axes.edgecolor'] = "0.8"

    # suggested settings for paper format
    # plt.rcParams['font.size'] = 19
    # plt.rcParams['legend.fontsize'] = 19
    # plt.rcParams['axes.titlesize'] = 19
    # plt.rcParams['ytick.labelsize'] = 15
    # plt.rcParams['xtick.labelsize'] = 15

    if displayFreqRanking:
        plt.rcParams['figure.figsize'] = 6, 4  # suggested settings for paper format
        # ax = plt.subplot(212)
        # fig, (axd, ax) = plt.subplots(2)
        # plt.subplots_adjust(hspace=0, wspace=0)

        AX = gridspec.GridSpec(3, 20)
        AX.update(wspace=0.3, hspace=0.1)

        axr = plt.subplot(AX[0, :17])
        ax = plt.subplot(AX[1:, :17])
        ax_cbar = plt.subplot(AX[1:, 18])
        ax_arrow = plt.subplot(AX[1:, 17])


        # ax = plt.subplot2grid((3, 20), (1, 0), rowspan=2, colspan=18)
        # ax_cbar = plt.subplot2grid((3, 20), (1, 18), rowspan=2, colspan=2)
        # axr = plt.subplot2grid((3, 20), (0, 0), rowspan=1, colspan=18)
        # plt.subplots_adjust(hspace=0, wspace=0)


    else:
        # plt.rcParams['figure.figsize'] = 7, 2.5
        plt.rcParams['figure.figsize'] = 6, 3  # suggested settings for paper format
        # fig, ax = plt.subplots()
        # ax_cbar=None

        AX = gridspec.GridSpec(2, 19)
        AX.update(wspace=1, hspace=0.1)

        ax = plt.subplot(AX[0:, :17])
        ax_cbar = plt.subplot(AX[0:, 17])



    ''' Load CSV data to Pandas data frame '''
    print("Parsing CSV data to data frame ...", end =" ")
    df = pd.read_csv(StringIO(jsonRoot['data']['spectrumCSV']), skiprows=0, header=0, index_col=0).T#, usecols=range(0, 10))
    # df.index.values are frequencies
    # df.columns.values are timestamps

    # Global normalization
    # df_max = df.max().max() # get max of (max of each column)
    # df_min = df.min().min()
    # df = (df-df_min)/(df_max-df_min)  # min-max normalization (globally

    # Normalization in each column separately
    df = (df-df.min())/(df.max()-df.min())  # min-max normalization
    print("Done")


    # draw the heatmap
    print("Plotting the heatmap ....", end =" ")
    sns.heatmap(df, cmap='Blues', cbar_kws={'label': 'Magnitude', 'aspect': 0.01}, ax=ax, cbar_ax=ax_cbar)
    ax_cbar.set_frame_on(True)
    print("Done")

    # ax_cbar.grid()


    ''' X-axis Settings '''
    print("Setting X-axis ...", end=" ")
    ax.set_xlabel(xlabel='')
    plt.xlabel('')
    # print(df.column.values)
    # plt.xlabel('Time ($LCM(T_o, T_v)$)')
    # ax.set_xticklabels([x*0.5 for x in range(1, len(df.columns.values)+1)])
    print("Done")
    ''' End of X-axis Settings '''


    ''' ax Y-axis Settings '''
    print("Setting Y-axis ...", end=" ")
    Y_AXIS_TICK_INTERVAL = 10

    # Set Y-axis limit based on the largest frequency of all tasks
    freq_upper_limit = 100 * (int(taskSet.getLargestFreq() / 100) + 1)
    idx_of_freq_upper_limit = findIndexOfFrequency(df.index.values, freq_upper_limit)
    # print(df.index.values[idx_of_freq_upper_limit])
    ax.set_ylim(-1, idx_of_freq_upper_limit+1)    # start with -1 since index=0 (freq=0) was removed beforehand

    # Disable Y-axis ticks and labels
    # ax.yaxis.set_ticks_position('none')
    ax.tick_params(top=False, bottom=False, left=False, right=False, labelleft=False, labelbottom=False)

    print("Done")
    ''' End of ax Y-axis Settings '''



    print("Setting 2nd-layer Y-axis ...", end=" ")
    # Create a layer for Y-axis ticks and labels
    ax2 = ax.twinx()
    # ax2.set_xlabel(xlabel="")
    ax2.set_ylabel(ylabel='Frequency (Hz)')
    ax2.yaxis.set_major_locator(ticker.MultipleLocator(Y_AXIS_TICK_INTERVAL))
    ax2.set_ylim(-0.5, float(df.index.values[idx_of_freq_upper_limit])+0.5)
    ax2.yaxis.tick_left()   # the same as ax2.yaxis.set_ticks_position('left')
    ax2.yaxis.set_label_position("left")
    ax2.tick_params(top=False, bottom=False, left=True, right=True, labelleft=True, labelbottom=False, labeltop=False)
    # print(ax2.yaxis.get_majorticklabels()[1])
    # ax2.yaxis.get_majorticklabels()[5].set_y(100)
    # ax2.set_frame_on(True)
    # for spine in ax2.spines.values():
        # spine.set_edgecolor('green')
        # spine.set_linewidth(0.5)
        # spine.set_visible(True)

    # hide the label at the top of Y-axis (since it's overlapping with the other label)
    plt.setp(ax2.get_yticklabels()[-2], visible=False)

    print("Done")


    print("Setting 3rd-layer X-axis ...", end=" ")

    # Display X-axis as time in ms
    # ax3 = ax2.twiny()
    # ax3.set_xlabel(xlabel='Duration (ms)')
    # ax3.xaxis.set_label_position("bottom")
    # ax3.set_xlim(0, float(df.columns.values[-1])*tickToMsMultiplier)

    # Display X-axis as time in T_v
    ax3 = ax2.twiny()
    ax3.set_xlabel(xlabel='Duration ($T_v$)')
    ax3.xaxis.set_label_position("bottom")
    ax3.xaxis.labelpad = 0
    ax3.set_xlim(0, len(df.columns.values))

    ax3.tick_params(top=False, bottom=True, left=False, right=False, labelleft=False, labelbottom=True, labeltop=False)
    print("Done")


    ''' Draw the frequency ranking plot if required '''
    if displayFreqRanking:
        print("Plotting frequency ranking ...", end=" ")

        targetFreq = round(taskSet.getTaskById(taskRankingsObject[0]['id']).frequency, 2)
        taskFreqRanking = taskRankingsObject[0]['ranking']

        axr.tick_params(top=True, bottom=True, left=True, right=False, labelleft=True, labelbottom=False, labeltop=False)

        axr.set_xlim(0, len(taskFreqRanking)-1)
        axr.tick_params(axis="x",direction="in")

        axr.set_ylabel(ylabel='Ranking')
        axr.set_ylim(1, max(taskFreqRanking))
        axr.set_yscale("log")
        axr.yaxis.labelpad = 0

        # Displaying minor ticks for log scale
        # Solution: https://stackoverflow.com/questions/44078409/matplotlib-semi-log-plot-minor-tick-marks-are-gone-when-range-is-large
        axr.get_yaxis().set_major_locator(ticker.LogLocator(numticks=max(taskFreqRanking)))
        locmin = ticker.LogLocator(base=10.0, subs=(0.2, 0.4, 0.6, 0.8), numticks=max(taskFreqRanking))
        axr.yaxis.set_minor_locator(locmin)
        axr.yaxis.set_minor_formatter(ticker.NullFormatter())

        axr.grid(linestyle=':')
        axr.plot(taskFreqRanking, color='navy', linewidth=1, label="{}Hz Ranking".format(targetFreq))
        leg = axr.legend()
        leg.get_frame().set_edgecolor('w')


        # Now let's draw the ground truth arrow
        ax_arrow.set_frame_on(False)
        ax_arrow.tick_params(top=False, bottom=False, left=False, right=False, labelleft=False, labelbottom=False,
                        labeltop=False)
        ax_arrow.arrow(1, targetFreq/freq_upper_limit, -0.5, 0, head_width=0.05, head_length=0.5, color='navy')
        ax_arrow.text(0.25, targetFreq/freq_upper_limit + 0.07, str(targetFreq) + "Hz", rotation=90, size=12, color='navy')

        print("Done")


    ''' Post-processing the plot '''
    print("Tightening layout ...", end=" ")
    plt.tight_layout()
    print("Done")


    ''' Save the plot to files with the specified format '''
    for outFileName in outFileNames:
        print("Exporting the plot ...", end=" ")
        outputFormat = outFileName.split('.')[-1]
        if outputFormat == "pdf" or outputFormat == "png":
            plt.savefig(outFileName, pad_inches=0.015, bbox_inches='tight')
            print('Save the plot to "{}".'.format(outFileName))
        print("Done")


    if showPlot:
        print("Showing the plot ...")
        plt.show()
        print("Done")

