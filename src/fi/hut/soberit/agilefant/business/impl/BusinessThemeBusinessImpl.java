package fi.hut.soberit.agilefant.business.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;

import fi.hut.soberit.agilefant.business.BusinessThemeBusiness;
import fi.hut.soberit.agilefant.db.BacklogDAO;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.BusinessThemeDAO;
import fi.hut.soberit.agilefant.db.ProductDAO;
import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.BacklogThemeBinding;
import fi.hut.soberit.agilefant.model.BusinessTheme;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.Product;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.State;
import fi.hut.soberit.agilefant.util.BusinessThemeMetrics;
import flexjson.JSONSerializer;

public class BusinessThemeBusinessImpl implements BusinessThemeBusiness {

    private BusinessThemeDAO businessThemeDAO;
    private ProductDAO productDAO;
    private BacklogDAO backlogDAO;
    private BacklogItemDAO backlogItemDAO;

    public BusinessTheme getBusinessTheme(int businessThemeId) {
        return businessThemeDAO.get(businessThemeId);
    }

    public Collection<BusinessTheme> getAll() {
        return businessThemeDAO.getAll();
    }
    
    public Collection<BusinessTheme> getSortedGlobalThemes(Boolean active) {
        return businessThemeDAO.getSortedGlobalThemes(active);
    }
       
    public Collection<BusinessTheme> getActiveBusinessThemes(int backlogId) {
        return this.getBusinessThemesByBacklogAndActivity(backlogId, true);
    }
    
    public Collection<BusinessTheme> getNonActiveBusinessThemes(int backlogId) {
        return this.getBusinessThemesByBacklogAndActivity(backlogId, false);
    }
    
    private Collection<BusinessTheme> getBusinessThemesByBacklogAndActivity(int backlogId, boolean activity) {
        Backlog bl = backlogDAO.get(backlogId);
        Product prod = null;
        if(bl instanceof Product) {
            prod = (Product)bl;
        } else if(bl instanceof Project) {
            prod = ((Project)bl).getProduct();
        } else if(bl instanceof Iteration) {
            prod = ((Iteration)bl).getProject().getProduct();
        }
        if(prod == null) {
            return new HashSet<BusinessTheme>();
        }
        return businessThemeDAO.getSortedBusinessThemesByProductAndActivity(prod, activity);
    }
    
    public List<BusinessTheme> getBacklogItemActiveOrSelectedThemes(int backlogItemId) {
        BacklogItem bli = backlogItemDAO.get(backlogItemId);
        if (bli == null) {
            return new ArrayList<BusinessTheme>();
        }
        List<BusinessTheme> activeThemes = new ArrayList<BusinessTheme>();
        Collection<BusinessTheme> bliThemes = bli.getBusinessThemes();
        Product prod = bli.getProduct();
        List<BusinessTheme> possibleThemes = businessThemeDAO.getSortedBusinessThemesByProductAndActivity(prod, null, true);
        for (BusinessTheme t: possibleThemes) {
            if (t.isActive() || bliThemes.contains(t)) {
                activeThemes.add(t);
            }
        }                        
        return activeThemes;
    }
    
    public Map<BusinessTheme, BusinessThemeMetrics> getThemeMetrics(int productId) {
        Product product = productDAO.get(productId);
        
        if (product == null) {
            return new HashMap<BusinessTheme, BusinessThemeMetrics>();
        }
        Map<Integer, Integer> allItems = businessThemeDAO.numberOfBacklogItemsByProduct(product, null);
        Map<Integer, Integer> doneItems = businessThemeDAO.numberOfBacklogItemsByProduct(product, State.DONE);
        
        Map<BusinessTheme, BusinessThemeMetrics> metricsMap = new HashMap<BusinessTheme, BusinessThemeMetrics>();

        for (BusinessTheme theme: product.getBusinessThemes()) {
            
            BusinessThemeMetrics metrics = new BusinessThemeMetrics();
            int donePercentage = 0;
            
            metrics.setNumberOfBlis(((allItems.get(theme.getId()) != null ) ? allItems.get(theme.getId()) : 0));
            metrics.setNumberOfDoneBlis(((doneItems.get(theme.getId()) != null ) ? doneItems.get(theme.getId()) : 0));

            if (metrics.getNumberOfBlis() > 0) {
                donePercentage = (int) ((float) metrics.getNumberOfDoneBlis() / (float) metrics.getNumberOfBlis() * 100f);
            }
            metrics.setDonePercentage(donePercentage);
            metricsMap.put(theme, metrics);
        }
        return metricsMap;
    }

