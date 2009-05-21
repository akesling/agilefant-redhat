package fi.hut.soberit.agilefant.web;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;

import fi.hut.soberit.agilefant.business.HourEntryBusiness;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.ProjectDAO;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.model.HourEntry;
import fi.hut.soberit.agilefant.model.TimesheetLoggable;
import fi.hut.soberit.agilefant.util.CalendarUtils;
import fi.hut.soberit.agilefant.util.DailySpentEffort;
import flexjson.JSONSerializer;


public class HourEntryAction extends ActionSupport implements CRUDAction {
    private static final long serialVersionUID = -3817350069919875136L;
    private int hourEntryId;
    private HourEntry hourEntry;
    private HourEntryBusiness hourEntryBusiness;
    private BacklogItemDAO backlogItemDAO;
    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    private int userId = 0;
    private TimesheetLoggable target;
    private String date;
    private Date internalDate;
    private int backlogId = 0;
    private int backlogItemId = 0;
    private int iterationId;
    private int projectId;
    private int productId;
    private String jsonData = "";
    private int week = -1;
    private int prevWeek;
    private int nextWeek;
    private int prevYear;
    private int nextYear;
    private int year = 0;
    private int day = 1;
    
    private List<DailySpentEffort> dailyEffort;
    private List<Object[]> weeks;
    private List<HourEntry> effortEntries;
    
    //multi edit
    private Map<Integer, String[]> userIdss = new HashMap<Integer,String[]>();
    private Map<Integer, String[]> dates = new HashMap<Integer, String[]>();
    private Map<Integer, String[]> descriptions = new HashMap<Integer, String[]>();
    private Map<Integer, String[]> efforts = new HashMap<Integer, String[]>();
    
    //private Map<Integer, String> userIds = new HashMap<Integer, String>();
    private Set<Integer> userIds = new HashSet<Integer>();
    
    //private Log logger = LogFactory.getLog(getClass());

