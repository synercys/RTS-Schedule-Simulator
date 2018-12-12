# Real-time Task Generator and Schedule Simulator

## Command - _rtaskgen_
- Usage
```
Usage: rtaskgen [-hV] [-i=<taskInputFile>] [-n=<taskSize>]
                [-o=<outputFilePrefix>] [-r=<tasksetFileToBeReadAndPrinted>]
  -h, --help                  Display this help message.
  -i, --in=<taskInputFile>    A file that contains task configurations.
  -n, --size=<taskSize>       The number of tasks in a task set. It is ignored
                                when a configuration file is specified (-i).
  -o, --out=<outputFilePrefix>
                              A file for storing generated task sets.
  -r, --read=<tasksetFileToBeReadAndPrinted>
                              A taskset file to be read and printed. This
                                option ignores other options.
  -V, --version               Display version info.
```
- Example
1. A minimal use (which generates a taskset of 5 tasks):
```
out/bin/rtaskgen 
```
_Example output:_
```
#TaskSet 1:
TaskSet(0.5110049327562939):
	Task-3: p=950, c=98, pri=1, offset=466, f=10.53
	Task-1: p=780, c=44, pri=2, offset=564, f=12.82
	Task-5: p=290, c=27, pri=3, offset=181, f=34.48
	Task-4: p=140, c=14, pri=4, offset=68, f=71.43
	Task-2: p=120, c=19, pri=5, offset=23, f=83.33
```

2. Use an existing configuration file to generate tasksets:
```
out/bin/rtaskgen -i sampleLogs/100tasks_per_condition.rttaskgen -o sampleLogs/output.tasksets
```

3. Read and print a taskset in a readable way:
```
out/bin/rtaskgen -r sampleLogs/5tasks.tasksets
```

- Configuration File (*.rttaskgen)
```
{
    "formatVersion":"12",
    "dataType":"rtTaskGen settings",
    "data":{
        "tickUnitInNs":100000,
        "configs":[
            {
                "numTaskPerSet":1,
                "numTaskSet":1,
                "maxHyperPeriod":30000,
                "minPeriod":100,
                "maxPeriod":1000,
                "minWcet":1,
                "maxWcet":500,
                "minInitOffset":0,
                "maxInitOffset":30000,
                "minUtil":0.1,
                "maxUtil":1,
                "generateFromHpDivisors":false,
                "nonHarmonicOnly":true,
                "needGenObserverTask":false,
                "maxObservationRatio":999,
                "minObservationRatio":1,
                "observerTaskPriority":1,
                "victimTaskPriority":2
            }
        ]
    }
}
```
- Taskset Output (*.tasksets)
```
{
   "data":{
      "tickUnitInNs":100000,
      "tasksets":[
         {
            "id":0,
            "tasks":[
               {
                  "phase":0,
                  "period":0,
                  "arrivalType":"periodic",
                  "name":"IDLE",
                  "id":0,
                  "type":"IDLE",
                  "deadline":0,
                  "wcet":0,
                  "priority":0
               },
               {
                  "phase":43,
                  "period":610,
                  "arrivalType":"periodic",
                  "name":"APP1",
                  "id":1,
                  "type":"APP",
                  "deadline":610,
                  "wcet":124,
                  "priority":2
               },
               {
                  "phase":17,
                  "period":110,
                  "arrivalType":"periodic",
                  "name":"APP2",
                  "id":2,
                  "type":"APP",
                  "deadline":110,
                  "wcet":20,
                  "priority":5
               },
               {
                  "phase":566,
                  "period":630,
                  "arrivalType":"periodic",
                  "name":"APP3",
                  "id":3,
                  "type":"APP",
                  "deadline":630,
                  "wcet":82,
                  "priority":1
               },
               {
                  "phase":151,
                  "period":230,
                  "arrivalType":"periodic",
                  "name":"APP4",
                  "id":4,
                  "type":"APP",
                  "deadline":230,
                  "wcet":29,
                  "priority":4
               },
               {
                  "phase":215,
                  "period":340,
                  "arrivalType":"periodic",
                  "name":"APP5",
                  "id":5,
                  "type":"APP",
                  "deadline":340,
                  "wcet":94,
                  "priority":3
               }
            ]
         }
      ]
   },
   "dataType":"single taskset",
   "formatVersion":"14"
}
```

