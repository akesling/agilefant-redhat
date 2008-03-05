-- Remove links to iteration goals if the Backlog Item is in an other backlog
update BacklogItem, IterationGoal set iterationGoal_id = null where BacklogItem.iterationGoal_id = IterationGoal.id and BacklogItem.backlog_id != IterationGoal.iteration_id;

-- Update AFTime to be in seconds and not in milliseconds
update BacklogItem set effortLeft = effortLeft / 1000;
update BacklogItem set originalEstimate = originalEstimate / 1000;
update HistoryEntry set effortLeft = effortLeft / 1000;
update HistoryEntry set originalEstimate = originalEstimate / 1000;

-- Add rank-column to Task 
alter table Task add column rank integer;

-- Set initial ranks to be task ids for existing tasks
update Task set rank = id;

-- Overhead-kenttä
alter table Assignment add column deltaOverhead integer;
alter table Backlog add column defaultOverhead integer;

-- Rename all tables to small characters
rename table Assignment to assignment,
	Backlog to backlog,
	BacklogItem to backlogitem,
	BacklogItem_User to backlogitem_user,
	History to history,
	HistoryEntry to historyentry,
	IterationGoal to iterationgoal,
	ProjectType to projecttype,
	Task to task,
	Team to team,
	Team_User to team_user,
	User to `user`,
	WorkType to worktype;
	