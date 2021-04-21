## FPS Calculator
### How to run the auto scripts:
1. Download the package
2. Copy all the `.pcapng` files whose FPS values you would like to analyze
3. `cd` into the package's directory
4. Run the following three in order:
   1. `./1_build.sh` which builds an docker image that can run the FPS calculator program
   2. `./2_start.sh` which will start a container
   3. Run `./3_run.py` inside the started container
5. The textual results will be saved in the file `results.txt`

### How to run manually:
You can directly run the Python program `FPSCalculator_PerSecond.py` with arguments based on your needs:
```
usage: FPSCalculator_PerSecond.py [-h] -i INPUT [--src SRC] [--dst DST] [--start_id START_ID] [--end_id END_ID] [--duration DURATION] [--time TIME] [-d]

optional arguments:
  -h, --help            show this help message and exit
  --src SRC             Source IP address
  --dst DST             Destination IP address
  --start_id START_ID   Beginning packet ID to include in the analysis
  --end_id END_ID       Ending packet ID to include in the analysis
  --duration DURATION   Compute FPS based on 1 seond as a unit, for the specified seconds
  --time TIME           Time unit per FPS
  -d, --debug           Enable raw data messages

required arguments:
  -i INPUT, --input INPUT
                        Path to a Pcapng file
```

For example:
`python FPSCalculator_PerSecond.py -i pcapng-to-be-analyzed/lec_v1.pcapng --start_id=10 --duration=30 --time=0.5 -d`

---

## Bps Calculator
Bit rate can be calculated by using the Python program `PcapngBpsCalculator.py`:
```
usage: PcapngBpsCalculator.py [-h] -i INPUT [--src SRC] [--dst DST] [--start_id START_ID] [--end_id END_ID] [--duration DURATION] [--byte] [--format] [-d]

optional arguments:
  -h, --help            show this help message and exit
  --src SRC             Source IP address
  --dst DST             Destination IP address
  --start_id START_ID   Beginning packet ID to include in the analysis
  --end_id END_ID       Ending packet ID to include in the analysis
  --duration DURATION   Compute FPS based on 1 seond as a unit, for the specified seconds
  --byte                Calculate Bytes rather than the default bits, per second
  --format              Format the output value
  -d, --debug           Enable raw data messages

required arguments:
  -i INPUT, --input INPUT
                        Path to a Pcapng file
```

For example:
`python PcapngBpsCalculator.py -i pcapng-to-be-analyzed/lec_v1.pcapng -d`
