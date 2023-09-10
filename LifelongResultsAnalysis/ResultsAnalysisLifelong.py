import glob
import os
from dataclasses import dataclass, field
from statistics import median
from typing import Optional

import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
from datetime import datetime
import itertools
from matplotlib.axes import Axes
from matplotlib.figure import Figure
# https://github.com/Phlya/adjustText/wiki
from adjustText import adjust_text


PRINT = 1
EXTRA_METRICS = True
FAIL_ON_ERROR = True
FIG_FORMATS = ["png", "pdf"]

MIN_SAMPLES = 10

DEF_FIG_WIDTH, DEF_FIG_HEIGHT = 6, 4
plt.rcParams['figure.figsize'] = [20, 15]
plt.rc('font', **{'family': 'DejaVu Sans',
                  'weight': 'bold',
                  # 'size'   : 13
                  })
DEJAVU_MONO = {'family': 'DejaVu Sans Mono', 'weight': 'bold'}

# plt.style.use('tableau-colorblind10')

# class Cols:
# col names
SOLVED_BY_ALL = 'Solved By All'
COL_INSTANCE_NAME = 'Instance Name'
COL_SOLVER = 'Solver'
COL_SOLVED = 'Solved'
COL_SKIPPED = "Skipped"
COL_NUM_AGENTS = '# Agents'
COL_VALID = 'Valid'
COL_MAP_NAME = 'Map Name'
COL_ELAPSED_TIME = "Elapsed Time (ms)"
COL_LOW_LEVEL_RUN_TIME = "Total Low Level Time (ms)"
COL_PROCESSOR = "Processor Info"
COL_SOC = 'SOC'
COL_MAKESPAN = 'makespan'
AVG_THROUGHPUT = 'averageThroughput'
AVG_INDV_THROUGHPUT = 'averageIndividualThroughput'
TIME_TO_50 = 'timeTo50%Completion'
TIME_TO_80 = 'timeTo80%Completion'
THROUGHPUT_AT_25 = 'throughputAtT25'
THROUGHPUT_AT_50 = 'throughputAtT50'
THROUGHPUT_AT_75 = 'throughputAtT75'
THROUGHPUT_AT_100 = 'throughputAtT100'
THROUGHPUT_AT_150 = 'throughputAtT150'
THROUGHPUT_AT_200 = 'throughputAtT200'
THROUGHPUT_AT_250 = 'throughputAtT250'
THROUGHPUT_AT_300 = 'throughputAtT300'
THROUGHPUT_AT_400 = 'throughputAtT400'
THROUGHPUT_AT_500 = 'throughputAtT500'
COL_TOTAL_ASTAR_RUNTIME = 'totalAStarRuntimeMS'
COL_TOTAL_ASTAR_CALLS = 'totalAStarCalls'
COL_AVG_ASTAR_RUNTIME = 'avgAStarRuntimeMS'
COL_GRAPH_DEGREE = 'Avg In Degree'
COL_GRAPH_VERTICES = '# Traversable Locations'
COL_MANAGEMENT_OVERHEAD_RUNTIME_PERCENT = "ManagementOverheadRuntime(%)"

RELEVANT_COLUMNS = [COL_SOLVER, COL_NUM_AGENTS, THROUGHPUT_AT_25, THROUGHPUT_AT_50, THROUGHPUT_AT_75, THROUGHPUT_AT_100,
                    THROUGHPUT_AT_150, THROUGHPUT_AT_200, THROUGHPUT_AT_250, THROUGHPUT_AT_300, THROUGHPUT_AT_400,
                    THROUGHPUT_AT_500, COL_TOTAL_ASTAR_RUNTIME, COL_TOTAL_ASTAR_CALLS] \
                   + [COL_GRAPH_DEGREE, COL_GRAPH_VERTICES] + [COL_ELAPSED_TIME, COL_LOW_LEVEL_RUN_TIME]
