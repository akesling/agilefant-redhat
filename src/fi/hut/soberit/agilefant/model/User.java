package fi.hut.soberit.agilefant.model;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import fi.hut.soberit.agilefant.web.page.PageItem;

@Entity
public class User implements PageItem {
	
	private int id;
	private String password;
	private String loginName;
	private String fullName;
	private Collection<Task> assignments = new HashSet<Task>();	
	private Collection<Backlog> 	backlogs     = new HashSet<Backlog>();
	private Collection<BacklogItem> backlogItems = new HashSet<BacklogItem>();
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(nullable = false)
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
		
	public String getFullName() {
		return fullName;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	
	public String getLoginName() {
		return loginName;
	}
	
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
    @OneToMany(mappedBy="assignee")
	public Collection<Task> getAssignments() {
		return assignments;
	}

	public void setAssignments(Collection<Task> assignments) {
		this.assignments = assignments;
	}
	@Transient
	public Collection<PageItem> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}
	@Transient
	public String getName() {
		// TODO Auto-generated method stub
		return this.loginName;
	}
	@Transient
	public PageItem getParent() {
		// TODO Auto-generated method stub
		return null;
	}
	@Transient
	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	@OneToMany(mappedBy="assignee")
	public Collection<BacklogItem> getBacklogItems() {
		return backlogItems;
	}
	public void setBacklogItems(Collection<BacklogItem> backlogItems) {
		this.backlogItems = backlogItems;
	}

	@OneToMany(mappedBy="assignee")
	public Collection<Backlog> getBacklogs() {
		return backlogs;
	}
	public void setBacklogs(Collection<Backlog> backlogs) {
		this.backlogs = backlogs;
	}
}
