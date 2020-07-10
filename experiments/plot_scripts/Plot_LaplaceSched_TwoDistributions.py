import PlotUtility
import matplotlib.pyplot as plt
from scipy.stats import laplace


# The range of percentile is 0 to 1.0
def compute_noise_level_percentile(location, epsilon, j, delta, percentile):
    # Equation:
    #   b = 2 J \Delta\eta / \epsilon
    b = (2*j*delta)/epsilon
    return laplace.ppf(percentile, scale=b, loc=location)

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


if __name__ == '__main__':

    # Experiment config
    period1 = 33.33
    period2 = 100
    j = 16
    delta = 190
    epsilon = 100


    PlotUtility.config_plot(plt)
    plt.rcParams['figure.figsize'] = 6,4
    fig, ax = plt.subplots()

    curve1_x, curve1_y = get_epsilon_scheduler_noise_distribution_curve(location=period1, epsilon=epsilon, j=j, delta=delta)
    curve2_x, curve2_y = get_epsilon_scheduler_noise_distribution_curve(location=period2, epsilon=epsilon, j=j, delta=delta)
    ylim = max(max(curve1_y),max(curve2_y))*1.1


    ax.plot(curve1_x, curve1_y, color=PlotUtility.palletForMany[0], linestyle=PlotUtility.line_style_list[0], linewidth=2, label='$33.3ms$')
    ax.plot(curve2_x, curve2_y, color=PlotUtility.palletForMany[2], linestyle=PlotUtility.line_style_list[0], linewidth=2, label='$100ms$')

    # y axis config
    plt.ylabel('Probability Density Function')
    plt.ylim(0, ylim)


    # x axis config
    plt.xlabel('Inter-Arrival Time (ms)')
    plt.xlim(0, 210)
    plt.xticks([i for i in range(0, 200+1, 20)])

    # inadmissible area on the left
    left, bottom, width, height = (0, 0, 10, ylim)
    rect = plt.Rectangle((left, bottom), width, height,
                         facecolor="black", alpha=0.1)
    ax.add_patch(rect)

    # inadmissible area on the right
    left, bottom, width, height = (200, 0, 10, ylim)
    rect = plt.Rectangle((left, bottom), width, height,
                         facecolor="black", alpha=0.1)
    ax.add_patch(rect)

    # ax.arrow(period1, 0, 0, 1, head_width=3, head_length=0.03, color='maroon')
    ax.plot([period1, period1], [0, ylim], color='maroon', alpha=0.8, linestyle='--', linewidth=1.5)
    ax.plot([period2, period2], [0, ylim], color='maroon', alpha=0.8, linestyle='--', linewidth=1.5)


    # Post plot configurations

    # Grid
    plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)

    # Legend
    legend = plt.legend(shadow=True, loc='upper right', title='$\mu$')
    legend.get_frame().set_edgecolor('grey')

    title = "$J_i={}$".format(j)
    title += ", $\Delta\eta_i={}$".format(delta)
    title += ", $\epsilon_i={}$".format(epsilon)
    # props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
    ax.text(1, 1.08, title, transform=ax.transAxes, fontsize=14,
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