THROUGHPUT_SHORT_NAMES = {THROUGHPUT_AT_25: "t25", THROUGHPUT_AT_50: "t50", THROUGHPUT_AT_75: "t75",
                          THROUGHPUT_AT_100: "t100", THROUGHPUT_AT_150: "t150",
                          THROUGHPUT_AT_200: "t200", THROUGHPUT_AT_250: "t250",
                          THROUGHPUT_AT_300: "t300", THROUGHPUT_AT_400: "t400", THROUGHPUT_AT_500: "t500"}


def analyze_maps_results(chosen_maps: list[str], output_dir_path: str, data_raw: DataFrame):
    max_throughput_dfs = []
    max_thr_agents_dfs = []
    if not chosen_maps:
        chosen_maps = data_raw[COL_MAP_NAME].unique()
    for chosen_map in chosen_maps:
        if len(data_raw.query(f'`{COL_MAP_NAME}` == "{chosen_map}"')) == 0 and not FAIL_ON_ERROR:
            print(f'No data for map {chosen_map}. Skipping...')
            continue
        summary_df, df_max_throughput, df_max_thr_agents = analise_map_results(data_raw=data_raw, chosen_map=chosen_map,
                                                                               output_dir_path=output_dir_path)
        if summary_df is None or df_max_throughput is None or df_max_thr_agents is None:
            continue
        chosen_map_short = chosen_map.replace('.map', '')
        # df_max_throughput.rename(columns={"Max Throughput": chosen_map_short}, inplace=True)
        df_max_throughput.rename(index={"Max Throughput": chosen_map_short}, inplace=True)
        max_throughput_dfs.append(df_max_throughput)
        df_max_thr_agents.rename(index={"Agents": chosen_map_short}, inplace=True)
        max_thr_agents_dfs.append(df_max_thr_agents)
    _output_summary_tables(max_thr_agents_dfs, max_throughput_dfs, output_dir_path, data_raw=data_raw)


def load_all_data(path_data_file_or_dir: str, aliases: Optional[dict[str, str]] = None,
                  keep_solvers: Optional[list[str]] = None, drop_solvers=None):
    if os.path.isdir(path_data_file_or_dir):
        files = os.path.join(path_data_file_or_dir, "*.csv")
        files = glob.glob(files)
        data_raw: DataFrame = pd.concat(map(pd.read_csv, files), ignore_index=True)

        runs_per_instance = []
        for file in files:
            one_instance_df = pd.read_csv(file)
            runs_per_instance.append(len(one_instance_df))
            if len(set(runs_per_instance)) > 1:
                print(f'Error:\n{file} has {len(one_instance_df)} runs, expected {runs_per_instance[0]} runs after checking {len(runs_per_instance)} files. Suspected crash! Check logs.')
                if FAIL_ON_ERROR:
                    assert runs_per_instance[0] == len(one_instance_df)
    else:
        data_raw: DataFrame = pd.read_csv(path_data_file_or_dir)

    if not all(valid == 1 for valid in data_raw[COL_VALID]):
        print(data_raw[data_raw[COL_VALID] == 0])
        if FAIL_ON_ERROR:
            assert all(valid == 1 for valid in data_raw[COL_VALID])

    data_raw[COL_MAP_NAME] = [name.replace('.json', '') for name in data_raw[COL_MAP_NAME]]
    data_raw.drop_duplicates(subset=[COL_MAP_NAME, COL_INSTANCE_NAME, COL_NUM_AGENTS, COL_SOLVER], keep='first',
                             inplace=True)

    if aliases:
        for original, alias in aliases.items():
            data_raw.replace(to_replace=original, value=alias, inplace=True)

    if drop_solvers:
        data_raw = data_raw[~data_raw[COL_SOLVER].isin(drop_solvers)]
    if keep_solvers:
        data_raw = data_raw[data_raw[COL_SOLVER].isin(keep_solvers)]

    solvers_unique = data_raw[COL_SOLVER].unique()
    processors_unique = []
    if COL_PROCESSOR in data_raw.columns:
        processors_unique = data_raw[COL_PROCESSOR].unique()

    return data_raw, solvers_unique, processors_unique


