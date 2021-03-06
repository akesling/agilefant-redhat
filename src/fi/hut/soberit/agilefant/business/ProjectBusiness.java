package fi.hut.soberit.agilefant.business;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.exception.OperationNotPermittedException;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.BusinessTheme;
import fi.hut.soberit.agilefant.model.Product;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.ProjectType;
import fi.hut.soberit.agilefant.model.User;
import fi.hut.soberit.agilefant.util.DailyWorkLoadData;
import fi.hut.soberit.agilefant.util.ProjectMetrics;
import fi.hut.soberit.agilefant.util.ProjectPortfolioData;

/**
 * Updates projects' ranks.
 * 
 * @author Aleksi Toivonen
 * 
 */

public interface ProjectBusiness {

    /**
     * Get all projects.
     * 
     * @return all projects.
     */
    public Collection<Project> getAll();

    /**
     * Get all ongoing projects that are ranked.
     * 
     * @return
     */
    public Collection<Project> getOngoingRankedProjects();

    /**
     * Get all ongoing projects that are not ranked.
     * 
     * @return
     */
    public Collection<Project> getOngoingUnrankedProjects();

    /**
     * Move project's rank up by one "visible" rank. May jump over many
     * projects, because projects that are ranked but not ongoing are affected.
     * 
     * @param project
     */
    public void moveUp(int projectId);

    /**
     * Move project's rank down by one "visible" rank. May jump over many
     * projects, because projects that are ranked but not ongoing are affected.
     * 
     * @param project
     */
    public void moveDown(int projectId);

    /**
     * Sets project's rank to the highest of all ranked projects.
     * 
     * @param project
     */
    public void moveToTop(int projectId);

    /**
     * Sets project's rank to the lowest of all ranked projects.
     * 
     * @param project
     */
    public void moveToBottom(int projectId);

    /**
     * Clears project's rank.
     * 
     * @param project
     */
    public void unrank(int projectId);

    /**
     * Get all project types.
     * 
     * @return collection of project types
     */
    public Collection<ProjectType> getProjectTypes();

    /**
     * Delete a project type.
     * 
     * @param projectTypeId
     *                id of the project type to be deleted.
     * @throws OperationNotPermittedException
     *                 if ProjectType has WorkTypes
     * @throws ObjectNotFoundException
     *                 if no such object exists
     */
    public void deleteProjectType(int projectTypeId)
            throws OperationNotPermittedException, ObjectNotFoundException;

    /**
     * Returns all items in project.
     * @param project Project which items are returned.
     * @return Collection of items.
     */
    public Collection<BacklogItem> getBlisInProjectAndItsIterations(
            Project project);
    
    /**
     * Get a ProjectPortfolioData object that contains information for the users column of the project
     * portfolio page.
     * 
     * @return A ProjectPortfolioData object
     */
    public ProjectPortfolioData getProjectPortfolioData();

    
    /**
     * Get a DailyWorkLoadData object that contains information for the users load
     * 
     * @return A ProjectPortfolioData object
     */
    public DailyWorkLoadData getDailyWorkLoadData(User user, int weeksAhead);
    
    /**
	* Constructs and returns a map containing informaton on whether a user has
	* been assigned to some backlog item under the given project without been
	* assigned to the project itself.
	*
	* @param Project
    *                project that the data is constructed for
	*
	* @return A map that maps a user to integer 1 if the user is not assigned to
	* the project but has work under it, otherwise user is mapped to integer 0
	*/
    public Map<User, Integer> getUnassignedWorkersMap(Project project);
    
    
    /**
     * 
     * @param backlogs
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Backlog> getProjectsAndIterationsInTimeFrame(List<Backlog> backlogs, 
            Date startDate, Date endDate);   
    /**
     * 
     * @param weeksAhead
     * @param items
     * @return
     */
    public HashMap<Integer, String> calculateEffortLefts(Date from, int weeksAhead,
            Map<Backlog, List<BacklogItem>> items);

    /**
     * 
     * @param from
     * @param weeksAhead
     * @param items
     * @param user
     * @return
     */
    public HashMap<Integer, String> calculateOverheads(Date from, int weeksAhead, List<Backlog> backlogs, User user);

    /**
     * Get the list of assignable users.
     * <p>
     * Returns all enabled users and users currently assigned to the project.
     * @param project
     * @return
     */
    public List<User> getAssignableUsers(Project project);
    
    
    public void removeAllHourEntries( Backlog backlog );
    /**
     * Calculates a product's projects' metrics and sets them to the projects.
     * @param product
     */
    public void calculateProjectMetrics(Product product);
    
    /**
     * Calculates a project's assignments' total overheads metrics.
     * @param project
     */
    public Map<Integer, AFTime> calculateTotalOverheads(Project project);
    
    /**
     * Get all project types as json string.
     * @return
     */
    public String getAllProjectTypesAsJSON();
    
    /**
     * Get target project type as json.
     * @param projectType
     * @return
     */
    public String getProjectTypeJSON(ProjectType projectType);
    
    /**
     * Get target project type as json.
     * @param projectTypeId
     * @return
     */
    public String getProjectTypeJSON(int projectTypeId);

    /**
     * Get total effort left, original estimate, done items, all items
     * and done percentage for all items contained in a project, including 
     * backlog items in project's iteration.
     * 
     * @param proj
     * @return
     */
    public ProjectMetrics getProjectMetrics(Project proj);
    
    /**
     * Get planned size totals for themes attached to a project, including
     * themes attached to project's iterations.
     * 
     * @param proj
     * @return
     */
    public Map<BusinessTheme,AFTime> formatThemeBindings(Project proj);

    int count();

    int countByProjectType(int projectTypeId);

}
