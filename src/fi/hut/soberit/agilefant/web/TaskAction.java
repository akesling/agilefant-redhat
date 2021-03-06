package fi.hut.soberit.agilefant.web;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;

import fi.hut.soberit.agilefant.business.TaskBusiness;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.TaskDAO;
import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.Task;
import fi.hut.soberit.agilefant.security.SecurityUtil;
import flexjson.JSONSerializer;

/**
 * Task Action
 * 
 * @author khel
 */
public class TaskAction extends ActionSupport implements CRUDAction {

    private static final long serialVersionUID = -8560828440589313663L;

    private int taskId;

    private int backlogItemId;

    private Task task;

    private TaskDAO taskDAO;

    private BacklogItemDAO backlogItemDAO;

    private TaskBusiness taskBusiness;
    
    private String jsonData = "";

    public TaskBusiness getTaskBusiness() {
        return taskBusiness;
    }

    public void setTaskBusiness(TaskBusiness taskBusiness) {
        this.taskBusiness = taskBusiness;
    }

    /**
     * Creates a new task.
     * 
     * @return Action.SUCCESS
     */
    public String create() {
        taskId = 0;
        task = new Task();
        return Action.SUCCESS;
    }

    /**
     * Fetches a task for editing (based on taskId set)
     * 
     * @return Action.SUCCESS, if task was found or Action.ERROR if task wasn't
     *         found
     */
    public String edit() {
        task = taskDAO.get(taskId);
        if (task == null) {
            super.addActionError(super.getText("task.notFound"));
            return Action.ERROR;
        }
        backlogItemId = task.getBacklogItem().getId();
        return Action.SUCCESS;
    }
    
    
    /**
     * Stores the task for an ajax request.
     */
    public String ajaxStore() {
        Task ret = null;
        try {
            ret = taskBusiness.storeTask(taskId, backlogItemId, task.getName(), task.getState());
        }
        catch (Exception e) {
            return CRUDAction.AJAX_ERROR;
        }
        JSONSerializer ser = new JSONSerializer();
        ser.include("name").include("id").include("state").exclude("*");
        jsonData = ser.serialize(ret);
        return CRUDAction.AJAX_SUCCESS;
    }
    
    /**
     * Deletes the task for an ajax request.
     */
    public String ajaxDelete() {
        try {
            taskBusiness.removeTask(taskId);
        }
        catch (Exception e) {
            return CRUDAction.AJAX_ERROR;
        }
        return CRUDAction.AJAX_SUCCESS;
    }

    /**
     * Stores the task (a new task created with create() or an old one fetched
     * with edit()
     * 
     * @return Action.SUCCESS if task is saved ok or Action.ERROR if there's
     *         something wrong. (more information with getActionErrors())
     */
    @Deprecated
    public String store() {
        Task storable = new Task();
        //Backlog backlog;

        if (task.getName().equals("")) {
            super.addActionError(super.getText("task.missingName"));
            return Action.ERROR;
        }

        if (taskId > 0) {
            storable = taskDAO.get(taskId);
            if (storable == null) {
                super.addActionError(super.getText("task.notFound"));
                return Action.ERROR;
            }
        }

        this.fillStorable(storable);

        if (super.hasActionErrors()) {
            return Action.ERROR;
        }

        if (taskId == 0) {
            // Set rank for task temporarily to -1 to indicate new item 
            storable.setRank(-1);
            taskId = (Integer) taskDAO.create(storable);
            // Get rank for new task.
            Task lowestRankedTask = taskDAO.getLowestRankedTask(storable.getBacklogItem());
            if(lowestRankedTask == null || lowestRankedTask.getRank() < 0) {
                storable.setRank(0);
            }
            else {
                storable.setRank(lowestRankedTask.getRank() + 1);
            }
            taskDAO.store(storable);
        }
        else
            taskDAO.store(storable);
                
        /* Update effort history */
        backlogItemDAO.get(backlogItemId).getBacklog();

        return Action.SUCCESS;
    }

    /**
     * Stores a new task.
     * 
     * @return The ID of the newly stored Task
     */
    public Integer storeNew() {

        if (task.getName().equals("")) {
            super.addActionError(super.getText("task.missingName"));
            return null;
        }

        Task storable = new Task();
        this.fillStorable(storable);

        if (super.hasActionErrors()) {
            return null;
        }

        return (Integer) taskDAO.create(storable);
    }

    /**
     * Deletes a task (based on taskId set)
     * 
     * @return Action.SUCCESS if task was deleted or Action.ERROR if task wasn't
     *         found
     */
    public String delete() {
        task = taskDAO.get(taskId);
        
        if (task == null) {
            super.addActionError(super.getText("task.notFound"));
            return Action.ERROR;
        }
        
        BacklogItem backlogItem = task.getBacklogItem();
        backlogItemId = backlogItem.getId();
        backlogItem.getTasks().remove(task);
        task.setBacklogItem(null);
        taskDAO.remove(task);

        return Action.SUCCESS;
    }