    public boolean delete(int themeId) throws ObjectNotFoundException {
        boolean global = false;
        BusinessTheme businessTheme = businessThemeDAO.get(themeId);

        if (businessTheme == null) {
            throw new ObjectNotFoundException();
        }
        try {
            global = businessTheme.isGlobal();
            
            Collection<BacklogItem> associations = businessTheme.getBacklogItems();
            for(BacklogItem bli : associations) {
                bli.getBusinessThemes().remove(businessTheme);
            }
            Collection<BacklogThemeBinding> bindings = businessTheme.getBacklogBindings();
            if(bindings != null) {
                for(BacklogThemeBinding bind : bindings) {
                    businessThemeDAO.removeBacklogThemeBinding(bind);
                }
            }
            if (!businessTheme.isGlobal()) {
                businessTheme.getProduct().getBusinessThemes().remove(businessTheme);
            }
            businessThemeDAO.remove(themeId);
        } catch (Exception e) { }
        
        return global;
    }

    public void setBusinessThemeDAO(BusinessThemeDAO businessThemeDAO) {
        this.businessThemeDAO = businessThemeDAO;
    }

    /**
     * {@inheritDoc}
     */
    public BusinessTheme store(int businessThemeId, int productId, BusinessTheme theme)
            throws ObjectNotFoundException, DataIntegrityViolationException, Exception {
        BusinessTheme persistable = null;
        Product product = null;
        //hack
        if(productId > 0) {
            Backlog bl = backlogDAO.get(productId);
            if(bl instanceof Product) {
                product = (Product)bl;
            } else if(bl instanceof Project) {
                product = ((Project)bl).getProduct();
            } else if(bl instanceof Iteration) {
                product = ((Iteration)bl).getProject().getProduct();
            }
        }
        if (businessThemeId > 0 && (productId > 0 || productId == -1)) {
            persistable = businessThemeDAO.get(businessThemeId);
       
            if (persistable == null) {
                throw new ObjectNotFoundException(
                        "Selected theme was not found.");
            }
         
            persistable.setDescription(theme.getDescription());
            persistable.setName(theme.getName());
            persistable.setProduct(product);
            persistable.setActive(theme.isActive());
        } else {
            theme.setProduct(product);
            persistable = theme;
        }
        
        if (product == null) {
            persistable.setGlobal(true);
        }
        else {
            persistable.setGlobal(false);
        }
        
        try {
            if (persistable.getId() > 0) {
                businessThemeDAO.store(persistable);
            } else {
                int newId = (Integer) businessThemeDAO.create(persistable);
                persistable = businessThemeDAO.get(newId);
            }
        } catch (DataIntegrityViolationException dve) {
            throw new DataIntegrityViolationException("businessTheme.duplicateName");            
        } catch (Exception e) {
            throw new Exception();
        }
        
        return persistable;
    }
    
    public void activateBusinessTheme(int businessThemeId)
            throws ObjectNotFoundException {
        BusinessTheme theme = getBusinessTheme(businessThemeId);
        if (theme == null) {
            throw new ObjectNotFoundException();
        }
        activateBusinessTheme(theme);
    }

    public void activateBusinessTheme(BusinessTheme businessTheme) {
        if (businessTheme == null) {
            return;
        }
        businessTheme.setActive(true);
        businessThemeDAO.store(businessTheme);
    }

    public void deactivateBusinessTheme(int businessThemeId)
            throws ObjectNotFoundException {
        BusinessTheme theme = getBusinessTheme(businessThemeId);
        if (theme == null) {
            throw new ObjectNotFoundException();
        }
        deactivateBusinessTheme(theme);
    }

    public void deactivateBusinessTheme(BusinessTheme businessTheme) {
        if (businessTheme == null) {
            return;
        }
        businessTheme.setActive(false);
        businessThemeDAO.store(businessTheme);
    }
    
