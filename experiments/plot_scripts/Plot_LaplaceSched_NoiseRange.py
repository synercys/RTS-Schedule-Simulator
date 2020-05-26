import PlotUtility
import matplotlib.pyplot as plt
from scipy.stats import laplace


# The range of percentile is 0 to 1.0
def compute_noise_level_percentile(epsilon, j, delta, percentile):
    # Equation:
    #   b = 2 J \Delta\eta / \epsilon
    b = (2*j*delta)/epsilon
    return laplace.ppf(percentile, scale=b)



def plot_noise_range(show_plot, in_csv_file_list, out_plot_file_list, label_list):
    percentile = 0.95
    delta = 10 # 10 ms
    j_list = [1, 10, 50, 100]

    # Build epsilon list
    epsilon_list = []
    # [0.01, 0.1)
    epsilon_list.extend([i/100 for i in range(1, 10)])
    # [0.1, 1)
    epsilon_list.extend([i/10 for i in range(1, 10)])
    # [1, 1000]
    epsilon_list.extend([i for i in range(1, 1000)])
    # epsilon_list.extned([pow(10, i) for i in range(-2, 5, 1)])

    noise_level_list = []
    for i in range(len(j_list)):
        noise_level_list.append([compute_noise_level_percentile(epsilon, j_list[i], delta, percentile) for epsilon in epsilon_list])


    ### general plot configurations ###
    PlotUtility.config_plot(plt)
    plt.rcParams['figure.figsize'] = 5.5,5

    # ax.set_xscale("log")
    plt.xscale('log')
    plt.yscale('log')


    for i in range(len(j_list)):
        plt.plot(epsilon_list, noise_level_list[i], label=j_list[i], linewidth=2, linestyle=PlotUtility.line_style_list[i])


    # Post plot configurations

    # Legend
    legend = plt.legend(shadow=True, loc='lower left', title='$J$')
    legend.get_frame().set_edgecolor('grey')

    # Axis Titles
    plt.ylabel('Noise Scale at '+str(100*percentile)[:-2]+'$^{th}$ Percentile ($ms$)')
    plt.xlabel('$\epsilon$')

    plt.xlim(0, max(epsilon_list)+1)
    plt.ylim(0, 10000)
    # plt.ylim(bottom=0)

    # Grid
    plt.grid(True, 'major', 'y', color='0.8', linestyle='--', linewidth=1)
    plt.grid(True, 'major', 'x', color='0.8', linestyle='--', linewidth=1)





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
    plot_noise_range(show_plot, csv_file_list, out_plot_file_list, label_list)