def _output_summary_tables(max_thr_agents_dfs, max_throughput_dfs, output_dir_path, data_raw: DataFrame):
    merged_max_throughput_df = pd.concat(max_throughput_dfs)
    merged_max_throughput_df.to_csv(os.path.join(output_dir_path, 'max_throughput_per_map.csv'),
                                    float_format='%.0f')
    _plot_max_throughput_per_map(merged_max_throughput_df, output_dir_path)

    map_stats = data_raw[[COL_MAP_NAME, COL_GRAPH_VERTICES, COL_GRAPH_DEGREE]]\
        .drop_duplicates([COL_MAP_NAME, COL_GRAPH_VERTICES, COL_GRAPH_DEGREE])
    map_stats[COL_MAP_NAME] = [name.replace('.map', '') for name in map_stats[COL_MAP_NAME]]
    merged_max_throughput_with_map_stats_df = pd.merge(map_stats, merged_max_throughput_df, left_on=COL_MAP_NAME,
                                                       right_index=True, how='right')
    merged_max_throughput_with_map_stats_df.to_csv(os.path.join(output_dir_path,
                                                                'max_throughput_per_map_with_map_stats.csv'),
                                                   float_format='%.1f', index=False)

    merged_max_thr_agents_df = pd.concat(max_thr_agents_dfs)
    # thr_and_agents_df = pd.concat([merged_max_throughput_df, merged_max_thr_agents_df], keys = ['Max Throughput', 'Agents'])
    thr_and_agents_df = DataFrame()
    for column in merged_max_throughput_df.columns:
        thr_and_agents_df[column] = merged_max_throughput_df[column].astype(int).astype(str) + "(" + \
                                    merged_max_thr_agents_df[
                                        column].astype(int).astype(str) + ")"
    thr_and_agents_df.to_csv(os.path.join(output_dir_path, 'max_throughput_and_agents_per_map.csv'),
                             float_format='%.0f')

    # same but with stats
    merged_thr_and_agents_df_with_map_stats_df = pd.merge(map_stats, thr_and_agents_df, left_on=COL_MAP_NAME,
                                                          right_index=True, how='right')
    merged_thr_and_agents_df_with_map_stats_df.to_csv(os.path.join(output_dir_path,
                                                                   'max_throughput_and_agents_per_map_with_map_stats.csv'),
                                                      float_format='%.1f', index=False)


def _plot_max_throughput_per_map(merged_max_throughput_df, output_dir_path, normalize_per_map=True):
    df: DataFrame = merged_max_throughput_df.transpose()
    if normalize_per_map:
        df = df / df.max()
    print(df)
    df = df.loc[df.sum(axis=1).sort_values(ascending=False).index]
    for format in FIG_FORMATS:
        ax = df.plot.bar(rot=22.5, stacked=True)
        for c in ax.containers:
            # Optional: if the segment is small or 0, customize the labels
            labels = [f'{v.get_height(): .2f}' if v.get_height() > 0 else '' for v in c]
            # remove the labels parameter if it's not needed for customized labels
            ax.bar_label(c, labels=labels, label_type='center')

        handles, labels = ax.get_legend_handles_labels()
        ax.legend(handles[::-1], labels[::-1], loc='best')

        fig = ax.get_figure()
        out_path = os.path.join(output_dir_path, f"max_throughput_per_map{'_norm' if normalize_per_map else ''}_plot."
                                                 f"{format}")
        fig.savefig(out_path, format=format, bbox_inches='tight', dpi=300, facecolor="white")


