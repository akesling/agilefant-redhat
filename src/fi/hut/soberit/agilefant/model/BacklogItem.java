package fi.hut.soberit.agilefant.model;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import fi.hut.soberit.agilefant.web.page.PageItem;

@Entity
public class BacklogItem implements PageItem, Assignable {
	
	private int id;
	private Priority priority;
	private String name;
	private String description;
	private Backlog backlog;
	private Collection<Task> tasks = new HashSet<Task>();
	private AFTime remainingEffortEstimate;
	private AFTime taskEffortLeft;
	private User assignee;

	@Type(type="af_time")
	@Formula(value="(select SEC_TO_TIME(SUM(TIME_TO_SEC(t.effortEstimate))) from Task t where t.backlogItem_id = id)")
	public AFTime getTaskEffortLeft() {
		return taskEffortLeft;
	}
	
	protected void setTaskEffortLeft(AFTime taskEffortLeft) {
		this.taskEffortLeft = taskEffortLeft;
	}
	
	@Type(type="af_time")
	public AFTime getRemainingEffortEstimate() {
		return remainingEffortEstimate;
	}
	
	public void setRemainingEffortEstimate(AFTime remainingEffortEstimate) {
		this.remainingEffortEstimate = remainingEffortEstimate;
	}
	
	public String getDescription() {
	    return description;
	}
	public void setDescription(String description) {
	    this.description = description;
	}
	
	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(nullable = false)	
	public int getId() {
	    return id;
	}
	public void setId(int id) {
	    this.id = id;
	}
	
	//@Column(nullable = false)
	public String getName() {
	    return name;
	}
	public void setName(String name) {
	    this.name = name;
	}
	
	@OneToMany(mappedBy="backlogItem")
	public Collection<Task> getTasks() {
	    return tasks;
	}
	public void setTasks(Collection<Task> tasks) {
	    this.tasks = tasks;
	}
	
	@ManyToOne
	@JoinColumn (nullable = false)
	public Backlog getBacklog() {
	    return backlog;
	}
		
	public void setBacklog(Backlog backlog) {
	    this.backlog = backlog;
	}
	
	@Transient
	public Collection<PageItem> getChildren() {
		Collection<PageItem> c = new HashSet<PageItem>(this.tasks.size());
		c.addAll(this.tasks);
		return c;
	}
	@Transient
	public PageItem getParent() {
		//TODO: do some checks
		return (PageItem)getBacklog();
	}
	@Transient
	public boolean hasChildren() {
		return this.tasks.size() > 0 ? true : false;
	}
		
	@Type(type="fi.hut.soberit.agilefant.db.hibernate.EnumUserType",
			parameters = {
				@Parameter(name="useOrdinal", value="true"),
				@Parameter(name="enumClassName", value="fi.hut.soberit.agilefant.model.Priority")
			}
	)
	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}	
	
	@ManyToOne
	public User getAssignee() {
		return assignee;
	}
	public void setAssignee(User assignee) {
		this.assignee = assignee;
	}	
}
