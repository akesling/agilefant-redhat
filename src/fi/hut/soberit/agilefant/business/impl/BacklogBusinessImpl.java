package fi.hut.soberit.agilefant.business.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import fi.hut.soberit.agilefant.business.BacklogBusiness;
import fi.hut.soberit.agilefant.business.BacklogItemBusiness;
import fi.hut.soberit.agilefant.business.HistoryBusiness;
import fi.hut.soberit.agilefant.db.AssignmentDAO;
import fi.hut.soberit.agilefant.db.BacklogDAO;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.IterationGoalDAO;
import fi.hut.soberit.agilefant.db.ProductDAO;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.Assignment;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.Priority;
import fi.hut.soberit.agilefant.model.Product;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.State;
import fi.hut.soberit.agilefant.model.User;
import fi.hut.soberit.agilefant.util.BacklogComparator;
import fi.hut.soberit.agilefant.util.BacklogLoadData;
import fi.hut.soberit.agilefant.util.EffortSumData;

/**
 * 
 * @author Teemu Ilmonen
 * 
 */
public class BacklogBusinessImpl implements BacklogBusiness {
    
    Logger log = Logger.getLogger(this.getClass());
    
    private BacklogItemDAO backlogItemDAO;

    private HistoryBusiness historyBusiness;

    private BacklogDAO backlogDAO;

    private UserDAO userDAO;

    private AssignmentDAO assignmentDAO;
    
    private IterationGoalDAO iterationGoalDAO;
    
    private BacklogItemBusiness backlogitemBusiness;

    // @Override
    public void deleteMultipleItems(int backlogId, int[] backlogItemIds)
            throws ObjectNotFoundException {
        Backlog backlog = backlogDAO.get(backlogId);
        if (backlog == null) {
            throw new ObjectNotFoundException("Backlog id " + backlogId
                    + " was invalid.");
        }

        for (int id : backlogItemIds) {
            Collection<BacklogItem> items = backlog.getBacklogItems();
            Iterator<BacklogItem> iterator = items.iterator();
            while (iterator.hasNext()) {
                BacklogItem item = iterator.next();
                if (item.getId() == id) {
                    iterator.remove();
                    backlogItemDAO.remove(id);
                }
            }
        }
        historyBusiness.updateBacklogHistory(backlog.getId());
    }

    public BacklogItem createBacklogItemToBacklog(int backlogId) {
        BacklogItem backlogItem = new BacklogItem();
        backlogItem = new BacklogItem();
        Backlog backlog = backlogDAO.get(backlogId);
        if (backlog == null)
            return null;
        backlogItem.setBacklog(backlog);
        backlog.getBacklogItems().add(backlogItem);
        return backlogItem;
    }

    /**
     * {@inheritDoc}
     */
    public void changePriorityOfMultipleItems(int[] backlogItemIds,
            Priority priority) throws ObjectNotFoundException {

        for (int id : backlogItemIds) {
            BacklogItem bli = backlogItemDAO.get(id);
            if (bli == null) {
                throw new ObjectNotFoundException(
                        "Could not change priority. Object with id " + id
                                + " was not found.");
            }
            bli.setPriority(priority);
        }
    }
    
    /** {@inheritDoc} */
    public void changeStateOfMultipleItems(int[] backlogItemIds, State state)
            throws ObjectNotFoundException {
        
        for (int id : backlogItemIds) {
            BacklogItem bli = backlogItemDAO.get(id);
            
            if (bli == null) {
                throw new ObjectNotFoundException(
                        "Could not change priority. Object with id " + id
                        + "was not found."
                );
            }
                     
            bli.setState(state);
            // If State is done, effort left is 0h.
            if (state == State.DONE)
                bli.setEffortLeft(new AFTime(0));
        }
    }
    
    /** {@inheritDoc} */
    public void changeIterationGoalOfMultipleItems(int[] backlogItemIds,
            int iterationGoalId) throws ObjectNotFoundException {
        
        for (int id : backlogItemIds) {
            BacklogItem bli = backlogItemDAO.get(id);
            
            if (bli == null) {
                throw new ObjectNotFoundException(
                        "Could not change iteration goal. Object with id " + id
                        + "was not found."
                );
            }
            bli.setIterationGoal(iterationGoalDAO.get(iterationGoalId));
        }
        
    }
    