def analise_map_results(data_raw: pd.DataFrame, chosen_map: str, output_dir_path: str,
                        solvers: Optional[list[str]] = None):
    data_raw_one_map, num_unique_instances = choose_map(data_raw=data_raw, chosen_map=chosen_map)
    if data_raw_one_map is None or num_unique_instances is None:
        return None, None, None
    if PRINT:
        print(f"Unique instances for map {chosen_map}: {num_unique_instances}")
        if PRINT >= 2:
            print(f":\n{data_raw_one_map[COL_INSTANCE_NAME].unique()}")
        if PRINT >= 3:
            print(data_raw_one_map)
    if num_unique_instances < MIN_SAMPLES:
        print(f"WARNING! Skipping map {chosen_map} due to only {num_unique_instances} instances")
        return None, None, None

    add_derived_fields(data_raw_one_map)

    thr_summary_df = plot_by_agents(data_raw=data_raw_one_map, chosen_map=chosen_map, output_dir_path=output_dir_path)

    if COL_AVG_ASTAR_RUNTIME in data_raw_one_map.columns and EXTRA_METRICS:
        print(f"Plotting astar runtime for {chosen_map}")
        astar_avg_runtime_summary_df = plot_by_agents(data_raw=data_raw_one_map, chosen_map=chosen_map,
                                                      output_dir_path=output_dir_path,
                                                      chosen_metric=COL_AVG_ASTAR_RUNTIME)

        astar_calls_summary_df = plot_by_agents(data_raw=data_raw_one_map, chosen_map=chosen_map,
                                                output_dir_path=output_dir_path,
                                                chosen_metric=COL_TOTAL_ASTAR_CALLS)
    else:
        print(f"Skipping astar runtime for {chosen_map}")

    if COL_MANAGEMENT_OVERHEAD_RUNTIME_PERCENT in data_raw_one_map.columns and EXTRA_METRICS:
        print(f"Plotting {COL_MANAGEMENT_OVERHEAD_RUNTIME_PERCENT} for {chosen_map}")
        overhead_runtime_percent_summary_df = \
            plot_by_agents(data_raw=data_raw_one_map, chosen_map=chosen_map, output_dir_path=output_dir_path, chosen_metric=COL_MANAGEMENT_OVERHEAD_RUNTIME_PERCENT)
    else:
        print(f"Skipping {COL_MANAGEMENT_OVERHEAD_RUNTIME_PERCENT} for {chosen_map}")

    df_max_throughput = max_throughput_by_agents_df(summary_throughput_by_agents_df=thr_summary_df, chosen_map=chosen_map,
                                                    output_dir=output_dir_path, plot=True)

    df_max_thr_agents = max_throughput_agents_by_agents_df(summary_throughput_by_agents_df=thr_summary_df, chosen_map=chosen_map,
                                         output_dir=output_dir_path)

    # dfs_plotting_data = aggregate_metrics(data_raw=data_raw_one_map, by='Mean')
    # ncols = 3
    # plot_through_over_time(dfs_plotting_data, nrows=math.ceil(len(dfs_plotting_data)/(float(ncols))), ncols=ncols,
    #                        output_dir=output_dir_path, chosen_map=chosen_map,
    #                        kind='line', other_plot_args={'linewidth': 3})

    return thr_summary_df, df_max_throughput, df_max_thr_agents