    /**
     * Transforms a task to BacklogItem
     * 
     * @author hhaataja
     */
    public String transformToBacklogItem() {
        // First store the task if any changes were made
        this.store();

        Task storedTask = new Task();
        BacklogItem backlogItem = new BacklogItem();

        if (task.getName().equals("")) {
            super.addActionError(super.getText("task.missingName"));
            return Action.ERROR;
        }

        // Get Task from database
        storedTask = taskDAO.get(taskId);
        if (storedTask == null) {
            super.addActionError(super.getText("task.notFound"));
            return Action.ERROR;
        }
        // Inherit from task's backlogItem
        backlogItem.setBacklog(storedTask.getBacklogItem().getBacklog());
        //TODO: Deprecated method
        backlogItem.setAssignee(storedTask.getBacklogItem().getAssignee());
        backlogItem.setIterationGoal(storedTask.getBacklogItem()
                .getIterationGoal());
        backlogItem.setPriority(storedTask.getBacklogItem().getPriority());

        // Inherit from task
        backlogItem.setName(storedTask.getName());
        backlogItem.setDescription(storedTask.getDescription());
        backlogItem.setState(storedTask.getState());
        // These are null because they are not defined for task
        backlogItem.setEffortLeft(null);
        backlogItem.setOriginalEstimate(null);

        // Remove the persistent task because it has been transformed to backlog
        // item
        taskDAO.remove(storedTask);
        backlogItemDAO.store(backlogItem);

        this.setBacklogItemId(backlogItem.getId());

        return Action.SUCCESS;
    }

    protected void fillStorable(Task storable) {
        if (storable.getBacklogItem() == null) {
            BacklogItem backlogItem = backlogItemDAO.get(backlogItemId);
            if (backlogItem == null) {
                super.addActionError(super.getText("backlogItem.notFound"));
                return;
            }
            storable.setBacklogItem(backlogItem);
            backlogItem.getTasks().add(storable);
            storable.setCreator(SecurityUtil.getLoggedUser());
        }

        if (task.getName().equals("")|| 
                this.task.getName().trim().equals("")) {
            super.addActionError(super.getText("task.missingName"));
            return;
        }
        //storable.setPriority(task.getPriority());
        storable.setState(task.getState());
        storable.setName(task.getName());
        storable.setDescription(task.getDescription());
    }

    // TODO - should this be so?
    public int getProjectId() {
        return taskId;
    }

    // TODO - should this be so?
    public void setProjectId(int projectId) {
        this.taskId = projectId;
    }

    public Task getTask() {
        return task;
    }

    // TODO - is this used/needed?
    public void setProject(Task task) {
        this.task = task;
    }

    public void setTaskDAO(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    protected TaskDAO getTaskDAO() {
        return this.taskDAO;
    }

    public int getBacklogItemId() {
        return backlogItemId;
    }

    public void setBacklogItemId(int backlogItemId) {
        this.backlogItemId = backlogItemId;
    }

    public int getTaskId() {
        return taskId;
    }

    /**
     * Set the task id (used by edit() and delete() -methods)
     * 
     * @param taskId
     *                Id of the wanted task.
     */
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
        this.backlogItemDAO = backlogItemDAO;
    }

    public String moveTaskUp() {
        try {
            this.taskBusiness.rankTaskUp(taskId);
            this.backlogItemId = this.taskBusiness.getTaskById(taskId)
                    .getBacklogItem().getId();
            return CRUDAction.AJAX_SUCCESS;
        } catch (ObjectNotFoundException onfe) {
            addActionError(onfe.getMessage());
            return CRUDAction.AJAX_ERROR;
        }
    }

    public String moveTaskDown() {
        try {
            this.taskBusiness.rankTaskDown(taskId);
            this.backlogItemId = this.taskBusiness.getTaskById(taskId)
                    .getBacklogItem().getId();
            return CRUDAction.AJAX_SUCCESS;
        } catch (ObjectNotFoundException onfe) {
            addActionError(onfe.getMessage());
            return CRUDAction.AJAX_ERROR;
        }
    }

    public String moveTaskBottom() {
        try {
            this.taskBusiness.rankTaskBottom(taskId);
            this.backlogItemId = this.taskBusiness.getTaskById(taskId)
                    .getBacklogItem().getId();
            return CRUDAction.AJAX_SUCCESS;
        } catch (ObjectNotFoundException onfe) {
            addActionError(onfe.getMessage());
            return CRUDAction.AJAX_ERROR;
        }
    }

    public String moveTaskTop() {
        try {
            this.taskBusiness.rankTaskTop(taskId);
            this.backlogItemId = this.taskBusiness.getTaskById(taskId)
                    .getBacklogItem().getId();
            return CRUDAction.AJAX_SUCCESS;
        } catch (ObjectNotFoundException onfe) {
            addActionError(onfe.getMessage());
            return CRUDAction.AJAX_ERROR;
        }
    }

    public String getJsonData() {
        return jsonData;
    }

}