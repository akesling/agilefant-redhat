package fi.hut.soberit.agilefant.business;

import fi.hut.soberit.agilefant.model.BacklogItem;

/**
 * Interface for business functionality related to backlog items.
 * 
 * @author Mika Salminen
 * 
 */
public interface BacklogItemBusiness {

    /**
     * Returns backlog item by its id.
     * 
     * @param backlogItemId the id of wanted backlog item
     *                
     * @return backlog item for the id
     */
    public BacklogItem getBacklogItem(int backlogItemId);
    
    /**
     * Removes backlog item specified by id.
     *
     * @param backlogItemId id of backlog item to be removed
     * 
     * @return true if item was removed, else false
     */
    
    public boolean removeBacklogItem(int backlogItemId);
}
