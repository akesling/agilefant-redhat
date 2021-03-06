package fi.hut.soberit.agilefant.model;

import java.util.Collection;
import java.util.HashSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;

import fi.hut.soberit.agilefant.db.hibernate.Email;
import fi.hut.soberit.agilefant.web.page.PageItem;
import flexjson.JSON;

/**
 * Hibernate entity bean representing a user. User represents a person using the
 * webapp: it's more a thing of the implementation than anything conceptual.
 * <p>
 * The user carries information on username, password, full name and email. Also
 * there're different collections of items, where this user is assigned.
 */
@BatchSize(size=20)
@Entity
@Table(name = "user")
public class User implements PageItem {

    private int id;

    private String password;

    private String loginName;

    private String fullName;

    private String email;

    private String initials;
    
    private AFTime weekHours;
    
    private boolean enabled;

    private Collection<Assignment> assignments = new HashSet<Assignment>();

    private Collection<Backlog> backlogs = new HashSet<Backlog>();

    private Collection<BacklogItem> backlogItems = new HashSet<BacklogItem>();

    private Collection<Team> teams = new HashSet<Team>();

    /**
     * Get the id of this object.
     * <p>
     * The id is unique among all users.
     */
    // tag this field as the id
    @Id
    // generate automatically
    @GeneratedValue(strategy = GenerationType.AUTO)
    // not nullable
    @Column(nullable = false)
    @JSON
    public int getId() {
        return id;
    }

    /**
     * Set the id of this object.
     * <p>
     * You shouldn't normally call this.
     */
    public void setId(int id) {
        this.id = id;
    }

    /** Get full name. */
    @Type(type = "escaped_truncated_varchar")
    @JSON
    public String getFullName() {        
        return fullName;
    }

    /** Set full name. */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /** Get login name. */
    @Column(unique = true)
    @Type(type = "escaped_truncated_varchar")
    @JSON
    public String getLoginName() {
        return loginName;
    }

    /** Set login name. */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /** Get password. */
    @Type(type = "truncated_varchar")
    @JSON(include = false)
    public String getPassword() {
        return password;
    }

    /** Set password. */
    public void setPassword(String password) {
        this.password = password;
    }

    /** {@inheritDoc} */
    @Transient
    @JSON(include = false)
    public Collection<PageItem> getChildren() {
        // TODO Auto-generated method stub
        return null;
    }

    /** Get login name. */
    @Transient
    @JSON
    public String getName() {
        return this.loginName;
    }

    /** {@inheritDoc} */
    @Transient
    @JSON(include = false)
    public PageItem getParent() {
        return null;
    }

    /** {@inheritDoc} */
    @Transient
    public boolean hasChildren() {
        // TODO Auto-generated method stub
        return false;
    }

    /** Get backlog items, of which the user is responsible. */
    @ManyToMany(mappedBy = "responsibles", targetEntity = fi.hut.soberit.agilefant.model.BacklogItem.class, fetch = FetchType.LAZY)
    @JSON(include = false)
    public Collection<BacklogItem> getBacklogItems() {
        return backlogItems;
    }

    /** Set backlog items, where the user is assigned. */
    public void setBacklogItems(Collection<BacklogItem> backlogItems) {
        this.backlogItems = backlogItems;
    }

    /** Get backlogs, where the user is assigned. */
    @OneToMany(mappedBy = "assignee")
    @JSON(include = false)
    public Collection<Backlog> getBacklogs() {
        return backlogs;
    }

    /** Set backlogs, where the user is assigned. */
    public void setBacklogs(Collection<Backlog> backlogs) {
        this.backlogs = backlogs;
    }

    /** Set all Assignables, where this user is assigned. */
    @Transient
    @JSON(include = false)
    public Collection<Assignable> getAssignables() {
        Collection<Assignable> collection = new HashSet<Assignable>();

        collection.addAll(getBacklogs());
        collection.addAll(getBacklogItems());

        return collection;
    }

    /**
     * Get email addresses. Note that the field is validated to be a valid a
     * email address: an exception is thrown on store, if it's invalid.
     */
    @Column(nullable = true)
    @Email
    @Type(type = "truncated_varchar")
    @JSON
    public String getEmail() {
        return email;
    }

    /**
     * Set email addresses. Note that the field is validated to be a valid a
     * email address: an exception is thrown on store, if it's invalid.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    @OneToMany(mappedBy = "user")
    @JSON(include = false)
    public Collection<Assignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Collection<Assignment> assignments) {
        this.assignments = assignments;
    }

    /**
     * Get the user's initials.
     * 
     * @return the initials
     */
    @JSON
    public String getInitials() {
        return initials;
    }

    /**
     * Set the user's initials.
     * 
     * @param initials
     *                the initials to set
     */
    public void setInitials(String initials) {
        this.initials = initials;
    }  
       
    @Type(type = "af_time")
    @Column(columnDefinition = "integer default 144000")
    @JSON
    public AFTime getWeekHours() {       
        return weekHours;        
    }
    
    public void setWeekHours(AFTime hours) {
        this.weekHours = hours;
    } 
    
    /**
     * Get the user's teams.
     * 
     * @return the teams
     */
    @ManyToMany(
            targetEntity = fi.hut.soberit.agilefant.model.Team.class,
            fetch = FetchType.LAZY
    )
    @JoinTable(
            name = "team_user",
            joinColumns = {@JoinColumn( name = "User_id" )},
            inverseJoinColumns = {@JoinColumn( name = "Team_id")}
    )
    @JSON(include = false)
    public Collection<Team> getTeams() {
        return teams;
    }

    /**
     * Set the user's teams.
     * 
     * @param teams
     */
    public void setTeams(Collection<Team> teams) {
        this.teams = teams;
    }

    /**
     * Check, if the user is disabled
     * @return true, if user is disabled, false otherwise
     */
    @Column(columnDefinition = "boolean default 1")
    @JSON
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Set the user's enabled status.
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