def choose_map(data_raw: pd.DataFrame, chosen_map: str):
    if PRINT:
        print(f"\nChoosing map {chosen_map}\n")

    data_raw_one_map = data_raw[data_raw[COL_MAP_NAME].str.contains(chosen_map)]

    if len(data_raw_one_map[COL_INSTANCE_NAME].unique()) not in [25, 30]:
        print(f"Error: {len(data_raw_one_map[COL_INSTANCE_NAME].unique())} instances for map {chosen_map}, expected 25 or 30")
        print(sorted([inst for inst in data_raw_one_map[COL_INSTANCE_NAME].unique()]))
        # return None, None # TODO parameterise this
        # assert len(data_raw_one_map[COL_INSTANCE_NAME].unique()) in [25, 30] # TODO parameterise this

    # drop agent numbers that don't have all their runs
    median_num_rows_for_agent_num = max(len(group[1]) for group in data_raw_one_map.groupby(COL_NUM_AGENTS))
    data_raw_one_map_consistent_agents = data_raw_one_map.groupby(COL_NUM_AGENTS).filter(lambda x: len(x) == median_num_rows_for_agent_num)
    dropped_agent_numbers = [inst for inst in data_raw_one_map[COL_NUM_AGENTS].unique() if inst not in data_raw_one_map_consistent_agents[COL_NUM_AGENTS].unique()]
    dropped_agent_major_numbers = [num for num in dropped_agent_numbers if num % 25 == 0]
    if PRINT:
        print(f"dropped agent numbers: {sorted(dropped_agent_numbers)}")
    # if any(num < max(dropped_agent_numbers)/2 for num in dropped_agent_numbers):
    if len(dropped_agent_major_numbers) > 3:
        counts_per_number = {group[0]: len(group[1]) for group in data_raw_one_map.groupby(COL_NUM_AGENTS)}
        print(f"count per number: {counts_per_number}")
        print(f"count per numbers: {[pair for pair in counts_per_number.items() if pair[0] in dropped_agent_numbers]}")
        assert False
    if data_raw_one_map_consistent_agents.empty:
        return None, None

    # drop instances that don't have all their runs
    median_num_rows_for_instance = median(len(group[1]) for group in data_raw_one_map_consistent_agents.groupby(COL_INSTANCE_NAME))
    data_raw_one_map_consistent_agents_and_instances = data_raw_one_map_consistent_agents.groupby(COL_INSTANCE_NAME).filter(lambda x: len(x) == median_num_rows_for_instance)
    if PRINT:
        print(f"dropped instances: {[inst for inst in data_raw_one_map_consistent_agents[COL_INSTANCE_NAME].unique() if inst not in data_raw_one_map_consistent_agents_and_instances[COL_INSTANCE_NAME].unique()]}")
    if data_raw_one_map_consistent_agents_and_instances.empty:
        return None, None

    num_unique_instances = len(data_raw_one_map_consistent_agents_and_instances[COL_INSTANCE_NAME].unique())
    return data_raw_one_map_consistent_agents_and_instances, num_unique_instances


def add_derived_fields(df_for_plots):
    if EXTRA_METRICS:
        if COL_TOTAL_ASTAR_RUNTIME in df_for_plots.columns:
            df_for_plots[COL_AVG_ASTAR_RUNTIME] = \
                df_for_plots.apply(lambda x: x[COL_TOTAL_ASTAR_RUNTIME] / float(x[COL_TOTAL_ASTAR_CALLS]), axis=1)
        if COL_LOW_LEVEL_RUN_TIME in df_for_plots.columns and COL_ELAPSED_TIME in df_for_plots.columns:
            df_for_plots[COL_MANAGEMENT_OVERHEAD_RUNTIME_PERCENT] = \
                df_for_plots.apply(lambda x: 1 - (x[COL_LOW_LEVEL_RUN_TIME] / float(x[COL_ELAPSED_TIME])), axis=1)


def aggregate_metrics(data_raw: pd.DataFrame, by='Mean'):
    df_for_plots = data_raw[RELEVANT_COLUMNS]
    df_for_plots.rename(inplace=True, columns=THROUGHPUT_SHORT_NAMES)
    dfs_nums_agents = []
    dfs_plotting_data = []
    aggregation = by
    for num_agents in sorted(df_for_plots[COL_NUM_AGENTS].unique()):
        df = df_for_plots[df_for_plots[COL_NUM_AGENTS] == num_agents].groupby([COL_SOLVER])
        if aggregation == 'Median':
            df = df.median()
        elif aggregation == 'Mean':
            df = df.mean()
        elif aggregation == 'Max':
            df = df.max()
        elif aggregation == 'Min':
            df = df.min()
        else:
            raise Exception(f'invalid {aggregation=}')
        df = df.drop([COL_NUM_AGENTS], axis=1)
        if PRINT:
            print(f'{num_agents=}')
            if PRINT >= 2:
                print(df)
        dfs_nums_agents.append(df)
        dfs_plotting_data.append(
            DFPlottingData(df, f'{aggregation} throughput with {num_agents} Agents, More Is Better', []))

    return dfs_plotting_data


