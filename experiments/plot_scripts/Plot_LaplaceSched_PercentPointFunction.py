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

    # flattened_distribution = []
    curve_x, curve_y = [], []

    for i in range(steps):
        percentile = i*((right_percentile-left_percentile)/(steps-1)) + 0
        curve_x.append(percentile)
        curve_y.append(0)

    for i in range(steps):
        percentile = i*((right_percentile-left_percentile)/(steps-1)) + left_percentile
        curve_x.append(percentile)
        sample = compute_noise_level_percentile(location, epsilon, j, delta, percentile)
        if round_to_int:
            try:
                sample = int(sample)
            except:
                # sample = flattened_distribution[-1]
                sample = curve_y[-1]
        curve_y.append(sample)
        # flattened_distribution.append(sample)

    return curve_x, curve_y


def get_ppf_curve(location, epsilon , j, delta):
    total_count = 1000
    resolution = 1/(total_count+2-1)    # +2 is to remove 0% and 100% points
    curve_x, curve_y = [], []
    for i in range(1, total_count+1):
        curve_x.append(i*resolution)
        curve_y.append(compute_noise_level_percentile(location,epsilon,j,delta,i*resolution))
    return curve_x, curve_y


if __name__ == '__main__':
    j = 16
    delta = 190    # 190 ms
    epsilon = 100
    location = 100
    flattened_steps = 100

    ppf_x, ppf_y = get_ppf_curve(0, epsilon, j, delta)
    flattened_x, flattened_y = get_flattened_laplace_distribution(0, epsilon, j, delta, 100, True)


    ''' general plot configurations '''
    PlotUtility.config_plot(plt)
    plt.rcParams['figure.figsize'] = 6,5
    fig, ax = plt.subplots()

    ax.plot(ppf_x, ppf_y, color=PlotUtility.palletForMany[2], linestyle='--')


    # y axis config
    plt.ylabel('Percent Point Function of X')
    # plt.ylim(bottom=0)
    start, end = ax.get_ylim()
    end = (int(end/100)+1)*100
    # plt.ylim(-end, end)
    ax.yaxis.set_ticks(np.arange(-end, end, 100))
    # ax.yaxis.set_major_formatter(ticker.FormatStrFormatter('%0.2f'))

    # x axis config
    plt.xlabel('Cumulative Probability')
    plt.xlim(0, 1)
    plt.xticks([i/10 for i in range(0, 10+1)])

    # Grid
    plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)
    # plt.grid(True, 'minor', 'x', color='0.8', linestyle='--', linewidth=1)


    ax2 = ax.twiny()
    ax2.bar(np.arange(len(flattened_x)), flattened_y, width=1, color=PlotUtility.palletForMany[2])

    ax2.set_xlim(0,len(flattened_x))
    ax2.set_axis_off()



    # Post plot configurations



    # Legend
    # legend = plt.legend(shadow=True, loc='upper right', title='$\mu$')
    # legend.get_frame().set_edgecolor('grey')

    title = "$J_i={}$".format(j)
    title += ", $\Delta\eta_i={}$".format(delta)
    title += ", $\epsilon_i={}$".format(epsilon)
    # props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    ax.text(1, 1.065, title, transform=ax.transAxes, fontsize=14,
            verticalalignment='top', horizontalalignment='right')


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