    /** {@inheritDoc} */
    public void setResponsiblesForMultipleBacklogItems(int[] backlogItemIds,
            Set<Integer> responsibleIds) throws ObjectNotFoundException {
        
        // Generate the list of responsibles
        Set<User> users = new HashSet<User>();
        
        for (int uid : responsibleIds) {
            User user = userDAO.get(uid);
            users.add(user);
        }
        
        for (int id : backlogItemIds) {
            BacklogItem bli = backlogItemDAO.get(id);
            
            if (bli == null) {
                throw new ObjectNotFoundException(
                        "Could not change responsibles. Backlog item with id " + id
                        + "was not found."
                );
            }
            
            bli.setResponsibles(users);
        }
    }
    

    /**
     * {@inheritDoc}
     */
    public void moveMultipleBacklogItemsToBacklog(int backlogItemIds[],
            int targetBacklogId) throws ObjectNotFoundException {
        Backlog targetBacklog = backlogDAO.get(targetBacklogId);

        // Store source backlogs of the backlog items to be able to update their
        // history data.

        Set<Integer> sourceBacklogIds = new HashSet<Integer>();

        if (targetBacklog == null) {
            throw new ObjectNotFoundException("Target backlog with id: "
                    + targetBacklogId + " was not found.");
        }

        for (int id : backlogItemIds) {
            BacklogItem bli = backlogItemDAO.get(id);
            if (bli == null) {
                throw new ObjectNotFoundException("Backlog item with id: " + id
                        + " was not found.");
            }
            Backlog sourceBacklog = bli.getBacklog();

            if (sourceBacklog.getId() != targetBacklog.getId()) {

                // Store the source backlog ids into the set
                sourceBacklogIds.add(bli.getBacklog().getId());

                // Set originalestimate to current effortleft
                //bli.setOriginalEstimate(bli.getEffortLeft());

                // Remove iteration goal
                if (bli.getIterationGoal() != null) {
                    bli.getIterationGoal().getBacklogItems().remove(bli);
                    bli.setIterationGoal(null);
                }
                
                // Set backlog item's backlog to target backlog
                bli.setBacklog(targetBacklog);
                backlogItemDAO.store(bli);

                // Remove BLI from source backlog
                sourceBacklog.getBacklogItems().remove(bli);

                // Store source backlog
                backlogDAO.store(sourceBacklog);

                // Add backlog item to new Backlog's backlog item list
                targetBacklog.getBacklogItems().add(bli);
            }
        }

        backlogDAO.store(targetBacklog);

        // Update history data for source backlogs
        for (Integer sourceBacklogId : sourceBacklogIds) {
            historyBusiness.updateBacklogHistory(sourceBacklogId);

        }

        // Update history data for target backlog
        historyBusiness.updateBacklogHistory(targetBacklog.getId());
    }

    
    
    
    /** {@inheritDoc} * */
    public EffortSumData getEffortLeftSum(Collection<BacklogItem> bliList) {
        EffortSumData data = new EffortSumData();
        AFTime hours = new AFTime(0);
        int nonEstimatedBLIs = 0;
        for (BacklogItem bli : bliList) {
            if (bli.getEffortLeft() == null)
                nonEstimatedBLIs++;
            else
                hours.add(bli.getEffortLeft());            
        }
        data.setEffortHours(hours);
        data.setNonEstimatedItems(nonEstimatedBLIs);
        return data;
    }
    
    /** {@inheritDoc} * */
    public EffortSumData getEffortLeftResponsibleDividedSum(Collection<BacklogItem> bliList) {
        EffortSumData data = new EffortSumData();
        AFTime hours = new AFTime(0);
        for (BacklogItem bli : bliList) {
            if (bli.getEffortLeft() != null){  
               if(bli.getResponsibles() != null){ 
                   if(bli.getResponsibles().size() != 0){
                       hours.add(new AFTime(bli.getEffortLeft().getTime()/bli.getResponsibles().size()));
                   }else{
                       hours.add(bli.getEffortLeft());
                   }
               }else{
                   hours.add(bli.getEffortLeft());
               }
            }
        }
        data.setEffortHours(hours);
        return data;
    }

    /** {@inheritDoc} * */
    public EffortSumData getOriginalEstimateSum(Collection<BacklogItem> bliList) {
        EffortSumData data = new EffortSumData();
        AFTime hours = new AFTime(0);
        int nonEstimatedBLIs = 0;
        for (BacklogItem bli : bliList) {
            if (bli.getOriginalEstimate() == null)
                nonEstimatedBLIs++;
            else
                hours.add(bli.getOriginalEstimate());            
        }
        data.setEffortHours(hours);
        data.setNonEstimatedItems(nonEstimatedBLIs);
        return data;
    }
    
