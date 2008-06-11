package fi.hut.soberit.agilefant.web;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;

import fi.hut.soberit.agilefant.business.TimesheetBusiness;
import fi.hut.soberit.agilefant.util.BacklogTimesheetNode;

/**
 * 
 * @author Vesa Pirila / Spider
 *
 */
public class TimesheetAction extends ActionSupport {

    private static final long serialVersionUID = -8988740967426943267L;
    
    private TimesheetBusiness timesheetBusiness;

    private List<BacklogTimesheetNode> products;
    
    private List<Integer> selected = new ArrayList<Integer>();
    
    private List<Integer> selUId = new ArrayList<Integer>();

    private int[] backlogIds;

    private String startDate;

    private String endDate;
    
    private String interval;
    
    private Map<Integer, String> userIds = new HashMap<Integer, String>();

    public Map<Integer, String> getUserIds() {
        return userIds;
    }

    public void setUserIds(Map<Integer, String> userIds) {
        this.userIds = userIds;
    }

    public int[] getBacklogIds() {
        return backlogIds;
    }

    public void setBacklogIds(int[] backlogIds) {
        this.backlogIds = backlogIds;
    }
    
    public String generateTree(){
        if(backlogIds == null) {
            addActionError("No backlogs selected.");
            return Action.ERROR;
        }
        try{
            products = timesheetBusiness.generateTree(backlogIds, startDate, endDate, userIds.keySet());
        }catch(IllegalArgumentException e){
            addActionError(e.getMessage());
            return Action.ERROR;
        }
        return Action.SUCCESS;
    }

    public TimesheetBusiness getTimesheetBusiness() {
        return timesheetBusiness;
    }

    public List<BacklogTimesheetNode> getProducts() {
        return products;
    }

    /**
     * This should not be used anywhere
     * @param products
     */
    public void setProducts(List<BacklogTimesheetNode> products) {
        this.products = products;
    }

    public void setTimesheetBusiness(TimesheetBusiness timesheetBusiness) {
        this.timesheetBusiness = timesheetBusiness;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public List<Integer> getSelected() {
        this.selected.clear();
        for(int sel : backlogIds) {
            this.selected.add(sel);
        }
        return selected;
    }

    public List<Integer> getSelUId() {
        for(int sel: userIds.keySet()) {
            this.selUId.add(sel);
        }
        return selUId;
    }
    
    
}
