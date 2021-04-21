#!/usr/bin/env python
##!/usr/bin/env bash

import os

# python PcapngFPSCalculator.py -i lec_v1.pcapng
# python PcapngFPSCalculator.py -i lec_v1.pcapng --start_id=10
# python PcapngFPSCalculator.py -i lec_v1.pcapng --start_id=10 --end_id=100 --fps_count=100 -d

fileFolderPath = "./pcapng-to-be-analyzed/"
firstFile = True
for file in os.listdir(fileFolderPath):
    if file.endswith(".pcapng"):
        file = os.path.join(fileFolderPath, file)
        print('Parsing "{}" ...'.format(file))

        # cmd = 'python PcapngFPSCalculator.py -i {} --start_id=100 --fps_count=100 > results.txt'.format(file)
        cmd = 'python FPSCalculator_PerSecond.py -i {} --start_id=10 --duration=30 --time=0.5 > results.txt'.format(file)

        if firstFile:
            firstFile = False
        else:
            cmd = cmd.replace('>', '>>')

        os.system(cmd)
