import numpy as np
import argparse
import sys

from pcapng import FileScanner
from pcapng.blocks import EnhancedPacket

from scapy.layers.inet import IP, TCP
from scapy.layers.l2 import Ether


def size_to_string(size):
    # 2**10 = 1024
    power = 2**10
    n = 0
    power_labels = {0 : '', 1: 'K', 2: 'M', 3: 'G', 4: 'T'}
    while size > power:
        size /= power
        n += 1
    return size, power_labels[n]


if __name__ == '__main__':

    parser = argparse.ArgumentParser()

    # required arguments
    required_arguments = parser.add_argument_group('required arguments')
    required_arguments.add_argument('-i', '--input', help='Path to a Pcapng file', required=True)

    # optional arguments
    parser.add_argument("--src", help="Source IP address")
    parser.add_argument("--dst", help="Destination IP address")
    parser.add_argument("--start_id", help="Beginning packet ID to include in the analysis")
    parser.add_argument("--end_id", help="Ending packet ID to include in the analysis")
    parser.add_argument("--duration", help="Compute FPS based on 1 seond as a unit, for the specified seconds")
    parser.add_argument("--byte", help="Calculate Bytes rather than the default bits, per second", action="store_true")
    parser.add_argument("--format", help="Format the output value", action="store_true")
    parser.add_argument("-d", "--debug", help="Enable raw data messages", action="store_true")

    # Parse arguments
    args = parser.parse_args()
    pcapngFilePath = args.input

    if args.src is not None:
        src_ip = args.src
    else:
        src_ip = '73.202.58.231'

    if args.dst is not None:
        dst_ip = args.dst
    else:
        dst_ip = '192.168.0.16'

    if args.start_id is not None:
        start_id = int(args.start_id)
    else:
        start_id = 0

    if args.end_id is not None:
        end_id = int(args.end_id)
    else:
        end_id = sys.maxsize

    if args.duration is not None:
        duration = float(args.duration)
    else:
        duration = sys.maxsize

    useFormat = args.format
    useByte = args.byte

    enabled_debug = args.debug

    with open(pcapngFilePath, 'rb') as fp:
        scanner = FileScanner(fp)
        firstBlock = True
        firstValidPacket = True
        frameDataSize = 0
        bpsDp = []
        seqHistory = []
        packetId = 0
        fpsDpCount = 0
        perSecondFrameCount = 0
        startTimestampOfCurrentSecond = 0
        bitsPerSecond = 0
        for block in scanner:

            if firstBlock and isinstance(block, EnhancedPacket):
                firstBlock = False
                timestampOrigin = block.timestamp

            if isinstance(block, EnhancedPacket):
                packetId += 1

                if packetId < start_id:
                    continue

                if packetId > end_id:
                    break

                decoded = Ether(block.packet_data)
                _pl1 = decoded.payload
                _pl2 = _pl1.payload

                if isinstance(_pl1, IP) and isinstance(_pl2, TCP) and _pl1.src == src_ip and _pl1.dst == dst_ip:

                    # Skip this packet if it was sent before
                    currentSeq = _pl2.seq
                    if currentSeq in seqHistory:
                        if enabled_debug:
                            print("Skipped a redundant packet.")
                        continue
                    else:
                        seqHistory.append(currentSeq)

                    currentPacketTimestamp = block.timestamp - timestampOrigin

                    if firstValidPacket:
                        firstValidPacket = False
                        firstValidPacketTimestamp = currentPacketTimestamp
                        startTimestampOfCurrentSecond = currentPacketTimestamp

                    if currentPacketTimestamp - startTimestampOfCurrentSecond < 1.0:
                        bitsPerSecond += len(_pl2.payload)*8
                    else:
                        startTimestampOfCurrentSecond += 1.0
                        bpsDp.append(bitsPerSecond)

                        if enabled_debug:
                            if useFormat:
                                if useByte:
                                    value, unit = size_to_string(bitsPerSecond / 8)
                                    unit = unit + 'Bps'
                                else:
                                    value, unit = size_to_string(bitsPerSecond)
                                    unit = unit + 'bps'
                                print('{:.2f}s:\t{:.2f}\t{}'.format(startTimestampOfCurrentSecond, value, unit))
                            else:
                                if useByte:
                                    print('{:.2f}s:\t{:.2f}\t{}'.format(startTimestampOfCurrentSecond, bitsPerSecond/8, 'Bps'))
                                else:
                                    print('{:.2f}s:\t{:.2f}\t{}'.format(startTimestampOfCurrentSecond, bitsPerSecond, 'bps'))

                            bitsPerSecond = len(_pl2.payload)*8

                    if currentPacketTimestamp - firstValidPacketTimestamp > duration:
                        break

        # npFpsDp = np.array(bpsDp)

        # # Remove outliers
        # distance_from_mean = abs(npFpsDp - npFpsDp.mean())
        # max_deviations = 2
        # not_outlier = distance_from_mean < max_deviations * npFpsDp.std()
        # npFpsDp = npFpsDp[not_outlier]

        # meanFps = npFpsDp.mean()
        # stdFps = npFpsDp.std()
        # maxFps = npFpsDp.max()
        # minFps = npFpsDp.min()
        # cvFps = stdFps/meanFps
        #
        # print('Results for "{}":'.format(pcapngFilePath))
        # print('  - Config:', end='')
        # print('      {}'.format(args))
        # # print('      packet ID range: {} ~ {}'.format(start_id, end_id))
        # # print('      FPS data point count: {}'.format(fps_count_limit))
        # print('  - FPS Results:', end='')
        # print('      Max: {:.2f}\t Mean: {:.2f}\t Min:{:.2f}\t Std: {:.2f}\t CV: {:.2f}'.format(maxFps, meanFps, minFps, stdFps, cvFps))
        # print('      latex: {:.2f} & {:.2f} & {:.2f} & {:.2f} & {:.2f}'.format(maxFps, meanFps, minFps, stdFps, cvFps))
        # print('')