    public void addOrUpdateThemeToBacklog(BacklogThemeBinding binding) {
        if (binding == null) {
            return;
        }

        /*
         * check if the binding is moved from product to iteration and remove
         * product binding if one is found
         */
        if (binding.getBacklog() instanceof Iteration) {
            Project proj = ((Iteration) binding.getBacklog()).getProject();
            BacklogThemeBinding projectBinding = lookupBacklogThemeBinding(
                    proj, binding.getBusinessTheme());
            if (projectBinding != null) {
                businessThemeDAO.removeBacklogThemeBinding(projectBinding);
            }
        }
        /*
         * return if target backlog is a product and one of product's child
         * iterations already has the theme
         */
        if (binding.getBacklog() instanceof Project) {
            Project proj = (Project) binding.getBacklog();
            if (proj.getIterations() != null) {
                for (Iteration iter : proj.getIterations()) {
                    if (iter.getBusinessThemeBindings() != null) {
                        for (BacklogThemeBinding bind : iter
                                .getBusinessThemeBindings()) {
                            if (bind.getBusinessTheme() == binding
                                    .getBusinessTheme()) {
                                return;
                            }
                        }
                    }
                }
            }
        }
        businessThemeDAO.saveOrUpdateBacklogThemeBinding(binding);
    }
    
    public void multipleAddOrUpdateThemeToBacklog(int[] themeIds, int backlogId, String[] allocations) {
        Set<Integer> added = new HashSet<Integer>();
        if(themeIds.length != allocations.length) {
            return;
        }
        for(int i = 0 ; i < themeIds.length; i++) {
            if (!added.contains(themeIds[i])) {
                added.add(themeIds[i]);
                addOrUpdateThemeToBacklog(themeIds[i], backlogId, allocations[i]);
            }
        }
    }
    
    private BacklogThemeBinding lookupBacklogThemeBinding(Backlog bl, BusinessTheme theme) {
        if(theme.getBacklogBindings() == null) {
            return null;
        }
        for(BacklogThemeBinding bind : theme.getBacklogBindings()) {
            if(bind.getBacklog() == bl) {
                return bind;
            }
        }
        return null;
    }
    public void addOrUpdateThemeToBacklog(int themeId, int backlogId, String allocation) { 
        String trimmed = allocation.trim();
        Backlog bl = backlogDAO.get(backlogId);
        BusinessTheme theme = businessThemeDAO.get(themeId);
        //check for existing binding
        BacklogThemeBinding binding = lookupBacklogThemeBinding(bl, theme);
        
        if(binding == null) {
            binding = new BacklogThemeBinding();
            binding.setBusinessTheme(theme);
            binding.setBacklog(bl);
        }
        
        /* parse allocation format and find out whether it is a relative or
         * fixed binding. Relative binding should have format "12%", "12.3%" or "12,3%".
         * As fixed format should be standard AFTime format.
         */
        if(trimmed.length() > 0 && trimmed.charAt(trimmed.length() - 1) == '%') { //relative
            try {
                trimmed  = trimmed.substring(0,trimmed.length()-1);
                binding.setPercentage(new Float(Float.parseFloat(trimmed)));
            } catch(Exception e) {
                binding.setPercentage(0f);
            }
            binding.setRelativeBinding(true);
        } else { //fixed
            binding.setRelativeBinding(false);
            try {
                binding.setFixedSize(new AFTime(trimmed, false));
            } catch(Exception e) {
                binding.setFixedSize(new AFTime(0));
            }
        }
        addOrUpdateThemeToBacklog(binding);
        
    }

    public void setBacklogItemThemes(Set<Integer> themeIds, int backlogItemId) {
        BacklogItem bli = backlogItemDAO.get(backlogItemId);
        setBacklogItemThemes(themeIds, bli);
    }

    
    public void setBacklogItemThemes(Set<Integer> themeIds, BacklogItem bli) {
        if(bli == null) {
            return;
        }
        
        Collection<BusinessTheme> set = new HashSet<BusinessTheme>();
        for(Integer themeId : themeIds) {
            BusinessTheme theme = getBusinessTheme(themeId);
            if (theme != null) {
                set.add(theme);
            }
        }
        bli.setBusinessThemes(set);
        backlogItemDAO.store(bli);
        
    }
    
    public void removeThemeFromBacklog(Backlog backlog,
            BusinessTheme businessTheme) {
        if(backlog == null || businessTheme == null) {
            return;
        }
        BacklogThemeBinding binding = lookupBacklogThemeBinding(backlog, businessTheme);
        if(binding != null) {
            businessThemeDAO.removeBacklogThemeBinding(binding);
        }
        
    }
    
