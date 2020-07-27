from scipy.stats import laplace
import PlotUtility
import matplotlib.pyplot as plt
from matplotlib.ticker import PercentFormatter
import matplotlib.ticker as ticker
import numpy as np
import seaborn as sns
from random import randint

# The range of percentile is 0 to 1.0
def compute_noise_level_percentile(location, epsilon, j, delta, percentile):
    # Equation:
    #   b = 2 J \Delta\eta / \epsilon
    b = (2*j*delta)/epsilon
    return laplace.ppf(percentile, scale=b, loc=location)


def get_flattened_laplace_distribution(location, epsilon, j, delta, steps, round_to_int):
    left_percentile = 0.501   # this should be zero since Laplace Dist. is symmetric
    right_percentile = 0.99

    flattened_distribution = []
    for i in range(steps):
        percentile = i*((right_percentile-left_percentile)/(steps-1)) + left_percentile
        sample = compute_noise_level_percentile(location, epsilon, j, delta, percentile)
        if round_to_int:
            try:
                sample = int(sample)
            except:
                sample = flattened_distribution[-1]
        # print(sample)
        flattened_distribution.append(sample)

    return flattened_distribution


def get_epsilon_scheduler_noise_distribution_curve(location, epsilon, j, delta):
    # Equation:
    #   b = 2 J \Delta\eta / \epsilon
    b = (2*j*delta)/epsilon

    # 5% to 95%
    left_bin = int(compute_noise_level_percentile(location, epsilon, j, delta, 0.01))
    right_bin = int(compute_noise_level_percentile(location, epsilon, j, delta, 0.99))

    # print(left_bin)
    # print(right_bin)

    curve_x = []
    curve_y = []
    total_count = 100
    resolution = (right_bin-left_bin)/total_count
    for i in range(total_count+1):
        curve_x.append(left_bin + resolution*i)
        curve_y.append(laplace.pdf(left_bin + resolution*i, loc=location, scale=b))
        # print(left_bin + resolution*i, ",", curve[-1])

    # for i in range(left_bin, right_bin+1):
    #     curve.append(laplace.pdf(i, loc=location, scale=b))
    #     print(i, ",", curve[-1])

    return curve_x, curve_y


def convert_to_c_array_string(list_to_convert):
    array_string = "{"
    for i in range(len(list_to_convert)):
        if i != 0:
            array_string += ", "
        array_string += str(list_to_convert[i])
    array_string += "}"
    return array_string


if __name__ == '__main__':
    j = 16
    delta = 190    # 190 ms
    epsilon = 100
    location = 100
    flattened_steps = 100

    ### Output C array ###
    ### Unit becomes 100us here in this loop building array strings ####
    # for epsilon_i in [1, 10, 100, 1000]:
    #     flattened_i = get_flattened_laplace_distribution(0, epsilon_i, j, delta*10, flattened_steps, round_to_int=True)
    #     print("\t{},\t// epsilon = {}".format(str(convert_to_c_array_string(flattened_i)), epsilon_i))

    flattened = get_flattened_laplace_distribution(location, epsilon, j, delta, flattened_steps, round_to_int=True)

    samples = []
    for i in range(10000):
        rad_idx = randint(0, len(flattened)*2-1)
        if rad_idx > len(flattened)-1:
            sample = 2*location - flattened[rad_idx-len(flattened)]
        else:
            sample = flattened[rad_idx]
        samples.append(sample)

    dist_x, dist_y = get_epsilon_scheduler_noise_distribution_curve(location, epsilon, j, delta)

    ''' general plot configurations '''
    PlotUtility.config_plot(plt)
    plt.rcParams['figure.figsize'] = 6,4
    fig, ax = plt.subplots()

    ax.hist(samples, bins=100, color=PlotUtility.palletForMany[2], ec='white', weights=np.ones(len(samples)) / len(samples))
    # ax.hist(rpi3_raw_data, bins=38, color=PlotUtility.palletForMany[2], ec='white', weights=np.ones(len(rpi3_raw_data)) / len(rpi3_raw_data))
    # ax.hist(rpi3_raw_data, bins=38, color=PlotUtility.palletForMany[2], ec='white')
    # plt.gca().yaxis.set_major_formatter(PercentFormatter(1))

    # y axis config
    plt.ylabel('Ralative Frequency')
    plt.ylim(bottom=0)
    start, end = ax.get_ylim()
    ax.yaxis.set_ticks(np.arange(start, end, 0.01))
    # ax.yaxis.set_major_formatter(ticker.FormatStrFormatter('%0.2f'))

    # x axis config
    plt.xlabel('Random Inter-Arrival Times (ms)')
    plt.xlim(20, 180)
    plt.xticks([i for i in range(20, 180+1, 20)])

    # Grid
    # plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    # plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)


    ax2 = ax.twinx()
    ax2.plot(dist_x, dist_y, color=PlotUtility.palletForMany[2], linestyle='--')
    plt.ylim(bottom=0)
    ax2.axis('off')

    ylim = max(dist_y)*1.1




    # inadmissible area on the left
    # left, bottom, width, height = (0, 0, 10, ylim)
    # rect = plt.Rectangle((left, bottom), width, height,
    #                      facecolor="black", alpha=0.1)
    # ax2.add_patch(rect)
    #
    # # inadmissible area on the right
    # left, bottom, width, height = (200, 0, 10, ylim)
    # rect = plt.Rectangle((left, bottom), width, height,
    #                      facecolor="black", alpha=0.1)
    # ax2.add_patch(rect)


    # Post plot configurations



    # Legend
    # legend = plt.legend(shadow=True, loc='upper right', title='$\mu$')
    # legend.get_frame().set_edgecolor('grey')

    # title = "$J_i={}$".format(j)
    # title += ", $\Delta\eta_i={}$".format(delta)
    # title += ", $\epsilon_i={}$".format(epsilon)
    # # props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    # ax.text(1, 1.08, title, transform=ax.transAxes, fontsize=14,
    #         verticalalignment='top', horizontalalignment='right')


    show_plot, csv_file_list, out_plot_file_list, label_list = PlotUtility.parse_arguments()
    # out_plot_file_list = []
    # show_plot = True

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
