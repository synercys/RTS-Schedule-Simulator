import PlotUtility
import matplotlib.pyplot as plt
import numpy as np
import statistics
import math


def plot_mean_frequency_error_ratio(show_plot, in_csv_file_list, out_plot_file_list, label_list):

    ### general plot configurations ###
    plt.rcParams['figure.figsize'] = 6.5, 4    # it was 12, 5.5
    plt.rcParams["font.family"] = "Arial"
    plt.rcParams['font.size'] = 16
    # plt.rcParams['legend.fontsize'] = 13
    # plt.rcParams['axes.titlesize'] = 15
    # plt.rcParams['ytick.labelsize'] = 16
    # plt.rcParams['xtick.labelsize'] = 16
    plt.rcParams['hatch.linewidth'] = 0.15
    plt.rcParams['hatch.color'] = 'Black'
    # plt.title('Interesting Graph\nCheck it out')
    plt.rcParams['axes.axisbelow'] = True

    fig, ax = plt.subplots()

    # bar_width = 0.15 if len(in_csv_file_list)>4 else 0.2
    bar_width = 0.6/len(in_csv_file_list)

    x_ticks = [i for i in range(0, 10)]
    x_ticks_shifted = [i-len(in_csv_file_list)*0.5*bar_width-bar_width*0.5 for i in range(0, 10)]

    y_list = []
    y_mean_list = []
    y_error_list = []
    x_mean = []
    for in_csv_idx in range(len(in_csv_file_list)):
        style_idx = in_csv_idx + 2
        mean_precision_list = get_utilization_mean_precision_list(in_csv_file_list[in_csv_idx])
        # ax.errorbar(x, y, y_stdev, marker='o', color='b', alpha=1, linestyle='-', label="ScheduLeak-FP", linewidth=1)
        # ax.plot(mean_precision_list.keys(), mean_precision_list.values(), marker='s', color='k', alpha=1, linestyle='-', label="$ScheduLeak$ (FP)", linewidth=1)
        #x_ticks = [float(i)-0.33 for i in range(0, 10)]
        ax.bar([i+bar_width*(in_csv_idx+1) for i in x_ticks_shifted], mean_precision_list.values(), bar_width, color=PlotUtility.palletForManyDimCustom[style_idx], edgecolor='k', hatch=PlotUtility.hatch_list[style_idx], alpha=1, linestyle='-', label=label_list[in_csv_idx], linewidth=1)

        data_dict = np.genfromtxt(in_csv_file_list[in_csv_idx], delimiter=',', unpack=True, names=True)

        x = data_dict['Utilization']
        this_y = data_dict['Mean_Mean_Task_Frequency_Error']

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

        ax.errorbar([i+bar_width*(in_csv_idx+1) for i in x_ticks_shifted],y_mean,y_error,color="black",marker=PlotUtility.pattern_list[style_idx], linewidth=1, ls='none')

        y_mean_list.append(y_mean)
        y_error_list.append(y_error)

    ### post plot configurations ###
    plt.xlabel('Task Set Utilization')
    plt.ylabel('Mean Frequency Error Ratio')

    # plt.legend(loc='upper center', shadow=True, edgecolor='k', ncol=3)
    plt.legend(bbox_to_anchor=(0,1.2), loc="upper left", shadow=True, edgecolor='k', ncol=2)
    #legend = plt.legend(shadow=True, loc='top')
    #legend.get_frame().set_edgecolor('k')

    # plt.xlim(0, 5)
    plt.xticks(x_ticks)
    ax.set_xticklabels(['[0.0,0.1]','[0.1,0.2]','[0.2,0.3]','[0.3,0.4]','[0.4,0.5]','[0.5,0.6]','[0.6,0.7]','[0.7,0.8]','[0.8,0.9]','[0.9,1.0]'], rotation = 0)
    ax.xaxis.set_tick_params(rotation=45)

    plt.ylim(0, 1.0)
    # plt.grid(True, 'major', 'both', color='0.8', linestyle='--', linewidth=1)
    plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    minor_ticks = [tmp / 10 for tmp in range(0, 11)]
    ax.set_yticks(minor_ticks, minor=True)
    plt.grid(True, 'minor', 'y', color='0.8', linestyle='--', linewidth=1)


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


def get_utilization_mean_precision_list(in_csv_file):
    data_dict = np.genfromtxt(in_csv_file, delimiter=',', unpack=True, names=True)
    utilization_list = data_dict['Utilization']
    precision_list = data_dict['Mean_Mean_Task_Frequency_Error']

    utilization_precision_list = {}
    for i in range(10):
        utilization_precision_list[str(i)] = []

    for i in range(0, len(utilization_list)):
        utilization_key = str(math.floor(utilization_list[i]*10))
        if not utilization_key in utilization_precision_list:
            utilization_precision_list[utilization_key] = []
        utilization_precision_list[utilization_key].append(precision_list[i])

    utilization_mean_precision_list = {}
    for utilization_key, precision_list in utilization_precision_list.items():
        if len(precision_list) == 0:
            precision_list.append(0)
        utilization_mean_precision_list[utilization_key] = statistics.mean(precision_list)

    return utilization_mean_precision_list


if __name__ == '__main__':

    # out_plot_file_list = []
    # csv_file_list = [
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_peak_count/laplace-e1000-d50000_dftDuration.csv',
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_peak_count/laplace-e10-d50000_dftDuration.csv',
    # ]
    # label_list = ["$\epsilon-Sched(10^3)$", "$\epsilon-Sched(10)$"]
    # show_plot = True

    show_plot, csv_file_list, out_plot_file_list, label_list = PlotUtility.parse_arguments()

    plot_mean_frequency_error_ratio(show_plot, csv_file_list, out_plot_file_list, label_list)
