import math
import numpy as np
import matplotlib.pyplot as plt
import PlotUtility
from matplotlib import gridspec
import matplotlib.ticker as ticker




# This plot takes 1+n CSV files as input: 1. EDF, 2++. Laplace with different epsilon
def plot_peak_count(show_plot, in_csv_file_list, out_plot_file_list, label_list):

    ''' general plot configurations '''
    PlotUtility.config_plot(plt)
    # plt.rcParams['figure.figsize'] = 8,5.5
    plt.rcParams['figure.figsize'] = 6.5,5.5

    AX = gridspec.GridSpec(6, 1)
    AX.update(wspace=0.3, hspace=0.15)

    ax_top = plt.subplot(AX[0, 0])
    ax = plt.subplot(AX[1:, 0])


    plot_labels = [] if label_list is None else label_list

    ''' Data '''
    y_list = []
    y_mean_list = []
    y_error_list = []
    x_mean = []
    first = True
    for csv_file in in_csv_file_list:
        data_dict = np.genfromtxt(csv_file, delimiter=',', unpack=True, names=True)
        if first:
            x = data_dict['Utilization']
            first = False
        this_y = data_dict['ZScore_Based_Peak_Count']
        y_list.append(this_y)

        x_mean = []
        y_mean = []
        y_error = []
        for u in range(10):
            util = u/10
            matched_values = np.array([])
            for i in range(len(this_y)):
                if x[i]>=util and x[i]<(util+0.1):
                    matched_values = np.append(matched_values, this_y[i])
            x_mean.append(u/10 + 0.05)
            y_mean.append(matched_values.mean())
            y_error.append(matched_values.std())

        y_mean_list.append(y_mean)
        y_error_list.append(y_error)

    ''' Deadline Miss Ratio (only for the last CSV) '''
    data_dict = np.genfromtxt(in_csv_file_list[-1], delimiter=',', unpack=True, names=True)
    x_deadline_miss_ratio = data_dict['Utilization']
    y_deadline_miss_ratio = data_dict['Deadline_Miss_Ratio']
    tasksets_with_deadline_misses_count = []
    for i in range(10):
        tasksets_with_deadline_misses_count.append(0)
        for j in range(len(x_deadline_miss_ratio)):
            if i/10 <= x_deadline_miss_ratio[j] < (i+1)/10:
                if y_deadline_miss_ratio[j] > 0.0:
                    tasksets_with_deadline_misses_count[i] += 1
        # print(tasksets_with_deadline_misses_count[-1])



    # plot general config
    # plt.title('Interesting Graph\nCheck it out')
    # plt.legend()
    # plt.grid(True, 'major', 'both', color='0.8', linestyle='--', linewidth=1)

    # x axis config
    plt.xlabel('Task Set Utilization')
    plt.xlim(0, 1)
    plt.xticks([i/10 for i in range(0, 11, 1)])

    # y axis config
    plt.ylabel('Outstanding Peak Count')
    # ylimMax = (max(y)/5+1)*5

    # ylimMax = math.ceil(max(y))
    # plt.ylim([0.8, ylimMax])
    # plt.yticks([i*0.5 for i in range(2, (int(ylimMax))*2, 1)])

    # plt.ylim(0, 1.1)
    # plt.yticks([i/10 for i in range(1, 11, 1)])

    # plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    # plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)


    # minor_ticks = [tmp / 10 for tmp in range(0, 11)]
    # plt.axes().set_yticks(minor_ticks, minor=True)

    # data points
    markerSize = 30
    for i in range(len(y_list)):
        y = y_list[i]
        y_mean = y_mean_list[i]
        y_error = y_error_list[i]
        ax.scatter(x, y, marker=PlotUtility.pattern_list[i], facecolors=PlotUtility.palletForMany[i], color=PlotUtility.palletForMany[i], alpha=0.5, s=[markerSize for i in range(len(y))], label=plot_labels[i])
        ax.errorbar(x_mean,y_mean,y_error,color="black",marker=PlotUtility.pattern_list[i], linewidth=1)

    ax_top.set_xlim(-0.5,9.5)
    # ax_top_ytop = math.ceil(max(y_deadline_miss_ratio)/0.1)*0.1
    ax_top_ytop = math.ceil(max(tasksets_with_deadline_misses_count)/50)*50
    # # ax_top.set_ylim(0, max(y_deadline_miss_ratio))
    ax_top.set_ylim(0, ax_top_ytop)
    # ax_top.get_yaxis().set_major_locator(ticker.LinearLocator(numticks=(ax_top_ytop/0.1)+1))
    ax_top.get_yaxis().set_major_locator(ticker.LinearLocator(numticks=(ax_top_ytop/50)+1))
    ax_top.yaxis.tick_right()
    ax_top.tick_params(top=False, bottom=False, left=False, right=True, labelleft=False, labelbottom=False, labeltop=False, labelright=False, direction='in')
    ax_top.spines["left"].set_visible(False)
    ax_top.spines["top"].set_visible(False)
    # ax_top.scatter(x_deadline_miss_ratio, y_deadline_miss_ratio, marker='o', facecolors='grey', color='grey', alpha=0.5, s=[5 for i in range(len(y_deadline_miss_ratio))],)

    # ax_top.bar(range(10), tasksets_with_deadline_misses_count)
    ax_top.plot(range(10), tasksets_with_deadline_misses_count, marker=PlotUtility.pattern_list[len(csv_file_list)-1], markersize=10, color='black')
    ax_top.text(0, 1.1, "The Number of Task Sets\nthat Have Deadline Misses\nin {}".format(label_list[-1]), transform=ax_top.transAxes, fontsize=14,
            verticalalignment='top', horizontalalignment='left')

    for i in range(10):
        if tasksets_with_deadline_misses_count[i] > 0:
            ax_top.text(i, tasksets_with_deadline_misses_count[i]+10, '$\\frac{' + str(tasksets_with_deadline_misses_count[i]) + '}{600}$', fontsize=12, ha='center', va='bottom')


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

    # out_plot_file_list = []
    # csv_file_list = [
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_peak_count/edf-d50000_dftDuration.csv',
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_peak_count/reorder3-d50000_dftDuration.csv',
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_peak_count/laplace-e1000-d50000_dftDuration.csv',
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_peak_count/laplace-e10-d50000_dftDuration.csv'
    # ]
    # label_list = ["Vanilla EDF", "TaskShuffler EDF", "$\epsilon-Scheduler=10^3$", "$\epsilon-Scheduler=10$"]
    # show_plot = True

    show_plot, csv_file_list, out_plot_file_list, label_list = PlotUtility.parse_arguments()

    plot_peak_count(show_plot, csv_file_list, out_plot_file_list, label_list)