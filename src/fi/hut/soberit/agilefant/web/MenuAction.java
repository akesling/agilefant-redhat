package fi.hut.soberit.agilefant.web;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opensymphony.xwork.ActionContext;
import com.opensymphony.xwork.ActionSupport;

import fi.hut.soberit.agilefant.business.BacklogBusiness;
import fi.hut.soberit.agilefant.business.MenuBusiness;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.util.MenuData;
import fi.hut.soberit.agilefant.web.page.PageItem;

/**
 * The action class for populating the lefthand menu.
 * 
 * @author rjokelai
 */
public class MenuAction extends ActionSupport {

    private static final long serialVersionUID = -4817943410890249969L;

    private MenuBusiness menuBusiness;

    private BacklogBusiness backlogBusiness;

    private String requestId = "";
    private int backlogId;
    private PageItem pageitem;

    private MenuData menuData;
    
    private Set<Backlog> openBacklogs = new HashSet<Backlog>();
    private Map<PageItem, MenuData> openDatas = new HashMap<PageItem, MenuData>();

    private int numberOfItems = 0;
    
    // For ajax requests
    private String openString = "";
    
    private String navi = "";
    private String subnavi = "";


    /**
     * Generates the basic backlog hierarchy menu.
     */
    @Override
    @SuppressWarnings("unchecked")
    public String execute() throws Exception {
        // Parse the request id
        backlogId = parseRequestId(requestId);
        
        // Generate open backlogs list
        String open = "";
        if (ActionContext.getContext().getSession().get("openBacklogs") == null) {
            open = "";
            ActionContext.getContext().getSession().put("openBacklogs", open);
        }
        else {
            open = ActionContext.getContext().getSession().get("openBacklogs").toString();   
        }
         
        List<Integer> backlogsToGet = new LinkedList<Integer>();
        if (!open.equalsIgnoreCase("")) {
            for (String id : open.split(",")) {
                backlogsToGet.add(Integer.valueOf(id));
            }
        }
        for (Backlog backlog : backlogBusiness.getMultiple(backlogsToGet)) {
          openBacklogs.add(backlog);
        }

        // Get the pageitem
        if (backlogId == 0) {
            pageitem = null;
        }
        else {
            pageitem = backlogBusiness.getBacklog(backlogId);
        }

        // Get the data
        menuData = menuBusiness.getSubMenuData(pageitem);
        
        // Get the submenu data
        for (Backlog blog : openBacklogs) {
            openDatas.put(blog, menuBusiness.getSubMenuData(blog));
        }
        
        // Calculate the number
        numberOfItems = menuData.getMenuItems().size();
        
        return super.execute();
    }
    
    
    /**
     * Method for ajax update of the menus. 
     * @return
     */
    @SuppressWarnings("unchecked")
    public String ajaxUpdateOpenMenus() {
        String[] openBacklogs = openString.split(",");
        String savedString = "";

        
        int i = 0;
        for (String backlog : openBacklogs) {
            String append = "";
            
            // Split it
            try {
                append = backlog.split("_")[1];
            } catch(ArrayIndexOutOfBoundsException e) {
            }
            
            // Append it to the list
            savedString += append;
            
            // Add to the counter
            i++;
            if (i == openBacklogs.length) {
                break;
            }
            
            // Add the separator
            savedString += ",";
        }
        
        // Update the session variable
        ActionContext.getContext().getSession().put("openBacklogs", savedString);

        return CRUDAction.AJAX_SUCCESS;
    }
    
    /**
     * Parse the requested id to correct form.
     * @return
     */
    public int parseRequestId(String request) {
        int returnId = 0;
        
        if (!request.equalsIgnoreCase("")) {
            try {
                returnId = Integer.valueOf(request.split("_")[1]);
            } catch (ArrayIndexOutOfBoundsException e) {
                returnId = 0;
            }
        }
        else {
            returnId = 0;
        }
        
        return returnId;
    }
    

    /*
     * Autogenerated list of setters and getters.
     */

    public void setBacklogBusiness(BacklogBusiness backlogBusiness) {
        this.backlogBusiness = backlogBusiness;
    }

    public MenuBusiness getMenuBusiness() {
        return menuBusiness;
    }

    public void setMenuBusiness(MenuBusiness menuBusiness) {
        this.menuBusiness = menuBusiness;
    }

    public int getBacklogId() {
        return backlogId;
    }

    public void setBacklogId(int backlogId) {
        this.backlogId = backlogId;
    }

    public PageItem getPageitem() {
        return pageitem;
    }

    public void setPageitem(PageItem pageitem) {
        this.pageitem = pageitem;
    }

    public MenuData getMenuData() {
        return menuData;
    }

    public void setMenuData(MenuData menuData) {
        this.menuData = menuData;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    public String getRequestId() {
        return requestId;
    }


    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }


    public String getOpenString() {
        return openString;
    }


    public void setOpenString(String openString) {
        this.openString = openString;
    }


    public Set<Backlog> getOpenBacklogs() {
        return openBacklogs;
    }


    public void setOpenBacklogs(Set<Backlog> openBacklogs) {
        this.openBacklogs = openBacklogs;
    }

    public Map<PageItem, MenuData> getOpenDatas() {
        return openDatas;
    }

    public void setOpenDatas(Map<PageItem, MenuData> openDatas) {
        this.openDatas = openDatas;
    }
    
    public String getNavi() {
        return navi;
    }

    public void setNavi(String navi) {
        this.navi = navi;
    }


    public String getSubnavi() {
        return subnavi;
    }


    public void setSubnavi(String subnavi) {
        this.subnavi = subnavi;
    }
}
