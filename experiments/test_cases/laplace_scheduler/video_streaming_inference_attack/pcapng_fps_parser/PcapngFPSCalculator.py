import numpy as np
import argparse
import sys

from pcapng import FileScanner
from pcapng.blocks import EnhancedPacket

from scapy.layers.inet import IP, TCP
from scapy.layers.l2 import Ether

if __name__ == '__main__':

    parser = argparse.ArgumentParser()

    # required arguments
    required_arguments = parser.add_argument_group('required arguments')
    required_arguments.add_argument('-i', '--input', help='Path to a Pcapng file', required=True)

    # optional arguments
    parser.add_argument("--start_id", help="Beginning packet ID to include in the analysis")
    parser.add_argument("--end_id", help="Ending packet ID to include in the analysis")
    parser.add_argument("--fps_count", help="The number of FPS data points to be analyzed")
    parser.add_argument("-d", "--debug", help="Enable raw data messages", action="store_true")

    # Parse arguments
    args = parser.parse_args()
    pcapngFilePath = args.input

    if args.start_id is not None:
        start_id = int(args.start_id)
    else:
        start_id = 0

    if args.end_id is not None:
        end_id = int(args.end_id)
    else:
        end_id = sys.maxsize

    if args.fps_count is not None:
        fps_count_limit = int(args.fps_count)
    else:
        fps_count_limit = sys.maxsize

    enabled_debug = args.debug
    # if args.debug is not None:
    #     enabled_debug = True
    # else:
    #     enabled_debug = False

    with open(pcapngFilePath, 'rb') as fp:
        scanner = FileScanner(fp)
        firstBlock = True
        lastFrameTimestamp = None
        frameDataSize = 0
        fpsDp = []
        seqHistory = []
        packetId = 0
        fpsDpCount = 0
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

                if isinstance(_pl1, IP) and isinstance(_pl2, TCP) and _pl1.src == '73.202.58.231' and _pl1.dst == '192.168.0.16':

                    # Skip this packet if it was sent before
                    currentSeq = _pl2.seq
                    if currentSeq in seqHistory:
                        print("Skipped a redundant packet.")
                        continue
                    else:
                        seqHistory.append(currentSeq)

                    if b'--frame\r\nContent-Type: image/jpeg' in _pl2.payload.original:

                        currentFrameTimestamp = block.timestamp - timestampOrigin

                        if lastFrameTimestamp is not None:
                            currentFps = 1 / (currentFrameTimestamp - lastFrameTimestamp)

                            if currentFps > 100:
                                continue

                            fpsDpCount += 1
                            fpsDp.append(currentFps)

                            if enabled_debug:
                                print('FPS_DP #{}, Packet #{}) {:.2f}:\t{:.2f}\tfps\t{}\tbytes'.format(fpsDpCount, packetId, currentFrameTimestamp, currentFps, frameDataSize))

                            if fpsDpCount >= fps_count_limit:
                                break

                        lastFrameTimestamp = currentFrameTimestamp
                        frameDataSize = 0

                    frameDataSize += block.packet_len

        npFpsDp = np.array(fpsDp)
        meanFps = npFpsDp.mean()
        stdFps = npFpsDp.std()
        maxFps = npFpsDp.max()
        minFps = npFpsDp.min()
        cvFps = stdFps/meanFps

        print('Results for "{}":'.format(pcapngFilePath))
        print('  - Config:', end='')
        print('      {}'.format(args))
        # print('      packet ID range: {} ~ {}'.format(start_id, end_id))
        # print('      FPS data point count: {}'.format(fps_count_limit))
        print('  - FPS Results:', end='')
        print('      Mean: {:.2f}\t Std: {:.2f}\t CV:{:.2f}\t Max: {:.2f}\t Min: {:.2f}'.format(meanFps, stdFps, cvFps, maxFps, minFps))
        print('')
