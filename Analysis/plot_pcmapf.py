import pandas as pd
import os
import math
import matplotlib.pyplot as plt
import shutil

PIC_BESTBOUND_DIR = 'plots/pic-bestbound'
CSV_BESTBOUND_DIR = 'plots/csv-bestbound'
PIC_RUNTIME_DIR = 'plots/pic-runtime'
CSV_RUNTIME_DIR = 'plots/csv-runtime'
MAPF_MARKERS = False
MAPF_MARKERS_ONLY_WHEN_PCS = False
LABEL_X_AXIS = False


'''
    this file should live with the same level with result folder
'''

'''
    Classify results into folders group by maps
'''


def classify_files(map_files):
    files = os.listdir('results/')
    for name in map_files:
        if not os.path.exists('results/' + name):
            os.makedirs('results/' + name)
        for filename in files:
            if name == filename:
                continue
            if name + '-' in filename:
                shutil.move('results/' + filename, 'results/' + name + '/' + filename)


'''
    plot the runtime in the paper
'''


def plot_rumtime(map_file):
    cbs = []
    pp = []
    pp_rand = []
    pcs = []
    pcs_plus = []

    files = os.listdir('results/' + map_file)

    for filename in files:
        csv_file = 'results/' + map_file + '/' + filename
        csv_data = pd.read_csv(csv_file)
        csv_df = pd.DataFrame(csv_data)
        if ('Solution Cost' not in csv_df.columns):
            csv_df['Solution Cost'] = math.nan
        for index, row in csv_df.iterrows():
            if (math.isnan(row['Solution Cost'])):
                if row['Solver'] == 'CBS' or row['Solver'] == 'PP-no-restarts' or row['Solver'] == 'PP-rand-AStar':
                    row['Elapsed Time (ms)'] = -2  # timeout
                    row['Runtime to First Solution'] = -2
                elif row['Elapsed Time (ms)'] <= 60000:
                    print('unsolve detect')
                    row['Elapsed Time (ms)'] = -1  # unsolved
                    row['Runtime to First Solution'] = -1
                else:
                    row['Elapsed Time (ms)'] = -2  # timeout
                    row['Runtime to First Solution'] = -2
            if (row['Elapsed Time (ms)'] == -2):
                continue
            if row['# Agents'] == 5:  # ignore 5 agents
                continue
            if (row['Solver'] == 'CBS'):
                cbs.append(row['Elapsed Time (ms)'])
            if (row['Solver'] == 'PP-no-restarts'):
                pp.append(row['Elapsed Time (ms)'])
            if (row['Solver'] == 'PP-rand-AStar'):
                pp_rand.append(row['Runtime to First Solution'])
            if (row['Solver'] == 'PCS'):
                pcs.append(row['Elapsed Time (ms)'])
            if (row['Solver'] == 'PCS_SIPPH'):
                pcs_plus.append(row['Elapsed Time (ms)'])

    threshold = max(cbs + pp + pp_rand + pcs + pcs_plus)  # for pretty plotting purpose

    # sort by runtime, then index becomes the accumulated number of instance
    cbs.sort()
    pp.sort()
    pp_rand.sort()
    pcs.sort()
    pcs_plus.sort()

    # for marker purpose (only mark some of the nodes based on y)
    cbs_markders_on = []
    pp_markders_on = []
    pp_rand_markders_on = []
    pcs_markders_on = []
    pcs_plus_markders_on = []

    markers_pmin = 5
    start = 0
    cnt = int(len(cbs) / markers_pmin + 1)
    while start < len(cbs):
        cbs_markders_on.append(start)
        start += cnt
    if (cbs_markders_on != len(cbs) - 1):
        cbs_markders_on.append(len(cbs) - 1)

    start = 0
    cnt = int(len(pp) / markers_pmin + 1)
    while start < len(pp):
        pp_markders_on.append(start)
        start += cnt
    if (pp_markders_on != len(pp) - 1):
        pp_markders_on.append(len(pp) - 1)

    start = 0
    cnt = int(len(pp_rand) / markers_pmin + 1)
    while start < len(pp):
        pp_rand_markders_on.append(start)
        start += cnt
    if (pp_rand_markders_on != len(pp_rand) - 1):
        pp_rand_markders_on.append(len(pp_rand) - 1)

    start = 0
    cnt = int(len(pcs) / markers_pmin + 1)
    while start < len(pcs):
        pcs_markders_on.append(start)
        start += cnt
    if (pcs_markders_on != len(pcs) - 1):
        pcs_markders_on.append(len(pcs) - 1)

    start = 0
    cnt = int(len(pcs_plus) / markers_pmin + 1)
    while start < len(pcs_plus):
        pcs_plus_markders_on.append(start)
        start += cnt
    if (pcs_plus_markders_on != len(pcs_plus) - 1):
        pcs_plus_markders_on.append(len(pcs_plus) - 1)

    # plot
    plt.figure(figsize=(7, 4))
    plt.plot(range(1, len(pp) + 1), pp, linestyle='--', linewidth=2.5, marker='X', markerfacecolor='none',
             color='green', markevery=pp_markders_on, label='PP', markersize=9)
    plt.plot(range(1, len(pp_rand) + 1), pp_rand, linestyle='-.', marker='<', markerfacecolor='none', color='orange',
             markevery=pp_rand_markders_on, linewidth=2.5, label='PPR$^*$', markersize=9)
    plt.plot(range(1, len(pcs) + 1), pcs, linestyle='dashed', marker='P', markerfacecolor='none',
             markevery=pcs_markders_on, linewidth=2.5, color='crimson', label='PCS-H1', markersize=9)
    plt.plot(range(1, len(pcs_plus) + 1), pcs_plus, linestyle=(0, (3, 1, 1, 1, 1, 1)), marker='*',
             markerfacecolor='none', color='purple', markevery=pcs_plus_markders_on, linewidth=2.5, label='PCS-H2',
             markersize=9)
    plt.yticks([0, 15000, 30000, 45000, 60000], ['0', '15', '30', '45', '60'], fontsize=20)
    plt.ylabel('Runtime(s)', fontsize=20)
    plt.title(map_file, fontsize=20)
    plt.ylim(-2000, 60000)
    plt.xlabel('Coverage up to 60s', fontsize=20)
    plt.xticks(fontsize=20)
    if not os.path.exists(PIC_RUNTIME_DIR):
        os.makedirs(PIC_RUNTIME_DIR)
    plt.savefig(PIC_RUNTIME_DIR + '/' + map_file + '.pdf', bbox_inches='tight')
    plt.show()

    # output as csv
    df = pd.DataFrame(list(zip(pp, pp_rand, pcs, pcs_plus)),
                      columns=['PP', 'PPR', 'PCS', 'PCS_SIPPH'])
    df.sort_index(inplace=True)
    if not os.path.exists(CSV_RUNTIME_DIR):
        os.makedirs(CSV_RUNTIME_DIR)
    df.to_csv(CSV_RUNTIME_DIR + '/' + map_file + '.csv', index=True)

    # output the plot data to csv
    df = pd.DataFrame(index=range(1, 176))
    for name, xs, ys in zip(['PP', 'PPR', 'PCS', 'PCS_SIPPH'],
                            [range(1, len(pp) + 1), range(1, len(pp_rand) + 1),
                             range(1, len(pcs) + 1), range(1, len(pcs_plus) + 1)],
                            [pp, pp_rand, pcs, pcs_plus]):
        for x, y in zip(xs, ys):
            df.loc[x, name] = y
    df.sort_index(inplace=True)
    if not os.path.exists(CSV_RUNTIME_DIR):
        os.makedirs(CSV_RUNTIME_DIR)
    df.to_csv(CSV_RUNTIME_DIR + '/' + map_file + '.csv', index=True)


