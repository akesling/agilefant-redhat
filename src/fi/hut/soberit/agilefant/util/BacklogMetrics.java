package fi.hut.soberit.agilefant.util;

import fi.hut.soberit.agilefant.model.AFTime;

public class BacklogMetrics {
    private AFTime dailyVelocity = new AFTime(0);
    private AFTime scopingNeeded = new AFTime(0);
    private Integer scheduleVariance = new Integer(0);
    private Integer completedItems = new Integer(0);
    private Integer totalItems = new Integer(0);
    private Integer percentDone = new Integer(0);
    private boolean backlogOngoing = true;
    
    /*
     * Autogenerated list of getters and setters
     */
    
    public Integer getPercentDone() {
        return percentDone;
    }
    public void setPercentDone(Integer percentDone) {
        this.percentDone = percentDone;
    }
    public AFTime getDailyVelocity() {
        return dailyVelocity;
    }
    public void setDailyVelocity(AFTime dailyVelocity) {
        this.dailyVelocity = dailyVelocity;
    }
    public AFTime getScopingNeeded() {
        return scopingNeeded;
    }
    public void setScopingNeeded(AFTime scopingNeeded) {
        this.scopingNeeded = scopingNeeded;
    }
    public Integer getScheduleVariance() {
        return scheduleVariance;
    }
    public void setScheduleVariance(Integer scheduleVariance) {
        this.scheduleVariance = scheduleVariance;
    }
    public Integer getCompletedItems() {
        return completedItems;
    }
    public void setCompletedItems(Integer completedItems) {
        this.completedItems = completedItems;
    }
    public Integer getTotalItems() {
        return totalItems;
    }
    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }
    public boolean isBacklogOngoing() {
        return backlogOngoing;
    }
    public void setBacklogOngoing(boolean backlogOngoing) {
        this.backlogOngoing = backlogOngoing;
    }
}
