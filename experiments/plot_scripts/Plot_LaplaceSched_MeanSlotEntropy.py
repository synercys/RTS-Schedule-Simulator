import numpy as np
import matplotlib.pyplot as plt
import PlotUtility


# This plot takes 1+n CSV files as input: 1. EDF, 2++. Laplace with different epsilon
def plot_peak_count(show_plot, in_csv_file_list, out_plot_file_list, label_list):

    ''' general plot configurations '''
    PlotUtility.config_plot(plt)
    # plt.rcParams['figure.figsize'] = 8,5.5
    plt.rcParams['figure.figsize'] = 6.5,5


    plot_labels = [] if label_list is None else label_list

    ''' Data '''
    x_list = []
    y_list = []
    y_mean_list = []
    y_error_list = []
    x_mean = []
    first = True
    for csv_file in in_csv_file_list:
        data_dict = np.genfromtxt(csv_file, delimiter=',', unpack=True, names=True)
        # if first:
        #     x = data_dict['Utilization']
        #     first = False

        this_x = data_dict['Utilization']
        x_list.append(this_x)

        this_y = data_dict['Entropy']
        y_list.append(this_y)

        x_mean = []
        y_mean = []
        y_error = []
        for u in range(10):
            util = u/10
            matched_values = np.array([])
            for i in range(len(this_y)):
                if util <= this_x[i] < (util + 0.1):
                    matched_values = np.append(matched_values, this_y[i])
            x_mean.append(u/10 + 0.05)
            y_mean.append(matched_values.mean())
            y_error.append(matched_values.std())

        y_mean_list.append(y_mean)
        y_error_list.append(y_error)


    # plot general config
    # plt.title('Interesting Graph\nCheck it out')
    # plt.legend()
    # plt.grid(True, 'major', 'both', color='0.8', linestyle='--', linewidth=1)

    # x axis config
    plt.xlabel('Task Set Utilization')
    plt.xlim(0, 1)
    plt.xticks([i/10 for i in range(0, 11, 1)])

    # y axis config
    plt.ylabel('Average Slot Entropy')
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
        x = x_list[i]
        y_mean = y_mean_list[i]
        y_error = y_error_list[i]
        plt.scatter(x, y, marker=PlotUtility.pattern_list[i], facecolors=PlotUtility.palletForMany[i], color=PlotUtility.palletForMany[i], alpha=0.5, s=[markerSize for i in range(len(y))], label=plot_labels[i])
        plt.errorbar(x_mean,y_mean,y_error,color="black",marker=PlotUtility.pattern_list[i], linewidth=1)


    # Post plot configurations
    legend = plt.legend(shadow=False)
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
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_variance/edf-d20000_dftDuration.csv',
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_variance/laplace-e1000-d20000_dftDuration.csv',
    #     '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_variance/laplace-e10-d20000_dftDuration.csv',
    #     # '/Users/jjs/Documents/myProject/RTS-Schedule-Simulator/experiments/test_cases/laplace_scheduler/dft_variance/laplace-e1-d20000_dftDuration.csv'
    # ]
    # label_list = ["EDF", "$\epsilon-Scheduler=1000$", "$\epsilon-Scheduler=10$"]
    # show_plot = True

    show_plot, csv_file_list, out_plot_file_list, label_list = PlotUtility.parse_arguments()

    plot_peak_count(show_plot, csv_file_list, out_plot_file_list, label_list)