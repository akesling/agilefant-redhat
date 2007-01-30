package fi.hut.soberit.agilefant.web;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;

import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.IterationGoalDAO;
import fi.hut.soberit.agilefant.db.TaskDAO;
import fi.hut.soberit.agilefant.db.TaskEventDAO;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.model.AssignEvent;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.IterationGoal;
import fi.hut.soberit.agilefant.model.Task;
import fi.hut.soberit.agilefant.model.User;
import fi.hut.soberit.agilefant.security.SecurityUtil;

public class LinkToIterationGoalAction extends ActionSupport {
	
	private static final long serialVersionUID = 6934367420900867986L;
	private int iterationGoalId;
	private int backlogItemId;
	private IterationGoalDAO iterationGoalDAO;
	private BacklogItemDAO backlogItemDAO;
	
	public String execute(){
		IterationGoal goal = iterationGoalDAO.get(iterationGoalId);
		BacklogItem item = backlogItemDAO.get(backlogItemId);
		
		item.setIterationGoal(goal);
		backlogItemDAO.store(item);
		
		return Action.SUCCESS;
	}
	
	public int getIterationGoalId() {
		return iterationGoalId;
	}

	public void setIterationGoalId(int iterationGoalId) {
		this.iterationGoalId = iterationGoalId;
	}

	public int getBacklogItemId() {
		return backlogItemId;
	}

	public void setBacklogItemId(int backlogItemId) {
		this.backlogItemId = backlogItemId;
	}

	public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
		this.backlogItemDAO = backlogItemDAO;
	}

	public void setIterationGoalDAO(IterationGoalDAO iterationGoalDAO) {
		this.iterationGoalDAO = iterationGoalDAO;
	}

}