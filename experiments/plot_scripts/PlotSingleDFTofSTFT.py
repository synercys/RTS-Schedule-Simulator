import PlotUtility
import matplotlib.pyplot as plt
import json
import pandas as pd
import LogLoader
import matplotlib.ticker as ticker
import seaborn as sns



def findIndexOfFrequency(freqList, freqTarget):
    indexCount = 0
    for freq in freqList:
        if freq >= freqTarget:
            return indexCount
        indexCount += 1
    return indexCount


def plot_single_dft_of_stft(show_plot, in_csv_file_list, out_plot_file_list, label_list):

    ### general plot configurations ###
    PlotUtility.config_plot(plt)
    plt.rcParams['figure.figsize'] = 7, 2.5


    # Load .rtstft json
    print("Parsing the json file ...", end =" ")
    try:
        jsonRoot = json.load(open(in_csv_file_list[0], 'r'))
        print("Done")
    except:
        print("Failed to load the rtstft file: {}".format(in_csv_file_list[0]))
        exit()

    # Load task set
    print("Loading the task set ...", end =" ")
    taskSet = LogLoader.loadSingleTaskSetFromJsonObject(jsonRoot)
    print("Done")
    print(taskSet.toString())

    # Time unit
    nsPerTick = jsonRoot['data']['tickUnitInNs']
    tickToMsMultiplier = nsPerTick/1000_000_000

    # How many time bins (x-axis size)?
    lenTimeBins = len(jsonRoot['data']['unevenSpectrum'])
    dftIndx = lenTimeBins-1

    # Extract time bin labels
    timeBinLabels = []
    spectrumData = {}

    xFreq = jsonRoot['data']['unevenSpectrum'][dftIndx]['frequencies']



    # idx_of_freq_upper_limit = findIndexOfFrequency(jsonRoot['data']['unevenSpectrum'][i]['frequencies'],freq_upper_limit)
    # true_top_freq = jsonRoot['data']['unevenSpectrum'][i]['frequencies'][idx_of_freq_upper_limit]
    # print(true_top_freq, idx_of_freq_upper_limit)

    # axx = ax.inset_axes([i * heatmapWidth, 0, heatmapWidth, true_top_freq / freq_upper_limit])
    df = pd.DataFrame(jsonRoot['data']['unevenSpectrum'][dftIndx]['magnitudes'])
    df_max_min_range = 1 if (df.max() - df.min()).max() == 0 else (df.max() - df.min())  # in case if divisor is zero
    yMag = (df - df.min()) / (df_max_min_range)  # min-max normalization
    # yMag = df
    # sns.heatmap(df, cmap='Blues', cbar_kws={'label': 'Amplitude', 'aspect': 0.01}, ax=axx, cbar_ax=ax_cbar)
    # axx.tick_params(top=False, bottom=False, left=False, right=False, labelleft=False, labelbottom=False)
    # axx.set_ylim(-1, idx_of_freq_upper_limit + 1)





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
    # displayedAmplitude = [amp / maxAmplitude for amp in yMag[:xFreq_limitIndx]] # Normalized
    displayedAmplitude = yMag[:xFreq_limitIndx]

    # sns.heatmap(yMag, cmap='Blues', cbar_kws={'label': 'Amplitude'}, ax=ax)

    plt.plot(displayedFreq, displayedAmplitude, color='navy', alpha=0.75, linewidth=1.5)
    plt.grid(linestyle=':')

    '''Add ground truth lines'''
    if taskSet is not None:
        for task in taskSet.tasks:
            plt.plot([task.frequency, task.frequency], [0, yaxis_limit], color='maroon', alpha=0.8, linestyle='--', linewidth=1.5)
            ax.arrow(task.frequency, 1, 0, -0.05, head_width=3, head_length=0.03, color='maroon')


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

    out_plot_file_list = []
    csv_file_list = [
        '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_case_study_task_set/edf-v-CUSTFT-50.rtstft'
    ]
    label_list = ["RM", "TaskShuffler"]
    show_plot = True

    # show_plot, csv_file_list, out_plot_file_list, label_list = PlotUtility.parse_arguments()
    plot_single_dft_of_stft(show_plot, csv_file_list, out_plot_file_list, label_list)