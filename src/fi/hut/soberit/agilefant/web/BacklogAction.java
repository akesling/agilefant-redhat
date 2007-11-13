package fi.hut.soberit.agilefant.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;

import fi.hut.soberit.agilefant.business.BacklogBusiness;
import fi.hut.soberit.agilefant.db.BacklogDAO;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.EffortHistoryDAO;
import fi.hut.soberit.agilefant.db.TaskEventDAO;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.Deliverable;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.Product;
import fi.hut.soberit.agilefant.util.EffortHistoryUpdater;

public class BacklogAction extends ActionSupport {
	private static final long serialVersionUID = 8061288993804046816L;

	private int backlogId;

	private BacklogDAO backlogDAO;

	private EffortHistoryDAO effortHistoryDAO;

	private TaskEventDAO taskEventDAO;

	private int backlogItemId;

	private int[] backlogItemIds;

	private int targetBacklogId;

	private BacklogItemDAO backlogItemDAO;

	private BacklogBusiness backlogBusiness;

	/**
	 * Used to determine what action is taken when multiple
	 * <code>BacklogItems</code> are manipulated at once.
	 */
	private String itemAction;

	public String edit() {
		Backlog backlog = backlogDAO.get(backlogId);
		return solveResult(backlog);
	}

	public int getBacklogId() {
		return backlogId;
	}

	public void setBacklogId(int backlogId) {
		this.backlogId = backlogId;
	}

	public void setBacklogDAO(BacklogDAO backlogDAO) {
		this.backlogDAO = backlogDAO;
	}

	public String moveBacklogItem() {
		Backlog backlog = backlogDAO.get(backlogId);
		BacklogItem backlogItem = backlogItemDAO.get(backlogItemId);
		if (backlog == null) {
			super.addActionError(super.getText("backlog.notFound"));
			return Action.ERROR;
		}
		if (backlogItem == null) {
			super.addActionError(super.getText("backlogItem.notFound"));
		}

		backlogItem.getBacklog().getBacklogItems().remove(backlogItem);
		backlog.getBacklogItems().add(backlogItem);
		backlogItem.setBacklog(backlog);
		backlogItemDAO.store(backlogItem);

		return this.solveResult(backlog);
	}

	/**
	 * Moves selected <code>BacklogItems</code> to selected
	 * <code>Backlog</code>.
	 * 
	 * @return <code>Action.ERROR</code> if there was an error, otherwise a
	 *         redirect to display current backlog.
	 */
	public String moveSelectedItems() {
		Log logger = LogFactory.getLog(getClass());
		Backlog targetBacklog = this.backlogDAO.get(targetBacklogId);
		Backlog currentBacklog = this.backlogDAO.get(backlogId);

		if (backlogItemIds == null) {
			super.addActionError(super.getText("backlogItems.notSelected"));
			return Action.ERROR;
		}

		logger.info("Moving " + backlogItemIds.length + " items + "
				+ " to backlog: " + targetBacklogId);

		if (targetBacklog == null || currentBacklog == null) {
			super.addActionError(super.getText("backlog.notFound"));
			return Action.ERROR;
		}

		for (int i = 0; i < this.backlogItemIds.length; i++) {
			BacklogItem backlogItem = this.backlogItemDAO
					.get(this.backlogItemIds[i]);
			if (!backlogItem.getBacklog().getBacklogItems().remove(backlogItem)) {
				super.addActionError(super.getText("backlogItem.notFound"));
				return Action.ERROR;
			}

			if (!targetBacklog.getBacklogItems().add(backlogItem)) {
				return Action.ERROR;
			}

			backlogItem.setBacklog(targetBacklog);
			backlogItemDAO.store(backlogItem);

		}
		EffortHistoryUpdater.updateEffortHistory(effortHistoryDAO,
				taskEventDAO, backlogItemDAO, currentBacklog);
		EffortHistoryUpdater.updateEffortHistory(effortHistoryDAO,
				taskEventDAO, backlogItemDAO, targetBacklog);

		return this.solveResult(currentBacklog);
	}

