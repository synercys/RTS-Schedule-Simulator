class Task:
    id = 0
    type = "IDLE"
    name = "IDLE"
    phase = 0
    period = 0
    deadline = 0
    wcet = 0
    priority = 0
    arrivalType = "periodic"
    '''
    "phase": 0,
    "period": 0,
    "arrivalType": "periodic",
    "name": "IDLE",
    "id": 0,
    "type": "IDLE",
    "deadline": 0,
    "wcet": 0,
    "priority": 0
    '''
    def __init__(self, id, type, name, phase, period, deadline, wcet, priority, arrivalType):
        self.id = id
        self.type = type
        self.name = name
        self.phase = phase
        self.period = period
        self.deadline = deadline
        self.frequency = 1.0/period
        self.wcet = wcet
        self.priority = priority
        self.arrivalType = arrivalType

    def toString(self):
        # Task - 2: p = 970, c = 103, pri = 1, offset = 472, f = 10.31
        return "Task-{}: p = {:.5f}, c = {:.5f}, pri = {}, offset = {}, f = {:.3f}".format(self.id, self.period, self.wcet, self.priority, self.phase, 1.0/self.period)
