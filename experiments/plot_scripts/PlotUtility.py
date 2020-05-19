import argparse
import matplotlib.pyplot as plt


def parse_arguments():
    ''' Command line arguments '''
    parser = argparse.ArgumentParser()
    parser.add_argument('-i', "--in", required=True, action='append',
                        help="One or more CSV files with data to be plotted.")
    parser.add_argument('-o', "--out", required=False, action='append', help="Save the plot as a PDF file with the specified file name.")
    parser.add_argument('-l', "--label", required=False, action='append', help="Set line label(s).")
    parser.add_argument("--hide", action='store_true', help="Do not display the resulting plot.")
    # parser.add_argument('-c', "--case", required=True, help='Test case to be plotted ["duration", "histogram", "tasknums", "trial"].')

    # args = vars(parser.parse_args("-i /Users/jjs/Documents/myProject/ScheduLeak-Experiments/edf_scheduleak/sleak_all_duration_rawPrecision.csv -i /Users/jjs/Documents/myProject/ScheduLeak-Experiments/edf_scheduleak/esleak_all_duration_rawPrecision.csv -c tasknums -o test".split()))
    # args = vars(parser.parse_args("--in /Users/jjs/Documents/myProject/ScheduLeak-Experiments/edf_scheduleak/sleak_all_duration_rawPrecision.csv -c histogram".split()))
    args = vars(parser.parse_args())

    ''' Argument variables '''
    param_show_plot = not args["hide"]
    param_out_file_list = args["out"] if args["out"] is not None else []
    param_csv_file_list = args["in"]
    param_label_list = args["label"] if args["label"] is not None else []


    return param_show_plot, param_csv_file_list, param_out_file_list, param_label_list


def config_plot(plt):
    # plt.rcParams['figure.figsize'] = 8, 8
    plt.rcParams["font.family"] = "Arial"
    plt.rcParams["mathtext.fontset"] = "dejavuserif"
    plt.rcParams['font.size'] = 16
    # plt.rcParams['legend.fontsize'] = 13
    # plt.rcParams['axes.titlesize'] = 15
    # plt.rcParams['ytick.labelsize'] = 16
    # plt.rcParams['xtick.labelsize'] = 16
    plt.rcParams['hatch.linewidth'] = 0.15
    plt.rcParams['hatch.color'] = 'Black'
    # plt.rcParams['axes.axisbelow'] = False


palletFor2 = [{'color': 'lightgrey',
               'hatch': '',  # .....
               'alpha': 0.8},
              {'color': 'DodgerBlue',
               'hatch': '/////',
               'alpha': 0.7}]

pallet = [{'color': 'DodgerBlue',
           'hatch': '',
           'alpha': 0.7}]