	/**
	 * Deletes multiple selected <code>BacklogItems</code>.
	 * 
	 * @return <code>Action.ERROR</code> if there was an error, otherwise a
	 *         redirect to display current backlog.
	 */
	public String deleteSelectedItems() {
		Backlog currentBacklog = this.backlogDAO.get(backlogId);

		if (backlogItemIds == null) {
			super.addActionError(super.getText("backlogItems.notSelected"));
			return Action.ERROR;
		}

		backlogBusiness.deleteMultipleItems(backlogId, backlogItemIds);

		/* Update effort history */
		/*
		 * EffortHistoryUpdater.updateEffortHistory(effortHistoryDAO,
		 * taskEventDAO, backlogItemDAO, currentBacklog);
		 */

		return this.solveResult(currentBacklog);
	}

	/**
	 * Perform an action on multiple <code>BacklogItems</code>
	 * 
	 * @return <code>"move"</code> if the action is to move,
	 *         <code>"delete"</code> if the action is to delete and
	 *         <code>Action.ERROR</code> if the action can't be determined.
	 */
	public String doActionOnMultipleBacklogItems() {
		Log logger = LogFactory.getLog(getClass());

		if (itemAction.equals("MoveSelected")) {
			return "move";
		} else if (itemAction.equals("DeleteSelected")) {
			return "delete";
		}
		logger.error("Invalid action on multiple backlog items: " + itemAction);

		return Action.ERROR;
	}

	protected String solveResult(Backlog backlog) {
		if (backlog == null) {
			super.addActionError(super.getText("backlog.notFound"));
			return Action.ERROR;
		} else if (backlog instanceof Product) {
			return "editProduct";
		} else if (backlog instanceof Deliverable) {
			return "editDeliverable";
		} else if (backlog instanceof Iteration) {
			return "editIteration";
		}
		super.addActionError(super.getText("backlog.unknownType"));
		return Action.ERROR;
	}

	public int getBacklogItemId() {
		return backlogItemId;
	}

	public int[] getSelected() {
		return backlogItemIds;
	}

	public int getTargetBacklog() {
		return targetBacklogId;
	}

	public void setTargetBacklog(int targetBacklogId) {
		this.targetBacklogId = targetBacklogId;
	}

	public void setSelected(int[] backlogItemIds) {
		this.backlogItemIds = backlogItemIds;
	}

	public void setBacklogItemId(int backlogItemId) {
		this.backlogItemId = backlogItemId;
	}

	public String getItemAction() {
		return itemAction;
	}

	public void setItemAction(String itemAction) {
		this.itemAction = itemAction;
	}

	public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
		this.backlogItemDAO = backlogItemDAO;
	}

	/**
	 * @return the effortHistoryDAO
	 */
	public EffortHistoryDAO getEffortHistoryDAO() {
		return effortHistoryDAO;
	}

	/**
	 * @param effortHistoryDAO
	 *            the effortHistoryDAO to set
	 */
	public void setEffortHistoryDAO(EffortHistoryDAO effortHistoryDAO) {
		this.effortHistoryDAO = effortHistoryDAO;
	}

	/**
	 * @return the taskEventDAO
	 */
	public TaskEventDAO getTaskEventDAO() {
		return taskEventDAO;
	}

	/**
	 * @param taskEventDAO
	 *            the taskEventDAO to set
	 */
	public void setTaskEventDAO(TaskEventDAO taskEventDAO) {
		this.taskEventDAO = taskEventDAO;
	}

	public BacklogBusiness getBacklogBusiness() {
		return backlogBusiness;
	}

	public void setBacklogBusiness(BacklogBusiness backlogBusiness) {
		this.backlogBusiness = backlogBusiness;
	}
}