# Summarizing and Plotting Functions

@dataclass
class DFPlottingData:
    df: DataFrame
    name: str
    left_metrics: list[str]
    right_metrics: list[str] = field(default_factory=list)


def plot_by_agents(data_raw: pd.DataFrame, chosen_map: str, output_dir_path: str,
                   solvers: Optional[list[str]] = None, min_agents=0, max_agents=2000, chosen_metric=THROUGHPUT_AT_300):
    markers = itertools.cycle(('s', '+', '.', 'o', '*', 'd', '2', 'x',))
    # title = f"{chosen_map} {chosen_metric} by #Agents"
    title = f"{chosen_map}"
    df = data_raw[[COL_SOLVER, COL_NUM_AGENTS, chosen_metric]]
    df = df[(df[COL_NUM_AGENTS] >= min_agents) & (df[COL_NUM_AGENTS] <= max_agents)]

    if solvers:
        df = df[df[COL_SOLVER].isin(solvers)]

    grouped_by_solver = df.groupby(COL_SOLVER)

    fig: Figure
    ax1: Axes
    fig, ax1 = plt.subplots()
    # ax2 = ax1.twinx()
    # fontsize = 110  # paper / presentation
    # fontsize = 60  # daily
    fontsize = 30  # small
    ax1.set_title(title, fontsize=fontsize * 0.75)
    ax1.set_xlabel('# Agent', fontsize=fontsize * 0.75,  labelpad=-10)
    if 'throughputAtT' in chosen_metric:
        ax1.set_ylabel('Throughput at X=' + chosen_metric.split('throughputAtT')[1], fontsize=fontsize * 0.75, labelpad=-10)
    else:
        ax1.set_ylabel(chosen_metric, fontsize=fontsize * 0.75, labelpad=-10)
    # y_250_median = df[df[COL_NUM_AGENTS] == 250].median()
    summary_data = []

    def key(solver_and_df_tuple):
        solver, solver_df = solver_and_df_tuple
        return 0

    xytexts = []
    max_vals_xs_of_max_vals = []
    for solver, solver_df in sorted(grouped_by_solver, key=key):
        if PRINT >= 2:
            print(f"solver {solver} 400 agents: ")
            print(solver_df[solver_df[COL_NUM_AGENTS] == 400])
        agg_metric_per_num_agents = solver_df.groupby(COL_NUM_AGENTS).mean()
        x = agg_metric_per_num_agents.index
        y = agg_metric_per_num_agents[chosen_metric]

        summary_data.append(pd.Series(data=y, index=x, name=solver))
        ax1.plot(x, y, label=solver, marker=next(markers), linewidth=10, markersize=30)

        max_val = max(y)
        x_of_max_val = y.idxmax()
        x_of_max_val = round(x_of_max_val, 1)

        if (int(max_val), int(x_of_max_val)) in max_vals_xs_of_max_vals:
            continue
        max_vals_xs_of_max_vals.append((int(max_val), int(x_of_max_val)))

        max_x = max(x)
        direction = -10 if x_of_max_val > max_x*0.8 else 1
        xytext = (x_of_max_val + direction * (x_of_max_val * 0.01), max_val + (max_val * 0.005)
                  # + random.randint(-10, 10)
                  )
        # if all((abs(xtext - xytext[0]) > x_of_max_val * 0.05) or (abs(ytext - xytext[1]) > max_val * 0.05)
        #        for xtext, ytext in xytexts):
        #     xytexts.append(xytext)
        #     ax1.annotate(f'{max_val: .0f}',
        #                  xy=(x_of_max_val, max_val),
        #                  xytext=xytext,
        #                  arrowprops=dict(facecolor='black', shrink=0.1), fontsize=fontsize * 0.4)
        while any(not ((abs(xtext - xytext[0]) > x_of_max_val * 0.05) or (abs(ytext - xytext[1]) > max_val * 0.05))
                  for xtext, ytext in xytexts):
            xytext = (xytext[0], xytext[1] + (-1) * (max_val * 0.0005))

        xytexts.append(xytext)
        ax1.annotate(f'{max_val: .0f}',
                     xy=(x_of_max_val, max_val),
                     xytext=xytext,
                     arrowprops=dict(facecolor='black', shrink=0.1), fontsize=fontsize * 0.5)

        # direction = 1
        # ax1.annotate(f'{max_val: .1f}',
        #              xy=(x_of_max_val + direction * (x_of_max_val * 0.025), max_val + (max_val * 0.01)),
        #              xytext=(x_of_max_val + direction * (x_of_max_val * 0.05), max_val + (max_val * 0.01)
        #                      # + random.randint(-10, 10)
        #                      ),
        #              arrowprops=dict(facecolor='black', shrink=0.5), fontsize=fontsize * 0.5)

    summary_df = pd.DataFrame(summary_data).transpose()
    if PRINT >= 2:
        print(summary_df)
    out_path = os.path.join(output_dir_path, f"{title} {chosen_metric}" + "_Agents" if "Agents" not in title else "")
    summary_df.to_csv(f'{out_path}.csv', float_format='%.2f')

    ax1.set_xlim(df[COL_NUM_AGENTS].min(), df[COL_NUM_AGENTS].max())
    # ax1.set_xlim(0, df[COL_NUM_AGENTS].max())
    ax1.set_ylim(0, df[chosen_metric].max())
    # ax1.legend(fontsize=fontsize * 0.5, loc='best')

    loc = 'lower right' if chosen_map in ['empty-48-48.map', 'maze-128-128-10.map'] else 'best'
    # loc = 'best'

    # ax1.legend(prop=dict({'size': fontsize * 0.6}, **DEJAVU_MONO), loc=loc)
    # ax1.legend(prop=dict({'size': fontsize * 0.6}), loc=loc)
    ax1.legend(prop=dict({'size': fontsize * 0.5}), loc=loc)
    plt.tick_params(axis='both', which='major', labelsize=fontsize * 0.6)
    fig.tight_layout()
    for fig_format in FIG_FORMATS:
        fig.savefig(f'{out_path}.{fig_format}', facecolor="white", transparent=True, bbox_inches='tight')
    if PRINT:
        fig.show()
    return summary_df


