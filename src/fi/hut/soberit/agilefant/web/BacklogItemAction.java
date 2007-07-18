package fi.hut.soberit.agilefant.web;

import java.util.ArrayList;
import java.util.Collection;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;

import fi.hut.soberit.agilefant.db.BacklogDAO;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.IterationGoalDAO;
import fi.hut.soberit.agilefant.db.TaskDAO;
import fi.hut.soberit.agilefant.db.TaskEventDAO;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.IterationGoal;
import fi.hut.soberit.agilefant.model.Task;
import fi.hut.soberit.agilefant.model.User;
import fi.hut.soberit.agilefant.security.SecurityUtil;

public class BacklogItemAction extends ActionSupport implements CRUDAction {
	
	private static final long serialVersionUID = -4289013472775815522L;
	private BacklogDAO backlogDAO;
	private BacklogItemDAO backlogItemDAO;
	private int backlogId;
	private int backlogItemId;
	private BacklogItem backlogItem;
	private Backlog backlog;
	private Collection<BacklogItem> backlogItems = new ArrayList<BacklogItem>();
	private UserDAO userDAO;
	private boolean watch = false;
	private IterationGoalDAO iterationGoalDAO;
	private int iterationGoalId;
	private int assigneeId;
	private TaskDAO taskDAO;
	private TaskAction taskAction;
	private TaskEventDAO taskEventDAO;



	public String create() {
		backlogItemId = 0;
		backlogItem = new BacklogItem();
		backlog = backlogDAO.get(backlogId);
		//backlogId = backlog.getId();
		return Action.SUCCESS;
	}

	public String delete() {
		backlogItem = backlogItemDAO.get(backlogItemId);
		if(backlogItem == null){
			super.addActionError(super.getText("backlogItem.notFound"));
			return Action.ERROR;
		}
		// backlogId = backlogItem.getId();//?? removed when testing with jUnit
		backlogItemDAO.remove(backlogItemId);
		return Action.SUCCESS;
	}

	public String edit() {
		backlogItem = backlogItemDAO.get(backlogItemId);
		if (backlogItem == null){
			super.addActionError(super.getText("backlogItem.notFound"));
			return Action.ERROR;
		}
		backlog = backlogItem.getBacklog();
		backlogId = backlog.getId();
		return Action.SUCCESS;
	}

	public String store() {
		Integer storableId;
		Integer placeholderId;
		BacklogItem storable = new BacklogItem();
		if (backlogItemId > 0){
			storable = backlogItemDAO.get(backlogItemId);
			if (storable == null){
				super.addActionError(super.getText("backlogItem.notFound"));
				return Action.ERROR;
			}
		}
		this.fillStorable(storable);
		
		if (super.hasActionErrors()){
			return Action.ERROR;
		}
		
		/* Set placeholder task properties */
		if (storable.getId() == 0){
			Task placeholder = new Task();
			
			if(taskAction.create() != Action.SUCCESS) {
				super.addActionError(super.getText(
						"placeholder.task.notCreated"));
				return Action.ERROR;
			}
			storableId = (Integer) backlogItemDAO.create(storable);
			placeholder.setCreator(SecurityUtil.getLoggedUser());
			placeholder.setName("Placeholder");
			placeholder.setEffortEstimate(backlogItem.getAllocatedEffort());
			taskAction.setTask(placeholder);
			taskAction.setBacklogItemId(storableId);
			taskAction.setBacklogItemDAO(backlogItemDAO);
			taskAction.setTaskDAO(taskDAO);
			taskAction.setTaskEventDAO(taskEventDAO);
			taskAction.setUserDAO(userDAO);				
			placeholderId = taskAction.storeNew();
			storable.setPlaceHolder(taskDAO.get(placeholderId.intValue()));
		}
		backlogItemDAO.store(storable);
		return Action.SUCCESS;
	}
	
