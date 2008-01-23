package fi.hut.soberit.agilefant.web;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.opensymphony.xwork.Action;
import com.opensymphony.xwork.ActionSupport;

import fi.hut.soberit.agilefant.business.BacklogBusiness;
import fi.hut.soberit.agilefant.business.UserBusiness;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.ProductDAO;
import fi.hut.soberit.agilefant.db.ProjectDAO;
import fi.hut.soberit.agilefant.db.ProjectTypeDAO;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.Product;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.ProjectType;
import fi.hut.soberit.agilefant.model.User;

public class ProjectAction extends ActionSupport implements CRUDAction {

    private static final long serialVersionUID = -4636900464606739866L;

    private int projectId;

    private int productId;

    private int projectTypeId;

    private Project project;

    private ProjectDAO projectDAO;

    private ProjectTypeDAO projectTypeDAO;

    private ProductDAO productDAO;

    private Collection<ProjectType> projectTypes;

    private Backlog backlog;

    private BacklogItemDAO backlogItemDAO;

    private String startDate;

    private String endDate;

    private String dateFormat;

    private int[] selectedUserIds;

    private BacklogBusiness backlogBusiness;

    private List<User> users = new ArrayList<User>();

    private UserBusiness userBusiness;

    /**
     * @return the dateFormat
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * @param dateFormat
     *                the dateFormat to set
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String create() {
        this.prepareProjectTypes();
        // TODO: fiksumpi virheenkäsittely
        // if (this.projectTypes.isEmpty()){
        // super.addActionError("project.projectTypesNotFound");
        // return Action.ERROR;
        // }
        projectId = 0;
        project = new Project();
        backlog = project;
        
        // populate all users to drop-down list
        users = userBusiness.getAllUsers();
        
        return Action.SUCCESS;
    }

    public String edit() {
        Date startDate;
        this.prepareProjectTypes();
        if (this.projectTypes.isEmpty()) {
            super.addActionError("project.projectTypesNotFound");
            return Action.ERROR;
        }
        project = projectDAO.get(projectId);

        if (project == null) {
            super.addActionError("Invalid project id!");
            return Action.ERROR;
        }
        startDate = project.getStartDate();

        if (startDate == null) {
            startDate = new Date(0);
        }

        productId = project.getProduct().getId();
        backlog = project;
        /*
         * BacklogValueInjector.injectMetrics(backlog, startDate, taskEventDAO,
         * backlogItemDAO);
         */

        return Action.SUCCESS;
    }

    public String store() {
        Project storable = new Project();
        if (projectId > 0) {
            storable = projectDAO.get(projectId);
            if (storable == null) {
                super.addActionError(super.getText("project.notFound"));
                return Action.ERROR;
            }
        }

        try {
            this.fillStorable(storable);
        } catch (ParseException e) {
            super.addActionError(e.toString());
            return Action.ERROR;
        }

        if (super.hasActionErrors()) {
            return Action.ERROR;
        }

        if (projectId == 0)
            projectId = (Integer) projectDAO.create(storable);
        else
            projectDAO.store(storable);

        backlogBusiness.setAssignments(selectedUserIds, project);
        return Action.SUCCESS;
    }

    public String delete() {
        project = projectDAO.get(projectId);
        if (project == null) {
            super.addActionError(super.getText("project.notFound"));
            return Action.ERROR;
        }
        if (project.getBacklogItems().size() > 0
                || project.getIterations().size() > 0) {
            super.addActionError(super.getText("project.notEmptyWhenDeleting"));
            return Action.ERROR;
        }
        Product product = project.getProduct();
        productId = product.getId();
        product.getProjects().remove(project);
        project.setProduct(null);
        projectDAO.remove(project);
        return Action.SUCCESS;
    }

    protected void fillStorable(Project storable) throws ParseException {
        if (startDate == null) {
            super.addActionError(super.getText("Invalid startdate!"));
            return;
        } else if (endDate == null) {
            super.addActionError(super.getText("Invalid enddate!"));
            return;
        }

        if (this.project.getName() == null || this.project.getName().equals("")) {
            super.addActionError(super.getText("project.missingName"));
            return;
        }

        project.setStartDate(startDate, dateFormat);
        if (project.getStartDate() == null) {
            super.addActionError(super.getText("project.missingStartDate"));
            return;
        }

        project.setEndDate(endDate, dateFormat);
        if (project.getEndDate() == null) {
            super.addActionError(super.getText("project.missingEndDate"));
            return;
        }
        if (project.getStartDate().after(project.getEndDate())) {
            super
                    .addActionError(super
                            .getText("backlog.startDateAfterEndDate"));
            return;
        }

        Product product = productDAO.get(productId);
        if (product == null) {
            super.addActionError(super.getText("product.notFound"));
            return;
        } else if (storable.getProduct() != product) {
            /*
             * Setting the relation in one end of the relation is enought to
             * change the relation in both ends! Hibernate takes care of both
             * ends.
             */
            storable.setProduct(product);
            // product.getProjects().add(storable);
        }

        if (storable.getProjectType() == null
                || storable.getProjectType().getId() != projectTypeId) {
            ProjectType projectType = null;
            if (projectTypeId > 0) {
                projectType = projectTypeDAO.get(projectTypeId);
            }
            if (projectType != null) {
                storable.setProjectType(projectType);
            } else {
                super.addActionError(super
                        .getText("project.missingProjectType"));
                return;
            }
        }
        storable.setEndDate(endDate, dateFormat);
        storable.setStartDate(startDate, dateFormat);
        storable.setName(project.getName());
        storable.setDescription(project.getDescription());
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public Collection<Project> getAllProjects() {
        return this.projectDAO.getAll();
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
        this.backlog = project;
    }

    public void setProjectDAO(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public void setProductDAO(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public int getProjectTypeId() {
        return projectTypeId;
    }

    public void setProjectTypeId(int projectTypeId) {
        this.projectTypeId = projectTypeId;
    }

    public void setProjectTypeDAO(ProjectTypeDAO projectTypeDAO) {
        this.projectTypeDAO = projectTypeDAO;
    }

    private void prepareProjectTypes() {
        this.projectTypes = projectTypeDAO.getAll();
    }

    public Collection<ProjectType> getProjectTypes() {
        return this.projectTypes;
    }

    public void setProjectTypes(Collection<ProjectType> projectTypes) {
        this.projectTypes = projectTypes;
    }

    public Backlog getBacklog() {
        return this.backlog;
    }

    /**
     * @return the backlogItemDAO
     */
    public BacklogItemDAO getBacklogItemDAO() {
        return backlogItemDAO;
    }

    /**
     * @param backlogItemDAO
     *                the backlogItemDAO to set
     */
    public void setBacklogItemDAO(BacklogItemDAO backlogItemDAO) {
        this.backlogItemDAO = backlogItemDAO;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int[] getSelectedUserIds() {
        return selectedUserIds;
    }

    public void setSelectedUserIds(int[] selectedUserIds) {
        this.selectedUserIds = selectedUserIds;
    }

    public void setBacklogBusiness(BacklogBusiness backlogBusiness) {
        this.backlogBusiness = backlogBusiness;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUserBusiness(UserBusiness userBusiness) {
        this.userBusiness = userBusiness;
    }
}