def max_throughput_by_agents_df(summary_throughput_by_agents_df: pd.DataFrame, chosen_map: str, output_dir: str,
                                plot=False):
    df_max_throughput = DataFrame(
        summary_throughput_by_agents_df.max().sort_values(ascending=False).rename('Max Throughput')).transpose()
    table_formatting_aliases = {'baselineRHCR_w-inf_h01': r'baselineRHCR\\w-inf_h01',
                                '0% Cutoff (Always Persist)': r'0% Cutoff\\(Always Persist)', '25% Cutoff': r'25%\\Cutoff',
                                '50% Cutoff': r'50%\\Cutoff', '75% Cutoff': r'75%\\Cutoff',
                                '100% Cutoff (Always Restart)': r'100% Cutoff\\(Always Restart)',
                                'No Partial (Baseline)': r'No Partial\\(Baseline)',
                                'Persist Until Full Solution': r'Persist Until\\Full Solution',
                                'Persist Once Then Restart': r'Persist Once\\Then Restart', 'RHCR w=5 h=3': r'RHCR\\w=5 h=3',
                                'Adaptive(0.25, 0.1)': r'Adaptive\\(0.25,0.1)',
                                'Adaptive(0.25, 0.01)': r'Adaptive(0.25,\\0.01)'}
    # multirow_part1 = r'\begin{tabular}[c]{@{}l@{}}'
    multirow_part1 = r'---'
    # multirow_part2 = r'\end{tabular}'
    multirow_part2 = r'+++'
    table_formatting_aliases = {original: f'{multirow_part1}{alias}{multirow_part2}' for original, alias in
                                table_formatting_aliases.items()}
    df_max_throughput.rename(columns=table_formatting_aliases, inplace=True)
    out_path = os.path.join(output_dir, f"{chosen_map}_{'Max_Throughput'}")
    df_max_throughput.to_csv(f'{out_path}.csv', float_format='%.2f')

    if plot:
        DEF_FONTSIZE = 60
        df_transposed = df_max_throughput.transpose()
        df_transposed.rename(columns={'Max Throughput': 'Max Throughput at X=200'}, inplace=True)
        if 'AllAgents' in df_transposed.index:
            df_transposed = df_transposed.reindex(['Fail@LH03', 'Fail@LH05', 'Fail@LH07', 'Fail@LH10', 'AllAgents'])
        for fig_format in FIG_FORMATS:
            res_fig = df_transposed.plot.bar(fontsize=DEF_FONTSIZE * 0.5, rot=22.5)
            res_fig.legend(prop=dict({'size': DEF_FONTSIZE * 0.5}), loc='lower right')
            res_fig.figure.savefig(f'{out_path}_plot.{fig_format}', facecolor="white", transparent=True, bbox_inches='tight')

    return df_max_throughput


