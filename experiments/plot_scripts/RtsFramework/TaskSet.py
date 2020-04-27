import math

class TaskSet:
    id = 0
    tasks = []

    def __init__(self):
        pass

    def add(self, task):
        self.tasks.append(task)

    def getTaskByPriority(self, priority):
        for task in self.tasks:
            if task.priority == priority:
                return task
        return None

    def computeUtil(self):
        util = 0.0
        for task in self.tasks:
            util += task.wcet/task.period
        return util

    def getLargestFreq(self):
        maxFreq = 0;
        for task in self.tasks:
            maxFreq = max(maxFreq, task.frequency)
        return maxFreq

    # task priority starts with 1 (smallest), the larger the higher
    '''       
            switch (numTasks) {
            case 3:
                result[0] = 1; //observerTaskPriority = 1;
                result[1] = 3; //victimTaskPriority = 3;
                break;
            default:
                result[0] = (int) Math.floor((double) numTasks / 3.0) + 1; //observerTaskPriority = (int) Math.floor((double) numTasks / 3.0) + 1;
                result[1] = numTasks - (int) Math.floor((double) numTasks / 3); //victimTaskPriority = numTasks - (int) Math.floor((double) numTasks / 3);
                break;
            }
            //case 5: observerTaskPriority = 2; victimTaskPriority = 4; break;
            //case 7: observerTaskPriority = 3; victimTaskPriority = 5; break;
            //case 9: observerTaskPriority = 4; victimTaskPriority = 6; break;
            //case 11: observerTaskPriority = 4; victimTaskPriority = 8; break;
    '''
    def getScheduLeakObserverVictimTask(self):
        numTasks = len(self.tasks)
        if numTasks == 3:
            observerPriority = 1
            victimPriority = 3
        else:
            observerPriority = math.floor(numTasks/3) + 1
            victimPriority = numTasks - math.floor(numTasks/3)

        return self.getTaskByPriority(observerPriority), self.getTaskByPriority(victimPriority)


    ''' Example output:
    TaskSet(0.5474416457017328):
	    Task-2: p=970, c=103, pri=1, offset=472, f=10.31
	    Task-5: p=780, c=56, pri=2, offset=289, f=12.82
	    Task-3: p=640, c=78, pri=3, offset=561, f=15.63
	    Task-4: p=290, c=37, pri=4, offset=87, f=34.48
	    Task-1: p=100, c=12, pri=5, offset=66, f=100.00
	'''
    def toString(self):
        outString = "TaskSet({}):\n".format(self.computeUtil())
        for task in self.tasks:
            outString += "\t{}\n".format(task.toString() )
        return outString[:-1]   # With removing the last \n