package main.core;

import java.util.ArrayList;

public class PipePuzzleData implements Comparable<PipePuzzleData>
{
	public int dev_id = 0;
	public int size = 0;
	public int elbows = 0;
	public int pipes = 0;
	public int tJunctions = 0;
	public int crosses = 0;
	public ArrayList<StartGoal> starts = new ArrayList<StartGoal>();
	public ArrayList<StartGoal> goals = new ArrayList<StartGoal>();
	public ArrayList<SolutionData> solution = new ArrayList<SolutionData>();
	
	public static class StartGoal
	{
		public int x = 0;
		public int y = 0;
		public PipePuzzle.Orientation orientation = null;
	}
	
	public int compareTo(PipePuzzleData that)
	{
		if(this.dev_id < that.dev_id){
			return -1;
		}else if(this.dev_id > that.dev_id){
			return 1;
		}else{
			return 0;
		}
	}
}
