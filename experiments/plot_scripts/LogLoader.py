import json
from RtsFramework.Task import Task
from RtsFramework.TaskSet import TaskSet


def loadRawSchedule(fileName):
    rawSchedule = []
    file = open(fileName, "r")
    lines = file.readlines()
    for line in lines:
        for char in line:
            if char == "0":
                rawSchedule.append(0)
            elif char == "1":
                rawSchedule.append(1)
    return rawSchedule

def loadJsonScheduleAsBinaryRawSchedule(fileName):
    tickCounter = 0
    rawSchedule = []
    with open(fileName, 'r') as f:
        jsonSchedule = json.load(f)
        for interval in jsonSchedule['data']['scheduleIntervalEvents']:
            #TODO:  we didn't check if intervals in the log schedule are in order.
            rawSchedule.extend([0]*(interval['begin']-tickCounter))
            rawSchedule.extend([1]*(interval['end']-interval['begin']))
            tickCounter = interval['end']
    return rawSchedule

def loadTaskSetFromJsonFile(fileName):
    with open(fileName, 'r') as f:
        jsonTaskSets = json.load(f)
        timeToSecMultiplier = int(jsonTaskSets['data']['tickUnitInNs'])/1000_000_000
        jsonTaskSet = jsonTaskSets['data']['tasksets'][0]
        taskSet = TaskSet()
        for jsonTask in jsonTaskSet['tasks']:
            if jsonTask['type'] == 'IDLE':
                continue
            task = Task(jsonTask['id'], jsonTask['type'], jsonTask['name'], jsonTask['phase'], jsonTask['period']*timeToSecMultiplier, jsonTask['deadline']*timeToSecMultiplier, jsonTask['wcet']*timeToSecMultiplier, jsonTask['priority'], jsonTask['arrivalType'])
            taskSet.add(task)
        return taskSet

def loadSingleTaskSetFromJsonString(jsonStr):
    jsonTaskSets = json.loads(jsonStr)
    timeToSecMultiplier = int(jsonTaskSets['data']['tickUnitInNs'])/1000_000_000
    jsonTaskSet = jsonTaskSets['data']['taskSet']
    taskSet = TaskSet()
    for jsonTask in jsonTaskSet['tasks']:
        if jsonTask['type'] == 'IDLE':
            continue
        task = Task(jsonTask['id'], jsonTask['type'], jsonTask['name'], jsonTask['phase'], jsonTask['period']*timeToSecMultiplier, jsonTask['deadline']*timeToSecMultiplier, jsonTask['wcet']*timeToSecMultiplier, jsonTask['priority'], jsonTask['arrivalType'])
        taskSet.add(task)
    return taskSet

def loadSingleTaskSetFromJsonObject(jsonRoot):
    timeToSecMultiplier = int(jsonRoot['data']['tickUnitInNs'])/1000_000_000
    jsonTaskSet = jsonRoot['data']['taskSet']
    taskSet = TaskSet()
    for jsonTask in jsonTaskSet['tasks']:
        if jsonTask['type'] == 'IDLE':
            continue
        task = Task(jsonTask['id'], jsonTask['type'], jsonTask['name'], jsonTask['phase'], jsonTask['period']*timeToSecMultiplier, jsonTask['deadline']*timeToSecMultiplier, jsonTask['wcet']*timeToSecMultiplier, jsonTask['priority'], jsonTask['arrivalType'])
        taskSet.add(task)
    return taskSet
