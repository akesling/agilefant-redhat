package fi.hut.soberit.agilefant.business.impl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.hut.soberit.agilefant.business.HourEntryBusiness;
import fi.hut.soberit.agilefant.business.TimesheetBusiness;
import fi.hut.soberit.agilefant.db.BacklogDAO;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogHourEntry;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.BacklogItemHourEntry;
import fi.hut.soberit.agilefant.model.HourEntry;
import fi.hut.soberit.agilefant.util.BacklogTimesheetNode;
import fi.hut.soberit.agilefant.util.CalendarUtils;

public class TimesheetBusinessImpl implements TimesheetBusiness {
    private BacklogDAO backlogDAO;
    private HourEntryBusiness hourEntryBusiness;
    private List<BacklogTimesheetNode> roots;
    
    /** The map contains all nodes that are already in the tree, mapped by backlog id.
     *  It is used to avoid creating duplicate instances of the same node, and to group
     *  backlogs properly.   
     */
    private Map<Integer, BacklogTimesheetNode> nodes;
    
    private Date startDate, endDate;
    
    private Set<Integer> userIds;
    
    /**
     * {@inheritDoc}
     */
    public List<BacklogTimesheetNode> generateTree(List<Integer> backlogIds,
                                                   String startDateString, String endDateString,
                                                   Set<Integer> userIds)
            throws IllegalArgumentException{
        
        Backlog backlog, parent;
        BacklogTimesheetNode backlogNode, parentNode, childNode;
        
        try{
            if(startDateString == null || startDateString.trim().length() == 0)
                this.startDate = null;
            else
                this.startDate = CalendarUtils.parseDateFromString(startDateString);
            
            if(endDateString == null || endDateString.trim().length() == 0)
                this.endDate = null;
            else
                this.endDate = CalendarUtils.parseDateFromString(endDateString);
            
        }catch(ParseException e){
            System.err.println("Error in parsing date");
            throw new IllegalArgumentException("Error in parsing date");
        }
        
        if(this.startDate != null && this.endDate != null && this.startDate.after(this.endDate))
            throw new IllegalArgumentException("End date cannot be before start date");
        
        this.userIds = userIds;
        
        roots = new ArrayList<BacklogTimesheetNode>();
        nodes = new HashMap<Integer, BacklogTimesheetNode>();
      
        for(int id : backlogIds){
            backlog = backlogDAO.get(id);
            if(backlog != null){
                if(!nodes.containsKey(backlog.getId())){
                    if((parent = (Backlog) backlog.getParent()) != null){
                        backlogNode = new BacklogTimesheetNode(backlog, true, this);
                        nodes.put(id, backlogNode);
                        
                        if((parentNode = nodes.get(parent.getId())) != null){
                            parentNode.addChildBacklog(backlogNode);
                        }else{
                            parentNode = new BacklogTimesheetNode(parent, false, this);
                            parentNode.addChildBacklog(backlogNode);
                            nodes.put(parentNode.getBacklog().getId(), parentNode);
                            childNode = parentNode;
                        
                            while((parent = (Backlog) childNode.getBacklog().getParent()) != null){
                                if((parentNode = nodes.get(parent.getId())) != null){
                                    parentNode.addChildBacklog(childNode);
                                    break;
                                }else{
                                    parentNode = new BacklogTimesheetNode(parent, false, this);
                                    parentNode.addChildBacklog(childNode);
                                    nodes.put(parentNode.getBacklog().getId(), parentNode);
                                    childNode = parentNode;
                                }
                            }
                            
                            if(!roots.contains(parentNode))
                                roots.add(parentNode);
                            
                        }
                    }else{ // The node is a root (Product)
                        roots.add(new BacklogTimesheetNode(backlog, true, this));
                    }
                }else{ // Node is already in the tree
                    BacklogTimesheetNode node = nodes.get(backlog.getId());
                    if(!node.isExpanded())
                        node.expandChildren(this, true);
                }
            }
        }
        
        return roots;
    }

    /**
     * {@inheritDoc}
     */
    public List<? extends HourEntry> getFilteredHourEntries(BacklogItem backlogItem){
        Collection<BacklogItemHourEntry> hourEntries;
        List<BacklogItemHourEntry> filteredHourEntries = new ArrayList<BacklogItemHourEntry>();
        
        hourEntries = backlogItem.getHourEntries();
        
        if(hourEntries != null){
            for(BacklogItemHourEntry hourEntry : hourEntries){
                if(passesFilters(hourEntry))
                    filteredHourEntries.add(hourEntry);
            }
        }
        
        return filteredHourEntries;
    }
    
    /**
     * {@inheritDoc}
     */
    public List<? extends HourEntry> getFilteredHourEntries(Backlog backlog){
        List<BacklogHourEntry> hourEntries;
        List<BacklogHourEntry> filteredHourEntries = new ArrayList<BacklogHourEntry>();
        
        hourEntries = hourEntryBusiness.getEntriesByParent(backlog);
        
        if(hourEntries != null){
            for(BacklogHourEntry hourEntry : hourEntries){
                if(passesFilters(hourEntry))
                    filteredHourEntries.add(hourEntry);
            }
        }
        
        return filteredHourEntries;
    }
    
    public Collection<BacklogItem> getBacklogItems(Backlog bl) {
        return this.backlogDAO.getBlisWithSpentEffortByBacklog(bl,startDate,endDate,userIds);
    }
    
    /**
     * Check whether the given hourEntry passes the filters that were given in the time sheet query
     */
    private boolean passesFilters(HourEntry hourEntry){

        if(hourEntry.getDate() == null)
            return false;
        
        if((this.startDate != null && hourEntry.getDate().before(this.startDate)) | 
           (this.endDate != null && hourEntry.getDate().after(this.endDate)))
            return false;
        
        if(this.userIds.size() > 0 && !this.userIds.contains(hourEntry.getUser().getId()))
            return false;

        return true;
    }
    
    public AFTime calculateRootSum(List<BacklogTimesheetNode> roots) {
        AFTime sum = new AFTime(0);
        if(roots == null) {
            return sum;
        }
        for(BacklogTimesheetNode node : roots) {
            sum.add(node.getHourTotal());
        }
        return sum;
    }
    public BacklogDAO getBacklogDAO() {
        return backlogDAO;
    }

    public void setBacklogDAO(BacklogDAO backlogDAO) {
        this.backlogDAO = backlogDAO;
    }

    /**
     * {@inheritDoc}
     */
    public List<BacklogTimesheetNode> getRoots() {
        return roots;
    }

    public void setRoots(List<BacklogTimesheetNode> roots) {
        // Not available
    }

    /**
     * {@inheritDoc}
     */
    public HourEntryBusiness getHourEntryBusiness() {
        return hourEntryBusiness;
    }

    public void setHourEntryBusiness(HourEntryBusiness hourEntryBusiness) {
        this.hourEntryBusiness = hourEntryBusiness;
    }

    /**
     * {@inheritDoc}
     */
    public Map<Integer, BacklogTimesheetNode> getNodes() {
        return nodes;
    }
}
