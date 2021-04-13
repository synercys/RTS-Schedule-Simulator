#!/usr/bin/env python
##!/usr/bin/env bash

import os

# python PcapngFPSCalculator.py -i lec_v1.pcapng
# python PcapngFPSCalculator.py -i lec_v1.pcapng --start_id=10
# python PcapngFPSCalculator.py -i lec_v1.pcapng --start_id=10 --end_id=100 --fps_count=100 -d

firstFile = True
for file in os.listdir("./"):
    if file.endswith(".pcapng"):
        print('Parsing "{}" ...'.format(os.path.join("./", file)))

        cmd = 'python PcapngFPSCalculator.py -i {} --start_id=10 --fps_count=100 > results.txt'.format(file)

        if firstFile:
            firstFile = False
        else:
            cmd = cmd.replace('>', '>>')

        os.system(cmd)