'''
    plot the bestbound plot in the paper
    the trackder bounds folder should live with the same level of results
'''


def plot_bestbound(map_file):
    mapf = dict()
    pp = dict()
    pp_rand = dict()
    pcs = dict()
    pcs_plus = dict()

    # for x value
    xmapf = []
    xpp = []
    xpp_rand = []
    xpcs = []
    xpcs_plus = []

    files = os.listdir('results/' + map_file)

    for filename in files:
        # read tracker file
        tracker_df = pd.DataFrame(pd.read_csv('tracker_bounds/' + map_file + '.csv'))
        print(filename)
        csv_file = 'results/' + map_file + '/' + filename
        csv_data = pd.read_csv(csv_file)
        csv_df = pd.DataFrame(csv_data)
        scen_no = int((filename.split(' ')[1]).split('-')[-1].split('.')[0])
        if ('Solution Cost' not in csv_df.columns):
            csv_df['Solution Cost'] = math.nan
        optimal_bound = {}

        agents = set()
        for index, row in csv_df.iterrows():
            if row['Solver'] == 'PCS' or row['Solver'] == 'PCS_SIPPH':
                if (math.isnan(row['Solution Cost'])):
                    continue
                optimal_bound[row['# Agents']] = row['Solution Cost']
        for index, row in csv_df.iterrows():
            # mark timeout and unsolved
            if (math.isnan(row['Solution Cost'])):
                if row['Solver'] == 'CBS' or row['Solver'] == 'PP-no-restarts' or row['Solver'] == 'PP-rand-AStar':
                    row['Solution Cost'] = -2  # timeout
                elif row['Elapsed Time (ms)'] <= 60000:
                    row['Solution Cost'] = -1  # unsolved
                else:
                    row['Solution Cost'] = -2  # timeout

            if row['# Agents'] == 5:  # ignore 5 agents
                continue

            mapf_cost = tracker_df[(tracker_df['scen_type'] == 'even') & (tracker_df['type_id'] == scen_no) & (
                        tracker_df['agents'] == row['# Agents'])]['lower_cost'].to_list()[0]

            if row['# Agents'] in optimal_bound.keys():
                best_bound = optimal_bound[row['# Agents']]
            else:
                best_bound = mapf_cost

            # mapf solutions
            if (row['# Agents'] not in agents):
                cost = mapf_cost
                print('mapf cost', mapf_cost)
                if (cost >= 0):
                    cost = cost / best_bound
                if best_bound not in mapf.keys():
                    mapf[best_bound] = []
                mapf[best_bound] += [cost]
                agents.add(row['# Agents'])

            if (row['Solver'] == 'PP-no-restarts'):
                cost = row['Solution Cost']
                if (cost >= 0):
                    cost = cost / best_bound
                if best_bound not in pp.keys():
                    pp[best_bound] = []
                pp[best_bound] += [cost]

            if (row['Solver'] == 'PP-rand-AStar'):
                cost = row['Solution Cost']
                if (cost >= 0):
                    cost = cost / best_bound
                if best_bound not in pp_rand.keys():
                    pp_rand[best_bound] = []
                pp_rand[best_bound] += [cost]

            if (row['Solver'] == 'PCS'):
                cost = row['Solution Cost']
                if (cost >= 0):
                    cost = cost / best_bound
                if best_bound not in pcs.keys():
                    pcs[best_bound] = []
                pcs[best_bound] += [cost]

            if (row['Solver'] == 'PCS_SIPPH'):
                cost = row['Solution Cost']
                if (cost >= 0):
                    cost = cost / best_bound
                if best_bound not in pcs_plus.keys():
                    pcs_plus[best_bound] = []
                pcs_plus[best_bound] += [cost]

    sics = list(mapf.keys())
    sics.sort()  # sort based on best bound (or sic)
    ymapf = []
    ypp = []
    ypp_rand = []
    ypcs = []
    ypcs_plus = []

    for i in sics:
        for j in mapf[i]:
            if j == -2:  # we skip the timeouts
                continue
            ymapf.append(j)
            xmapf.append(i)
        for j in pp[i]:
            if j == -2:
                continue
            ypp.append(j)
            xpp.append(i)
        for j in pp_rand[i]:
            if j == -2:
                continue
            ypp_rand.append(j)
            xpp_rand.append(i)
        for j in pcs[i]:
            if j == -2:
                continue
            ypcs.append(j)
            xpcs.append(i)
        for j in pcs_plus[i]:
            if j == -2:
                continue
            ypcs_plus.append(j)
            xpcs_plus.append(i)

    # for pretty plotting
    threshold = max(ymapf + ypp + ypp_rand + ypcs + ypcs_plus) if MAPF_MARKERS \
        else max(ypp + ypp_rand + ypcs + ypcs_plus)
    if threshold > 2:
        cnt = round(threshold - 1) / 5
    elif threshold > 1:
        cnt = round((threshold - 1) / 5, 2)

    if not MAPF_MARKERS:
        yticks = [1]
    else:
        yticks = [round(min(ymapf + ypp + ypp_rand + ypcs + ypcs_plus), 2)]
        if 1 - yticks[0] < cnt:
            yticks[0] = 1

    while yticks[-1] < round(threshold, 2):
        yticks.append(round(yticks[-1] + cnt, 2))
    us = (yticks[-1] + 0.4 * cnt)
    yticks = yticks[:-1]
    ymapf = [us if x == -1 else x for x in ymapf]
    ypp = [us if x == -1 else x for x in ypp]
    ypp_rand = [us if x == -1 else x for x in ypp_rand]
    ypcs = [us if x == -1 else x for x in ypcs]
    ypcs_plus = [us if x == -1 else x for x in ypcs_plus]
    lower = min(ymapf + ypp + ypp_rand + ypcs + ypcs_plus) if MAPF_MARKERS \
        else min(ypp + ypp_rand + ypcs + ypcs_plus)

    # plot
    plt.figure(figsize=(6, 5))
    if MAPF_MARKERS:
        if MAPF_MARKERS_ONLY_WHEN_PCS:
            data = [(x, y) for x, y in zip(xmapf, ymapf) if x in [x for x, y in zip(xpcs_plus, ypcs_plus) if y < us]]
            plt.scatter([x for x, y in data], [y for x, y in data], marker='o', c='none', edgecolors='blue',
                        label='MAPF', s=50)
        else:
            plt.scatter(xmapf, ymapf, marker='o', c='none', edgecolors='blue', label='MAPF', s=50)
    plt.scatter(xpp, ypp, marker='x', c='green', label='PP', s=50)
    plt.scatter(xpp_rand, ypp_rand, marker='<', c='none', edgecolors='orange', label='PPR$^*$', s=50)
    plt.scatter(xpcs_plus, ypcs_plus, marker='*', c='none', edgecolors='purple', label='PCS', s=70)
    plt.axhline(y=us - 0.1 * cnt, color=(0, 0, 0, 0.5), ls='--')  # unsolvable line
    plt.grid(ls=':', alpha=0.9)
    plt.yticks(yticks + [us - 0.1 * cnt], yticks + ['US'], fontsize=20)
    plt.xticks(fontsize=20)
    plt.ylabel('SOC/Best Bound', fontsize=20)
    plt.ylim(lower * 0.99, us + 0.2 * cnt)
    if LABEL_X_AXIS:
        plt.xlabel('Best Bound', fontsize=20)
    plt.title(map_file, fontsize=20)

    plt.tight_layout()
    plt.locator_params(axis='x', nbins=5)
    if not os.path.exists(PIC_BESTBOUND_DIR):
        os.makedirs(PIC_BESTBOUND_DIR)
    plt.savefig(PIC_BESTBOUND_DIR + '/' + map_file + '.pdf', bbox_inches='tight')
    plt.show()

    # output the plot data to csv
    all_xs = set(xmapf + xpp + xpp_rand + xpcs + xpcs_plus)
    df = pd.DataFrame(index=all_xs)
    for name, xs, ys in zip(['ymapf', 'ypp', 'ypp_rand' 'ypcs','ypcs_plus'], [xmapf, xpp, xpp_rand, xpcs, xpcs_plus],
                            [ymapf, ypp, ypp_rand, ypcs, ypcs_plus]):
        for x, y in zip(xs, ys):
            df.loc[x, name] = y
    df.sort_index(inplace=True)
    if not os.path.exists(CSV_BESTBOUND_DIR):
        os.makedirs(CSV_BESTBOUND_DIR)
    df.to_csv(CSV_BESTBOUND_DIR + '/' + map_file + '.csv', index=True)


map_name = ['brc202d', 'den312d', 'den520d', 'empty-8-8', 'empty-16-16', 'empty-32-32', 'empty-48-48',
            'ht_chantry', 'ht_mansion_n', 'lak303d', 'lt_gallowstemplar_n', 'maze-32-32-2', 'maze-32-32-4',
            'maze-128-128-1', 'maze-128-128-2', 'maze-128-128-10', 'ost003d', 'random-32-32-10', 'random-32-32-20',
            'random-64-64-10', 'random-64-64-20', 'room-32-32-4', 'room-64-64-8', 'room-64-64-16', 'w_woundedcoast',
            'warehouse-10-20-10-2-1', 'warehouse-10-20-10-2-2', 'warehouse-20-40-10-2-1', 'warehouse-20-40-10-2-2',
            'Berlin_1_256', 'Boston_0_256', 'orz900d', 'Paris_1_256']

# first create folder of the given map name and classify results into different floders
classify_files(map_name)

# plot the best bound in the paper
for n in map_name:
    plot_bestbound(n)
# plot the runtime in the paper
for n in map_name:
    plot_rumtime(n)