	protected void fillStorable(BacklogItem storable){
		User oldAssignee = storable.getAssignee();
		User newAssignee = null;

		if ((oldAssignee == null && assigneeId > 0) || 
			 (oldAssignee != null && oldAssignee.getId() != assigneeId)) {
			if (assigneeId > 0) {
				newAssignee = userDAO.get(assigneeId);
			}
			storable.setAssignee(newAssignee);
		}
				
		if (this.backlogItem.getIterationGoal() != null){
			IterationGoal goal = iterationGoalDAO.get(this.backlogItem.getIterationGoal().getId());
			//IterationGoal goal = iterationGoalDAO.get(iterationGoalId);
			storable.setIterationGoal(goal);
		}
		if(this.backlogItem.getName().equals("")) {
			super.addActionError(super.getText("backlogitem.missingName"));
			return;
		}
		storable.setName(this.backlogItem.getName());
		storable.setDescription(this.backlogItem.getDescription());
		storable.setAllocatedEffort(this.backlogItem.getAllocatedEffort());
		storable.setPriority(this.backlogItem.getPriority());
		storable.setStatus(this.backlogItem.getStatus()); // added after failed jUnit test 
		
		backlog = backlogDAO.get(backlogId);
		if (backlog == null){
			super.addActionError(super.getText("backlog.notFound"));
		}
		if (storable.getId() > 0){
			storable.getBacklog().getBacklogItems().remove(storable);
			backlog.getBacklogItems().add(storable);
		}
		storable.setBacklog(backlog);
		
		if (watch) {
			User user = SecurityUtil.getLoggedUser();
			storable.getWatchers().put(user.getId(), user);
			user.getWatchedBacklogItems().add(storable);		
		}
		else {
			User user = SecurityUtil.getLoggedUser();
			storable.getWatchers().remove(user.getId());
		}
	}
	
//	private void fillTask(Task task, Integer storableId) {
//		EstimateHistoryEvent event = new EstimateHistoryEvent();
//		event.setActor(SecurityUtil.getLoggedUser());
////		event.setNewEstimate(newEstimate);
////		storable.setEffortEstimate(newEstimate);
////		taskDAO.store(storable);
////		event.setTask(storable);
////		storable.getEvents().add(event);
////		taskEventDAO.store(event);
//		task.setBacklogItem(backlogItemDAO.get(storableId));
//	}
	
	public Backlog getBacklog() {
		return backlog;
	}

	public void setBacklog(Backlog backlog) {
		this.backlog = backlog;
	}

	public int getBacklogId() {
		return backlogId;
	}

	public void setBacklogId(int backlogId) {
		this.backlogId = backlogId;
	}

	public BacklogItem getBacklogItem() {
		return backlogItem;
	}

	public void setBacklogItem(BacklogItem backlogItem) {
		this.backlogItem = backlogItem;
	}

	public int getBacklogItemId() {
		return backlogItemId;
	}

	public void setBacklogItemId(int backlogItemId) {
		this.backlogItemId = backlogItemId;
	}

	public Collection<BacklogItem> getBacklogItems() {
		return backlogItems;
	}

	public void setBacklogItems(Collection<BacklogItem> backlogItems) {
		this.backlogItems = backlogItems;
	}

	public void setBacklogDAO(BacklogDAO backlogDAO) {
		this.backlogDAO = backlogDAO;
	}

	public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
		this.backlogItemDAO = backlogItemDAO;
	}
	
/*	protected BacklogItemDAO getBacklogItemDAO() {
		return this.backlogItemDAO;
	}*/

	public void setUserDAO(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	public void setWatch(boolean watch) {
		this.watch = watch;
	}

	public void setIterationGoalDAO(IterationGoalDAO iterationGoalDAO) {
		this.iterationGoalDAO = iterationGoalDAO;
	}

	public void setAssigneeId(int assigneeId) {
		this.assigneeId = assigneeId;
	}

	/**
	 * Setter for Spring IoC
	 * 
	 * @param iterationGoalId iteration goal id to be set
	 */
	public void setIterationGoalId(int iterationGoalId){
		this.iterationGoalId= iterationGoalId;
	}
	
	/**
	 * Getter for Spring IoC
	 * 
	 * @return iteration goal id
	 */
	public int getIterationGoalId(){
		return iterationGoalId;
	}

	/**
	 * @return the task data access object
	 */
	public TaskDAO getTaskDAO() {
		return taskDAO;
	}

	/**
	 * @param taskDAO the task data access object to set
	 */
	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	/**
	 * @return the taskAction
	 */
	public TaskAction getTaskAction() {
		return taskAction;
	}

	/**
	 * @param taskAction the taskAction to set
	 */
	public void setTaskAction(TaskAction taskAction) {
		this.taskAction = taskAction;
	}

	/**
	 * @return the taskEventDAO
	 */
	public TaskEventDAO getTaskEventDAO() {
		return taskEventDAO;
	}

	/**
	 * @param taskEventDAO the taskEventDAO to set
	 */
	public void setTaskEventDAO(TaskEventDAO taskEventDAO) {
		this.taskEventDAO = taskEventDAO;
	}
	
}