    public void removeThemeBinding(BacklogThemeBinding binding) {
        if(binding != null) {
            businessThemeDAO.removeBacklogThemeBinding(binding);
        }
    }
    
    public String getThemesForProductAsJSON(int productId, boolean includeGlobal) {
        return getThemesForProductAsJSON(productDAO.get(productId), includeGlobal);
    }
    
    public String getThemesForProductAsJSON(Product product, boolean includeGlobal) {
        if (product == null) {
            return "[]";
        }
        Collection<BusinessTheme> themes;
        if(includeGlobal) {
            themes = businessThemeDAO.getSortedBusinessThemesByProductAndActivity(product, true, true);
        } else {
            themes = product.getBusinessThemes();
        }
        return new JSONSerializer().serialize(themes);
    }
    
    public String getActiveThemesForBacklogAsJSON(Backlog backlog) {
        if (backlog == null) {
            return "[]";
        }
        Collection<BusinessTheme> themes;
        Product prod;
        
        if (backlog instanceof Product) {
            prod = (Product)backlog;
        }
        else if (backlog instanceof Project) {
            prod = ((Project)backlog).getProduct(); 
        }
        else {
            prod = ((Iteration)backlog).getProject().getProduct();
        }
        themes = businessThemeDAO.getSortedBusinessThemesByProductAndActivity(prod, true, true);
        return new JSONSerializer().serialize(themes);
    }

    public String getActiveThemesForBacklogAsJSON(int backlogId) {
        return getActiveThemesForBacklogAsJSON(backlogDAO.get(backlogId));
    }
    
    public void loadBacklogThemeMetrics(Backlog backlog) {
        Collection<BacklogItem> blis = backlog.getBacklogItems();
        Collection<BacklogThemeBinding> bindings = backlog
                .getBusinessThemeBindings();
        Map<Integer, BusinessTheme> themes = new HashMap<Integer, BusinessTheme>();
        for (BacklogThemeBinding bind : bindings) {
            themes
                    .put(bind.getBusinessTheme().getId(), bind
                            .getBusinessTheme());
            bind.getBusinessTheme().setMetrics(new BusinessThemeMetrics());
        }
        for (BacklogItem bli : blis) {
            for (BusinessTheme bt : bli.getBusinessThemes()) {
                if (themes.get(bt.getId()) != null) {
                    if (bli.getState() == State.DONE) {
                        themes.get(bt.getId()).getMetrics()
                                .setNumberOfDoneBlis(
                                        themes.get(bt.getId()).getMetrics()
                                                .getNumberOfDoneBlis() + 1);
                    }
                    themes.get(bt.getId()).getMetrics().setNumberOfBlis(
                            themes.get(bt.getId()).getMetrics()
                                    .getNumberOfBlis() + 1);
                    if (themes.get(bt.getId()).getMetrics().getNumberOfBlis() > 0) {
                        themes.get(bt.getId()).getMetrics().setDonePercentage((int)(100f*
                                (float)themes.get(bt.getId()).getMetrics()
                                        .getNumberOfDoneBlis()
                                        / (float)themes.get(bt.getId()).getMetrics()
                                                .getNumberOfBlis()));
                    }
                }
            }
        }
    }

    
    public void removeThemeBinding(int bindingId) {
        removeThemeBinding(businessThemeDAO.getBindingById(bindingId));
    }
    public void removeThemeFromBacklog(int backlogId, int businessThemeId) {
        removeThemeFromBacklog(backlogDAO.get(backlogId), businessThemeDAO.get(businessThemeId));
    }
    
    public List<BacklogThemeBinding> getIterationThemesByProject(Project project) {
        return businessThemeDAO.getIterationThemesByProject(project);
    }

    public List<BacklogThemeBinding> getIterationThemesByProject(int projectId) {
        return getIterationThemesByProject((Project)backlogDAO.get(projectId));
    }
    
    public Map<BacklogItem, List<BusinessTheme>> getBacklogItemBusinessThemesByBacklog(
            Backlog backlog) {
        return businessThemeDAO.getBacklogItemBusinessThemesByBacklog(backlog);
    }
    
    /* getters and setters */
    
    public ProductDAO getProductDAO() {
        return productDAO;
    }

    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
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

}