def max_throughput_agents_by_agents_df(summary_throughput_by_agents_df: pd.DataFrame, chosen_map: str, output_dir: str, ):
    df_max_thr_agents = DataFrame(
        summary_throughput_by_agents_df.idxmax().sort_values(ascending=False).rename('Agents')).transpose()
    out_path = os.path.join(output_dir, f"{chosen_map}_{'Max_Throughput_Agents'}")
    df_max_thr_agents.to_csv(f'{out_path}.csv', float_format='%.2f')
    return df_max_thr_agents


def plot_through_over_time(dfs_plotting_data: list[DFPlottingData], nrows=1, ncols=1,
                           output_dir: str = "", chosen_map: str = "",
                           bar_labels=True, stacked_bars=False, other_plot_args={}, kind='barh',
                           fig_width=DEF_FIG_WIDTH, fig_height=DEF_FIG_HEIGHT):
    assert len(dfs_plotting_data) <= nrows * ncols
    iter_dfs = iter(dfs_plotting_data)
    fig, ax = plt.subplots(nrows=nrows, ncols=ncols, figsize=(fig_width * ncols, fig_height * nrows))
    for i in range(0, nrows):
        for j in range(0, ncols):
            if curr_plotting_data := next(iter_dfs, None):
                df, name, left_metrics, right_metrics = curr_plotting_data.df, curr_plotting_data.name, curr_plotting_data.left_metrics, curr_plotting_data.right_metrics
                if len(left_metrics) == 1 and len(right_metrics) == 0:
                    df = df[left_metrics[0]].unstack()

                subplot_ax: plt.Axes = ax[i, j] if (nrows > 1 and ncols > 1) else \
                    (ax[i] if (nrows > 1) else
                     (ax[j] if (ncols > 1) else
                      ax))

                plot_args = {key: val for key, val in
                             [('kind', kind), ('ax', subplot_ax)] + list(other_plot_args.items())}
                df = df.transpose()
                df.plot(**plot_args)

                if name:
                    subplot_ax.set_title(name)
    plt.tight_layout()
    if len(dfs_plotting_data) % 2 != 0 and (nrows * ncols) % 2 == 0:
        fig.delaxes(ax[nrows - 1, ncols - 1])

    if output_dir and chosen_map:
        save_file_path = os.path.join(output_dir, f"{chosen_map}_graph_per_agents_metrics")
        for fig_format in FIG_FORMATS:
            plt.savefig(f"{save_file_path}.{fig_format}", bbox_inches='tight')


def get_date_time_string():
    return datetime.now().strftime('%Y%d%m_%H%M%S')


def _get_solvers_string(solvers_unique: list[str]):
    return str(solvers_unique).translate({ord(char): None for char in "'[]\n"}).replace(' ', '_')
