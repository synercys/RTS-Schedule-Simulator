import numpy as np
import matplotlib.pyplot as plt
import os
import PlotUtility


def plot_case_entropy_correlation(show_plot, in_csv_file_list, out_plot_filename):

    ### general plot configurations ###
    PlotUtility.config_plot(plt)

    plt.rcParams['figure.figsize'] = 6,5.5

    plot_labels = []

    data_dict_base = np.genfromtxt(in_csv_file_list[0], delimiter=',', unpack=True, names=True)
    x = data_dict_base['Entropy']

    y = []
    for idx, in_csv_file in enumerate(in_csv_file_list):
        if idx == 0:
            continue
        y.append(np.genfromtxt(in_csv_file, delimiter=',', unpack=True, names=True)['Entropy'])

    for idx in range(len(y)-len(plot_labels)):
        plot_labels.append("")

    # # 100%
    # data_dict_1 = np.genfromtxt(in_csv_file_list[1], delimiter=',', unpack=True, names=True)
    # y100 = data_dict_1['Entropy']
    #
    # # 75%
    # data_dict_2 = np.genfromtxt(in_csv_file_list[2], delimiter=',', unpack=True, names=True)
    # y75 = data_dict_2['Entropy']
    #
    # # 50%
    # data_dict_2 = np.genfromtxt(in_csv_file_list[3], delimiter=',', unpack=True, names=True)
    # y50 = data_dict_2['Entropy']
    #
    # # 25%
    # data_dict_2 = np.genfromtxt(in_csv_file_list[4], delimiter=',', unpack=True, names=True)
    # y25 = data_dict_2['Entropy']


    # plot general config
    # plt.title('Interesting Graph\nCheck it out')
    # plt.grid(True, 'major', 'both', color='0.8', linestyle='--', linewidth=1)

    # x axis config
    plt.xlabel('True Schedule Entropy')

    plt.xlim(0, 20)
    # plt.xticks([i for i in range(0, 41, 5)])

    # y axis config
    plt.ylabel('Upper-Approximated Schedule Entropy')
    # ylimMax = (max(y)/5+1)*5
    # plt.ylim([0, 40])
    # plt.yticks([i for i in range(0, int(ylimMax)+1, 2)])
    # plt.yticks([i for i in range(0, 41, 5)])

    plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)


    # minor_ticks = [tmp / 10 for tmp in range(0, 11)]
    # plt.axes().set_yticks(minor_ticks, minor=True)

    # plt.plot([0, 40], [0, 40], '--', color='grey')

    # data points
    for idx in range(len(y)):
        plt.scatter(x, y[idx], marker='o', color=PlotUtility.palletForMany[idx], alpha=0.8,
                    s=[100 for i in range(len(y))], label=plot_labels[idx])

    # plt.scatter(x, y100, marker='o', color=PlotUtility.palletForMany[0], alpha=0.8, s=[100 for i in range(len(y100))], label="100%")
    # plt.scatter(x, y75, marker='o', color=PlotUtility.palletForMany[1], alpha=0.8, s=[100 for i in range(len(y75))], label="75%")
    # plt.scatter(x, y50, marker='o', color=PlotUtility.palletForMany[2], alpha=0.8, s=[100 for i in range(len(y50))], label="50%")
    # plt.scatter(x, y25, marker='o', color=PlotUtility.palletForMany[3], alpha=0.8, s=[100 for i in range(len(y25))], label="25%")

    # Post plot configurations
    # legend = plt.legend(shadow=True, loc='lower right')
    # legend.get_frame().set_edgecolor('k')


    if not out_plot_filename == "":
        filename, extension = os.path.splitext(out_plot_filename)
        plt.savefig('{}.pdf'.format(filename), pad_inches=0.020, bbox_inches='tight')
        plt.savefig('{}.png'.format(filename), pad_inches=0.020, bbox_inches='tight')

    if show_plot:
        plt.show()


def plot_case_entropy_correlation_single(show_plot, in_csv_file_list, out_plot_filename):
    ### general plot configurations ###
    PlotUtility.config_plot(plt)

    plt.rcParams['figure.figsize'] = 6, 5.5

    data_dict_base = np.genfromtxt(in_csv_file_list[0], delimiter=',', unpack=True, names=True)
    x = data_dict_base['Entropy']

    data_dict_1 = np.genfromtxt(in_csv_file_list[1], delimiter=',', unpack=True, names=True)
    y = data_dict_1['Entropy']

    # plot general config
    # plt.title('Interesting Graph\nCheck it out')
    # plt.legend()
    # plt.grid(True, 'major', 'both', color='0.8', linestyle='--', linewidth=1)

    # x axis config
    plt.xlabel('True Schedule Entropy')

    plt.xlim(0, 40)
    plt.xticks([i for i in range(0, 41, 5)])

    # y axis config
    plt.ylabel('Upper-Approximated Schedule Entropy')
    # ylimMax = (max(y)/5+1)*5
    plt.ylim([0, 40])
    # plt.yticks([i for i in range(0, int(ylimMax)+1, 2)])
    plt.yticks([i for i in range(0, 41, 5)])

    plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)

    # minor_ticks = [tmp / 10 for tmp in range(0, 11)]
    # plt.axes().set_yticks(minor_ticks, minor=True)

    # data points
    plt.scatter(x, y, marker='o', color=PlotUtility.pallet[0]['color'], alpha=PlotUtility.pallet[0]['alpha'],
                s=[100 for i in range(len(y))])
    plt.plot([0, 40], [0, 40], '--', color='grey')

    if not out_plot_filename == "":
        filename, extension = os.path.splitext(out_plot_filename)
        plt.savefig('{}.pdf'.format(filename), pad_inches=0.020, bbox_inches='tight')
        plt.savefig('{}.png'.format(filename), pad_inches=0.020, bbox_inches='tight')

    if show_plot:
        plt.show()


if __name__ == '__main__':

    # output_file_name = "test"
    # csv_file_list = [
    #     '../experiments/output/esleak_all_duration_rawPrecision.csv'
    # ]
    # show_plot = True

    show_plot, csv_file_list, output_file_name = PlotUtility.parse_arguments()

    plot_case_entropy_correlation(show_plot, csv_file_list, output_file_name)
