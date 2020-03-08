class TaskSet:
    id = 0
    tasks = []

    def __init__(self):
        pass

    def add(self, task):
        self.tasks.append(task)

    def computeUtil(self):
        util = 0.0
        for task in self.tasks:
            util += task.wcet/task.period
        return util

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