    /**
     * {@inheritDoc}
     */
    public String create() {
        hourEntryId = 0;
        hourEntry = new HourEntry();
        hourEntry.setDate(new Date());
        return Action.SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    public String delete() {
        HourEntry h = hourEntryBusiness.getHourEntryById(hourEntryId);
        if (h == null) {
            super.addActionError(super.getText("hourEntry.notFound"));
            return CRUDAction.AJAX_ERROR;
        }
        hourEntryBusiness.remove(hourEntryId);
        return CRUDAction.AJAX_SUCCESS;
    }

    /**
     * {@inheritDoc}
     */
    public String edit() {
        
        hourEntry = hourEntryBusiness.getHourEntryById(hourEntryId);        
        if (hourEntry == null) {
            super.addActionError(super.getText("hourEntry.notFound"));
            create();
            return Action.ERROR;
        }
        internalDate = hourEntry.getDate();
        return Action.SUCCESS;
    }
    private TimesheetLoggable getParent() {
        TimesheetLoggable parent = null;
        if(backlogItemId > 0 ) {
            parent = backlogItemDAO.get(backlogItemId);
        }else if( backlogId > 0 ){
            parent = projectDAO.get(backlogId);
        }
        return parent;
    }
    /**
     * {@inheritDoc}
     * TODO: check that target is valid
     */
    public String store() {
        HourEntry storable = new HourEntry();
        if (hourEntryId > 0) {
            storable = hourEntryBusiness.getHourEntryById(hourEntryId);
            if (storable == null) {
                super.addActionError(super.getText("hourEntry.notFound"));
                return CRUDAction.AJAX_ERROR;
            }
        }
        this.fillStorable(storable);
        if (super.hasActionErrors()) {
            return CRUDAction.AJAX_ERROR;
        }
        //Existing entries cannot be "shared"
        TimesheetLoggable parent = getParent();
        if(hourEntryId > 0 || userId > 0) {
            storable.setUser(userDAO.get(userId));
            jsonData = new JSONSerializer().serialize(hourEntryBusiness.store(parent,storable));
        } else if(userId == 0) {
            if(userIds.size() < 1) {
                super.addActionError(super.getText("hourEntry.noUsers"));
                return CRUDAction.AJAX_ERROR;
            }
            hourEntryBusiness.addHourEntryForMultipleUsers(parent,storable, userIds);
        } 
        return CRUDAction.AJAX_SUCCESS;
    }
    
    public String multiEdit() {
        hourEntryBusiness.updateMultiple(userIdss, dates, efforts, descriptions); 
        return CRUDAction.AJAX_SUCCESS;
    }
    protected void fillStorable(HourEntry storable) {
        storable.setDate(this.internalDate);
        storable.setDescription(this.hourEntry.getDescription());
        storable.setTimeSpent(this.hourEntry.getTimeSpent());
        storable.setUser(this.hourEntry.getUser());
    }
    
    public String getDaySumsByWeek() {
        Calendar cal = Calendar.getInstance();
        if(week == -1) {
            week = cal.get(Calendar.WEEK_OF_YEAR);
            year = cal.get(Calendar.YEAR);
        } else {
            cal.set(Calendar.YEAR, this.year);
            cal.set(Calendar.WEEK_OF_YEAR, this.week);
        }
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        nextWeek = cal.get(Calendar.WEEK_OF_YEAR);
        nextYear = cal.get(Calendar.YEAR);
        cal.add(Calendar.WEEK_OF_YEAR, -2);
        prevWeek = cal.get(Calendar.WEEK_OF_YEAR);
        prevYear = cal.get(Calendar.YEAR);
        this.dailyEffort = this.hourEntryBusiness.getDailySpentEffortByWeekAndUser(this.week, this.year, this.userId);
        cal.add(Calendar.WEEK_OF_YEAR, -9);
        this.weeks = new ArrayList<Object[]>();
        for(int i = 0; i < 20; i++) {
            Object[] cur = new Object[] {cal.get(Calendar.YEAR),cal.get(Calendar.WEEK_OF_YEAR)};
            this.weeks.add(cur);
            cal.add(Calendar.WEEK_OF_YEAR, 1);
        }
        return SUCCESS;
    }
    
    public String getHourEntriesByUserAndWeek() {
        this.effortEntries = this.hourEntryBusiness.getEntriesByUserAndDay(day, year, userId);
        return SUCCESS;
    }
    
    

    public int getHourEntryId() {
        return hourEntryId;
    }

    public void setHourEntryId(int hourEntryId) {
        this.hourEntryId = hourEntryId;
    }

    public HourEntry getHourEntry() {
        return hourEntry;
    }

    public void setHourEntry(HourEntry hourEntry) {
        this.hourEntry = hourEntry;
    }

    public HourEntryBusiness getHourEntryBusiness() {
        return hourEntryBusiness;
    }

    public void setHourEntryBusiness(HourEntryBusiness hourEntryBusiness) {
        this.hourEntryBusiness = hourEntryBusiness;
    }

    public TimesheetLoggable getTarget() {
        //TODO: Ugly workaround, refactor?
        this.target = getParent();
        return target;
    }

    public void setTarget(TimesheetLoggable parent) {
        this.target = parent;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String sDate) {
        try {
            this.internalDate = CalendarUtils.parseDateFromString(sDate);
        } catch(ParseException e) {
            
        }
    }

    public int getBacklogId() {
        return backlogId;
    }

    public void setBacklogId(int backlogId) {
        this.backlogId = backlogId;
    }

    public int getBacklogItemId() {
        return backlogItemId;
    }

    public void setBacklogItemId(int backlogItemId) {
        this.backlogItemId = backlogItemId;
    }

    public BacklogItemDAO getBacklogItemDAO() {
        return backlogItemDAO;
    }

    public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
        this.backlogItemDAO = backlogItemDAO;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public UserDAO getUserDAO() {
        return userDAO;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public int getIterationId() {
        return iterationId;
    }

    public void setIterationId(int iterationId) {
        this.iterationId = iterationId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public ProjectDAO getProjectDAO() {
        return projectDAO;
    }

    public void setProjectDAO(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public Set<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<Integer> userIds) {
        this.userIds = userIds;
    }

    public void setUserIdss(Map<Integer, String[]> userIdss) {
        this.userIdss = userIdss;
    }

    public void setDates(Map<Integer, String[]> dates) {
        this.dates = dates;
    }

    public void setDescriptions(Map<Integer, String[]> descriptions) {
        this.descriptions = descriptions;
    }

    public void setEfforts(Map<Integer, String[]> efforts) {
        this.efforts = efforts;
    }

    public Map<Integer, String[]> getUserIdss() {
        return userIdss;
    }

    public Map<Integer, String[]> getDates() {
        return dates;
    }

    public Map<Integer, String[]> getDescriptions() {
        return descriptions;
    }

    public Map<Integer, String[]> getEfforts() {
        return efforts;
    }

    public String getJsonData() {
        return jsonData;
    }

    public List<DailySpentEffort> getDailyEffort() {
        return dailyEffort;
    }

    public void setDailyEffort(List<DailySpentEffort> dailyEffort) {
        this.dailyEffort = dailyEffort;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getWeek() {
        return this.week;
    }
    
    public int getPrevWeek() {
        return prevWeek;
    }

    public int getNextWeek() {
        return nextWeek;
    }

    public void setYear(int year) {
        this.year = year;
    }
    public int getYear() {
        return this.year;
    }

    public int getPrevYear() {
        return prevYear;
    }

    public int getNextYear() {
        return nextYear;
    }

    public List<HourEntry> getEffortEntries() {
        return effortEntries;
    }

    public void setEffortEntries(List<HourEntry> effortEntries) {
        this.effortEntries = effortEntries;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public List<Object[]> getWeeks() {
        return weeks;
    }

    public void setWeeks(List<Object[]> weeks) {
        this.weeks = weeks;
    }
    
}
