import pandas as pd
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
from matplotlib.ticker import FormatStrFormatter
import numpy as np
import json
import os

# import data
with open('{}\\{}'.format( os.getcwd(), 'result.json')) as f:
    json_data = json.load(f)
width_in_inches = 10 #330
height_in_inches = 7
dots_per_inch = 66
plt.rcParams.update({'font.size': 24})

y = [[0 for x in range(4)] for y in range(5)]
for i in range(5):
    for j in range(4):
        y[i][j] = float(json_data[i*4+j]["averageFlowtime"])
        print('i = {}, j = {}, value = {}'.format(i, j, json_data[i*4+j]["averageFlowtime"]))

x = ['1', '10', '100', '1000']
labels = ['FIFO', 'SVF', 'EDF', 'STRF', 'Proposed']
color = ["#1F78B4", "#33A02C", "#FF7F00", "#FB9A99", "#CAB2D6"]
marker = ['o', 'x', '^', '>', '<']
i = 0

fig = plt.figure(
    figsize=(width_in_inches, height_in_inches),
    dpi=dots_per_inch
)
for label in labels:
    plt.plot(x, y[i], label=label, color=color[i], marker=marker[i], ms=16)
    i = i + 1
axes = fig.gca()
# x_major_ticks = np.arange(0, 110, 10)
# x_minor_ticks = np.arange(0, 110, 5)
# y_major_ticks = np.arange(0, 1.1, 0.1)
# y_minor_ticks = np.arange(0, 1.1, 0.05)
# axes.set_xticks(x_major_ticks)
# axes.set_xticks(x_minor_ticks, minor=True)
# axes.set_yticks(y_major_ticks)
# axes.set_yticks(y_minor_ticks, minor=True)
# axes.grid(which='both')
# axes.grid(which='minor', alpha=0.2)
# axes.grid(which='major', alpha=0.5)
# axes.set_xlim([0,110])
axes.yaxis.set_major_formatter(FormatStrFormatter('%.2f'))
axes.set_xlabel('Total number of servers')
# axes.set_ylim([0,1.1])

axes.set_ylabel('Average Flowtime, $\overline{p}$ \n(Time unit)')
plt.legend(ncol=2,prop={'size': 24})
plt.tight_layout()
plt.show()


plt.rcParams.update({'font.size': 24})
y2 = [[0 for x in range(4)] for y in range(5)]
for i in range(5):
    for j in range(4):
        y2[i][j] = float(json_data[i*4+j]["variationFlowtime"])/1000.0
        print('i = {}, j = {}, value = {}'.format(i, j, float(json_data[i*4+j]["variationFlowtime"])/1000.0))
i = 0
fig = plt.figure(
    figsize=(width_in_inches, height_in_inches),
    dpi=dots_per_inch
)
for label in labels:
    plt.plot(x, y2[i], label=label, color=color[i], marker=marker[i], ms=16)
    i = i + 1
axes = fig.gca()
# axes.yaxis.set_major_formatter(FormatStrFormatter('%.2f'))
axes.set_xlabel('Total number of servers')
axes.set_ylabel('Variation of Flowtime, $\sigma_p$ \n($10^3$)')
plt.legend(ncol=2,prop={'size': 24})
plt.tight_layout()
plt.show()


plt.rcParams.update({'font.size': 24})
y3 = [[0 for x in range(4)] for y in range(5)]
for i in range(5):
    for j in range(4):
        y3[i][j] = float(json_data[i*4+j]["reliability"])*100.0
        print('i = {}, j = {}, value = {}'.format(i, j, float(json_data[i*4+j]["reliability"])*100.0))
i = 0
fig = plt.figure(
    figsize=(width_in_inches, height_in_inches),
    dpi=dots_per_inch
)
for label in labels:
    plt.plot(x, y3[i], label=label, color=color[i], marker=marker[i], ms=16)
    i = i + 1
axes = fig.gca()
# axes.yaxis.set_major_formatter(FormatStrFormatter('%.2f'))
axes.set_xlabel('Total number of servers')
axes.set_ylabel('Reliability, $\overline{r}$ (%)')
plt.legend(ncol=2,prop={'size': 24})
plt.tight_layout()
plt.show()


plt.rcParams.update({'font.size': 24})
y4 = [[0 for x in range(4)] for y in range(5)]
for i in range(5):
    for j in range(4):
        y4[i][j] = float(json_data[i*4+j]["maxFlowtime"])
        print('i = {}, j = {}, value = {}'.format(i, j, float(json_data[i*4+j]["maxFlowtime"])))
i = 0
fig = plt.figure(
    figsize=(width_in_inches, height_in_inches),
    dpi=dots_per_inch
)
for label in labels:
    plt.plot(x, y4[i], label=label, color=color[i], marker=marker[i], ms=16)
    i = i + 1
axes = fig.gca()
# axes.yaxis.set_major_formatter(FormatStrFormatter('%.2f'))
axes.set_xlabel('Total number of servers')
axes.set_ylabel('Max. Flowtime \n(Time Unit)')
plt.legend(ncol=2,prop={'size': 24})
plt.tight_layout()
plt.show()