    public BacklogItemDAO getBacklogItemDAO() {
        return backlogItemDAO;
    }

    public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
        this.backlogItemDAO = backlogItemDAO;
    }

    public BacklogDAO getBacklogDAO() {
        return backlogDAO;
    }

    public void setBacklogDAO(BacklogDAO backlogDAO) {
        this.backlogDAO = backlogDAO;
    }

    public HistoryBusiness getHistoryBusiness() {
        return historyBusiness;
    }

    public void setHistoryBusiness(HistoryBusiness historyBusiness) {
        this.historyBusiness = historyBusiness;
    }

    public void setAssignments(int[] selectedUserIds, Map<String, Assignment> assignmentData, Backlog backlog) {
        if (backlog != null) {
            // Edit project assignments: remove all assignments, then create
            // some.
            Collection<Assignment> oldAssignments = backlog.getAssignments();
            for (Assignment ass : oldAssignments) {
                assignmentDAO.remove(ass);
            }
            Collection<User> users = getUsers(backlog, true);
            for (User user : users) {
                user.getAssignments().removeAll(oldAssignments);
                userDAO.store(user);
            }
            backlog.getAssignments().clear();
            backlogDAO.store(backlog);
            
            if (selectedUserIds != null) {
                for (int id : selectedUserIds) {
                    
                    User user = userDAO.get(id);
                    if (user != null) {
                        Assignment assignment = new Assignment(user, backlog);
                        if(assignmentData != null){
                            Assignment ass = assignmentData.get(id+"");
                            if(ass != null){
                                assignment.setDeltaOverhead(ass.getDeltaOverhead());
                            }
                        }
                        user.getAssignments().add(assignment);
                        backlog.getAssignments().add(assignment);
                        assignmentDAO.store(assignment);
                        userDAO.store(user);
                        backlogDAO.store(backlog);
                    }
                }
            }
        }
    }
    
    public void removeAssignments(User user) {
        if (user != null) {
            Collection<Assignment> assignments = assignmentDAO.getAll();
            for (Assignment ass : assignments) {
                if (ass.getUser().getId() == user.getId()) {
                    user.getAssignments().remove(ass);
                    ass.getBacklog().getAssignments().remove(ass);
                    userDAO.store(user);
                    backlogDAO.store(ass.getBacklog());
                    assignmentDAO.remove(ass);
                }
            }
        }
    }

    public Collection<User> getUsers(Backlog backlog, boolean areAssigned) {
        Collection<User> users;
        Collection<Assignment> assignments = backlog.getAssignments();
        users = new HashSet<User>();
        for (Assignment ass : assignments) {
            users.add(ass.getUser());
        }
        if (areAssigned)
            return users;
        else {
            Collection<User> allUsers = userDAO.getAll();
            allUsers.removeAll(users);
            return allUsers;
        }

    }
    
    /** {@inheritDoc} */
    public int getWeekdaysLeftInBacklog(Backlog backlog, Date from) {
        Date startDate = new Date(0);
        Date endDate = new Date(0);
        GregorianCalendar current = new GregorianCalendar();
        int numberOfWeekdays = 0;
        
        // Backlog shouldn't be a product
        if (backlog instanceof Product) {
            return 0;
        }
        else if (backlog instanceof Project) {
            startDate = (Date)((Project)backlog).getStartDate().clone();
            endDate = (Date)((Project)backlog).getEndDate().clone();
        }
        else {
            startDate = (Date)((Iteration)backlog).getStartDate().clone();
            endDate = (Date)((Iteration)backlog).getEndDate().clone();
        }
        
                
        // If project or iteration is past
        if (from.after(endDate)) {
            return 0;
        }
        
        // Check, which is later, start date or from date
        if (from.after(startDate)) {
            current.setTime(from);
        }
        else {
            current.setTime(startDate);
        }
        
        Date currentTime = current.getTime();
        
        // Add 1 to endDate to correct the time offset
        endDate.setDate(endDate.getDate() + 1);
        
        // Loop through the dates
        while (currentTime.before(endDate)) {
            if (current.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SUNDAY && 
                    current.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.SATURDAY) {
                numberOfWeekdays++;
            }
            current.add(GregorianCalendar.DATE, 1);
            currentTime = current.getTime();
        }
        
        return numberOfWeekdays;

    }
    
    
    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    public int getNumberOfDaysForBacklogOnWeek(Backlog backlog, Date time) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setFirstDayOfWeek(GregorianCalendar.MONDAY);
        cal.setTime(time);
        
        while (cal.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
            cal.add(GregorianCalendar.DATE, -1);
        }
        
        return getNumberOfDaysLeftForBacklogOnWeek(backlog, cal.getTime());
    }
    
    /** {@inheritDoc} */
    public int getNumberOfDaysLeftForBacklogOnWeek(Backlog backlog, Date time) {
        Date startDate = new Date();
        Date endDate = new Date();
        
        // Should not be a product
        if (backlog instanceof Product) {
            return 0;
        }
        else if (backlog instanceof Project) {
            startDate = (Date)((Project)backlog).getStartDate().clone();
            endDate = (Date)((Project)backlog).getEndDate().clone();
        }
        else if (backlog instanceof Iteration) {
            startDate = (Date)((Iteration)backlog).getStartDate().clone();
            endDate = (Date)((Iteration)backlog).getEndDate().clone();
        }
        
        // Set the time to start from
        GregorianCalendar cal = new GregorianCalendar();
        cal.setFirstDayOfWeek(GregorianCalendar.MONDAY);
        cal.setTime(time);
        cal.set(GregorianCalendar.HOUR, 0);
        cal.set(GregorianCalendar.MINUTE, 0);
        cal.set(GregorianCalendar.SECOND, 2);
        
        // Get the number of week
        int numberOfWeek = cal.get(GregorianCalendar.WEEK_OF_YEAR);
        int numberOfDays = 0;
        
        // Set the startdate to be at the start of the day
        startDate.setHours(0);
        startDate.setMinutes(0);
        startDate.setSeconds(0);
        endDate.setHours(23);
        endDate.setMinutes(59);
        endDate.setSeconds(59);
        
        // Loop through the week
        while (cal.get(GregorianCalendar.WEEK_OF_YEAR) == numberOfWeek) {
            // Break the loop on weekend
            if (cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SATURDAY ||
                cal.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY) {
                break;
            }
            
            // Check, if the date is in the backlog's timeframe
            if (startDate.before(cal.getTime()) && endDate.after(cal.getTime())) {
                numberOfDays++;
            }
            
            cal.add(GregorianCalendar.DATE, 1);
        }
        
        return numberOfDays;
    }
        
    /** {@inheritDoc} */
    public BacklogLoadData calculateBacklogLoadData(Backlog backlog, User user,
            Date from, int numberOfWeeks) {
        
        // Create the new data storage
        BacklogLoadData data = new BacklogLoadData();
        
        data.setBacklog(backlog);
        
        Collection<BacklogItem> bliList = new ArrayList<BacklogItem>();
        
        // Loop through the backlog items
        for (BacklogItem bli : backlog.getBacklogItems()) {
            if (bli.getResponsibles().contains(user)) {
                bliList.add(bli);
            }
        }
        
        // System.out.println("Number of backlog items: " + bliList.size());
        
        // Get the effort sum
        EffortSumData effortSum = getEffortLeftResponsibleDividedSum(bliList);
        //data.setTotalEffort(effortSum.getEffortHours());
        
        // Check if there are unestimated items
        if (effortSum.getNonEstimatedItems() > 0) {
            data.setUnestimatedItems(true);
        }
        
        // Get total number of days left in backlog
        int numberOfDaysLeft = getWeekdaysLeftInBacklog(backlog, from);
        if (numberOfDaysLeft == 0) {
            numberOfDaysLeft = 1;
        }
        
        // Calculate the effort per day
        long effortPerDay = (effortSum.getEffortHours().getTime()) / numberOfDaysLeft;
        
        
        // Loop through the weeks
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(from);
        for (int i = 0; i < numberOfWeeks; i++) {
            int daysOnWeek = getNumberOfDaysLeftForBacklogOnWeek(backlog, cal.getTime());
            AFTime effort = new AFTime(daysOnWeek * effortPerDay);
            AFTime totals = new AFTime(effort.getTime());
            
            /*System.out.println("Week " + cal.get(GregorianCalendar.WEEK_OF_YEAR) + 
                    " effort: " + effort);*/
            
            // Insert the week number
            data.getWeekNumbers().add(cal.get(GregorianCalendar.WEEK_OF_YEAR));
            
            // Set the weekly effort
            data.getEfforts().put(cal.get(GregorianCalendar.WEEK_OF_YEAR), effort);
            data.getTotalEffort().add(effort);
            
            // Set the weekly overhead
            if (backlog instanceof Project) {
                AFTime overhead = getOverheadForWeek((Project)backlog, user, daysOnWeek);
                data.getOverheads().put(cal.get(GregorianCalendar.WEEK_OF_YEAR), overhead);
                
                totals.add(overhead);
                data.getTotalOverhead().add(overhead);
                System.out.println("Overhead: " + overhead + "\nTotal overhead: " + data.getTotalOverhead());
            }
            
            // Set the weekly total
            data.getWeeklyTotals().put(cal.get(GregorianCalendar.WEEK_OF_YEAR), totals);
            
            // Next week
            cal.add(GregorianCalendar.WEEK_OF_YEAR, 1);
            // Roll to monday
            while (cal.get(GregorianCalendar.DAY_OF_WEEK) != GregorianCalendar.MONDAY) {
                cal.add(GregorianCalendar.DATE, -1);
            }
        }
        
        // Calculate the absolute total
        /*data.getAbsoluteTotal().add(data.getTotalEffort());
        data.getAbsoluteTotal().add(data.getTotalOverhead());*/
        
        /*System.out.println("Week\tEffort\tOverhead\tTotal");
        
        // Print loop for debugging
        for (Integer weekno : data.getWeekNumbers()) {
            System.out.println(weekno + "\t" +
                    data.getEfforts().get(weekno) + "\t" + 
                    data.getOverheads().get(weekno) + "\t" +
                    data.getWeeklyTotals().get(weekno));
        }
        
        System.out.println("Total\t" + data.getTotalEffort() + "\t" + data.getTotalOverhead() + 
                "\t" + data.getAbsoluteTotal());
        */
                
        return data;
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")
    public AFTime getOverheadForWeek(Project project, User user, int daysOnWeek) {
        AFTime totalOverhead = new AFTime(0);
        
        // Check that the user is assigned
        for (Assignment ass : project.getAssignments()) {
            if (ass.getUser().equals(user)) {
                if (project.getDefaultOverhead() != null) {
                    totalOverhead.add(project.getDefaultOverhead());
                }
                if (ass.getDeltaOverhead() != null) {
                    totalOverhead.add(ass.getDeltaOverhead());
                }
                break;
            }
        }
        
        // Calculate overhead per day, 5 days a week
        long overheadPerDay = (totalOverhead.getTime()) / 5;
        
        return new AFTime(daysOnWeek * overheadPerDay);
    }
    
    /** {@inheritDoc} */
    public List<Backlog> getUserBacklogs(User user, Date now, int weeksAhead) {
        ArrayList<Project> projects = new ArrayList<Project>();
        ArrayList<Backlog> backlogs = new ArrayList<Backlog>();
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setFirstDayOfWeek(GregorianCalendar.MONDAY);
        cal.setTime(now);
        Date endDate = cal.getTime();
        cal.add(GregorianCalendar.WEEK_OF_YEAR, weeksAhead);
        Date startDate = cal.getTime();
        
        
        // Iterate through users assignments
        for (Assignment ass : user.getAssignments()) {
            // If backlog is not a project, skip it
            if (!ass.getBacklog().getClass().equals(Project.class)) {
                continue;
            }
            
            projects.add((Project)ass.getBacklog());
        }
        
        Collections.sort(projects, new BacklogComparator());
        
        for (Project blog : projects) {
            if (!backlogs.contains(blog) &&
                    blog.getStartDate().before(startDate) &&
                    blog.getEndDate().after(endDate)) {
                backlogs.add(blog);
                
                // Get the ongoing iterations of the project
                for (Iteration it : blog.getIterations()) {
                    if (it.getStartDate().before(startDate) &&
                            it.getEndDate().after(endDate)) {
                        backlogs.add(it);
                    }
                }
                
            }
        }
        
        return backlogs;
    }

    public int getNumberOfAssignedUsers(Backlog backlog) {
        return getUsers(backlog, true).size();
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setAssignmentDAO(AssignmentDAO assignmentDAO) {
        this.assignmentDAO = assignmentDAO;
    }

    public void setIterationGoalDAO(IterationGoalDAO iterationGoalDAO) {
        this.iterationGoalDAO = iterationGoalDAO;
    }

    public void setBacklogitemBusiness(BacklogItemBusiness backlogitemBusiness) {
        this.backlogitemBusiness = backlogitemBusiness;
    }
}
