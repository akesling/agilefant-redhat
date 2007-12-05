package fi.hut.soberit.agilefant.business;

import java.util.Collection;

import fi.hut.soberit.agilefant.model.ActivityType;
import fi.hut.soberit.agilefant.model.Project;

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
     * Get all project types.
     * 
     * @return collection of project types
     */
    public Collection<ActivityType> getProjectTypes();
}
