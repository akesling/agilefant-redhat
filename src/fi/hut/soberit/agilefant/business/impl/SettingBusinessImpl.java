package fi.hut.soberit.agilefant.business.impl;

import java.util.Collection;
import java.util.List;

import fi.hut.soberit.agilefant.business.SettingBusiness;
import fi.hut.soberit.agilefant.db.SettingDAO;
import fi.hut.soberit.agilefant.model.Setting;

/**
 * Business implementation for handling of settings
 * 
 * @author kjniiran
 *
 */

public class SettingBusinessImpl implements SettingBusiness {
    private SettingDAO settingDAO;
    
    private static final String SETTING_NAME_HOUR_REPORTING = "HourReporting";
    public static final String SETTING_VALUE_TRUE = "true";
    
    /**
     * {@inheritDoc}
     */
    public Collection<Setting> getAll() {
        return settingDAO.getAll();
    }
    
    /**
     * {@inheritDoc}
     */
    public List<Setting> getAllOrderByName() {
        return settingDAO.getAllOrderByName();
    }
    
    /**
     * {@inheritDoc}
     */
    public Setting getSetting(int settingID) {
        return settingDAO.get(settingID);
    }
    
    public SettingDAO getSettingDAO() {
        return settingDAO;
    }
    
    public void setSettingDAO(SettingDAO settingDAO) {
        this.settingDAO = settingDAO;
    }

    /**
     * {@inheritDoc}
     */
    public void delete(int settingID) {
        settingDAO.remove(settingID);
    }
    
    /**
     * {@inheritDoc}
     */
    public void store(Setting setting) {
        settingDAO.store(setting);
    }
    
    public void setHourReporting(String mode) {
        Setting setting = settingDAO.getSetting(SETTING_NAME_HOUR_REPORTING);
        boolean selection = (mode != null && mode.equals(SETTING_VALUE_TRUE));
        if(setting == null) {
            setting = new Setting();
            setting.setName(SETTING_NAME_HOUR_REPORTING);
            setting.setValue(new Boolean(selection).toString());
            settingDAO.create(setting);
        } else {
            setting.setValue(new Boolean(selection).toString());
            settingDAO.store(setting);
        }
    }
    /**
     * {@inheritDoc}
     */
    public boolean isHourReportingEnabled() {
        Setting setting = settingDAO.getSetting(SETTING_NAME_HOUR_REPORTING);
        
        if (setting == null) {
            return false;
        }
        
        return setting.getValue().equals(SETTING_VALUE_TRUE);        
    }

    public Setting getSetting(String name) {
        return settingDAO.getSetting(name);
    }
}