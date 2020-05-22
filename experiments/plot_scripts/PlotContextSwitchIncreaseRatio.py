import math
import numpy as np
import matplotlib.pyplot as plt
import PlotUtility


# This plot takes two CSV files as input: 1. RM, 2. TaskShuffler
def plot_context_switch_increase_ratio(show_plot, in_csv_file_list, out_plot_file_list, label_list):

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
    y = data_dict2['Context_Switches']/data_dict1['Context_Switches'] # context switch ratio

    x_mean = []
    y_mean = []
    y_error = []
    for u in range(10):
        util = u/10
        matched_values = np.array([])
        for i in range(len(y)):
            if x[i]>=util and x[i]<(util+0.1):
                matched_values = np.append(matched_values, y[i])
        x_mean.append(u/10 + 0.05)
        y_mean.append(matched_values.mean())
        y_error.append(matched_values.std())


    # plot general config
    # plt.title('Interesting Graph\nCheck it out')
    # plt.legend()
    # plt.grid(True, 'major', 'both', color='0.8', linestyle='--', linewidth=1)

    # x axis config
    plt.xlabel('Task Set Utilization')
    plt.xlim(0, 1)
    plt.xticks([i/10 for i in range(0, 11, 1)])

    # y axis config
    plt.ylabel('Context Switch Ratio \n (TaskShuffler/RM)')
    # ylimMax = (max(y)/5+1)*5
    ylimMax = math.ceil(max(y))
    plt.ylim([0.8, ylimMax])
    plt.yticks([i*0.5 for i in range(2, (int(ylimMax))*2, 1)])
    # plt.ylim(0, 1.1)
    # plt.yticks([i/10 for i in range(1, 11, 1)])

    # plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    # plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)


    # minor_ticks = [tmp / 10 for tmp in range(0, 11)]
    # plt.axes().set_yticks(minor_ticks, minor=True)

    # data points
    markerSize = 30
    plt.scatter(x, y, marker='o', facecolors='none', color='DodgerBlue', alpha=0.7, s=[markerSize for i in range(len(y))])

    plt.errorbar(x_mean,y_mean,y_error,color="black",marker='o')


    # Post plot configurations
    # legend = plt.legend(shadow=False, loc='upper right')
    # legend.get_frame().set_edgecolor('grey')

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
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/for_sok_paper/rm_10lcm_sleaklcm.csv',
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/for_sok_paper/ts3_10lcm_sleaklcm.csv'
    # ]
    # label_list = ["RM", "TaskShuffler"]
    # show_plot = True

    show_plot, csv_file_list, out_plot_file_list, label_list = PlotUtility.parse_arguments()

    plot_context_switch_increase_ratio(show_plot, csv_file_list, out_plot_file_list, label_list)