## Command - _rtsim_
- Usage
```
Usage: rtsim [-bhvV] -d=<simDuration> -i=<taskInputFile>
             [-l=<optionLadderDiagramWidth>] -p=<schedulingPolicy>
             [-o=<outputFilePathAndFormat>]...
  -b, --bibs                  Output busy intervals as binary string.
  -d, --duration=<simDuration>
                              Simulation duration in 0.1ms (e.g., 10 is 1ms).
  -h, --help                  Display this help message.
  -i, --in=<taskInputFile>    A file that contains taskset parameters.
  -l, --ladder=<optionLadderDiagramWidth>
                              Applicable for xlsx format. Width of a ladder
                                diagram.
  -o, --out=<outputFilePathAndFormat>
                              File names (including their formats) for schedule
                                simulation output. The output format is
                                determined by the given file extension: ".
                                xlsx", ".txt", ".rtschedule".
  -p, --policy=<schedulingPolicy>
                              Scheduling policy ("EDF" or "RM").
  -v, --evar                  Enable execution time variation.
  -V, --version               Display version info.
```
- Schedule Output Format (*.rtschedule)
```
{
   "data":{
      "taskSet":{
         "id":0,
         "tasks":[
            {
               "phase":0,
               "period":0,
               "arrivalType":"periodic",
               "name":"IDLE",
               "id":0,
               "type":"IDLE",
               "deadline":0,
               "wcet":0,
               "priority":0
            },
            {
               "phase":564,
               "period":780,
               "arrivalType":"periodic",
               "name":"APP1",
               "id":1,
               "type":"APP",
               "deadline":780,
               "wcet":44,
               "priority":2
            },
            {
               "phase":23,
               "period":120,
               "arrivalType":"periodic",
               "name":"APP2",
               "id":2,
               "type":"APP",
               "deadline":120,
               "wcet":19,
               "priority":5
            },
            {
               "phase":466,
               "period":950,
               "arrivalType":"periodic",
               "name":"APP3",
               "id":3,
               "type":"APP",
               "deadline":950,
               "wcet":98,
               "priority":1
            },
            {
               "phase":68,
               "period":140,
               "arrivalType":"periodic",
               "name":"APP4",
               "id":4,
               "type":"APP",
               "deadline":140,
               "wcet":14,
               "priority":4
            },
            {
               "phase":181,
               "period":290,
               "arrivalType":"periodic",
               "name":"APP5",
               "id":5,
               "type":"APP",
               "deadline":290,
               "wcet":27,
               "priority":3
            }
         ]
      },
      "taskInstantEvents":[

      ],
      "tickUnitInNs":100000,
      "scheduleIntervalEvents":[
         {
            "endState":"end",
            "beginState":"start",
            "end":42,
            "begin":23,
            "taskId":2
         },
         {
            "endState":"end",
            "beginState":"start",
            "end":82,
            "begin":68,
            "taskId":4
         },
         {
            "endState":"end",
            "beginState":"start",
            "end":162,
            "begin":143,
            "taskId":2
         },
         {
            "endState":"end",
            "beginState":"start",
            "end":208,
            "begin":181,
            "taskId":5
         },
         {
            "endState":"end",
            "beginState":"start",
            "end":222,
            "begin":208,
            "taskId":4
         },
      "schedulingPolicy":"edf"
   },
   "dataType":"rtSim raw schedule",
   "formatVersion":"14"
}
```
- Example
1. A minimal use:
```
out/bin/rtsim -i sampleLogs/5tasks.tasksets -p EDF -d 1000
```
