package fi.hut.soberit.agilefant.web;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import com.opensymphony.xwork.Action;

import fi.hut.soberit.agilefant.business.BacklogBusiness;
import fi.hut.soberit.agilefant.business.BacklogItemBusiness;
import fi.hut.soberit.agilefant.db.IterationGoalDAO;
import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.BusinessTheme;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.IterationGoal;
import fi.hut.soberit.agilefant.model.Priority;
import fi.hut.soberit.agilefant.model.Product;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.State;

/**
 * JUnit integration test for BacklogAction.
 * 
 * @author
 */
public class BacklogActionTest extends TestCase {
    // class under test
    private BacklogAction backlogAction = new BacklogAction();

    private IterationGoalDAO iterationGoalDAO;

    private BacklogItemBusiness backlogItemBusiness;
    private BacklogBusiness backlogBusiness;

    /**
     * Test edit operation.
     */
    
    public void testEdit() {
        backlogBusiness = createMock(BacklogBusiness.class);
        backlogAction.setBacklogBusiness(backlogBusiness);
        
        Product product = new Product();
        product.setId(123);
        Project project = new Project();
        project.setId(125);
        Iteration iteration = new Iteration();
        iteration.setId(666);
        
        expect(backlogBusiness.get(123)).andReturn(product);
        expect(backlogBusiness.get(125)).andReturn(project);
        expect(backlogBusiness.get(666)).andReturn(iteration);
        expect(backlogBusiness.get(-100)).andReturn(null);
        replay(backlogBusiness);
        
        // test product
        backlogAction.setBacklogId(product.getId());
        assertEquals("editProduct", backlogAction.edit());
        // test project
        backlogAction.setBacklogId(project.getId());
        assertEquals("editProject", backlogAction.edit());
        // test iteration
        backlogAction.setBacklogId(iteration.getId());
        assertEquals("editIteration", backlogAction.edit());
        // test invalid
        backlogAction.setBacklogId(-100);
        assertEquals("error", backlogAction.edit());
        
        verify(backlogBusiness);
    }

    /**
     * Test MoveBacklogItem method.
     */
    public void testMoveBacklogItem() {
        backlogBusiness = createMock(BacklogBusiness.class);
        iterationGoalDAO = createMock(IterationGoalDAO.class);
        backlogItemBusiness = createMock(BacklogItemBusiness.class);
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemBusiness(backlogItemBusiness);
        
        // Generate test data
        Iteration origBacklog = new Iteration();                
        origBacklog.setId(135);                               
        origBacklog.setBacklogItems(new HashSet<BacklogItem>());
        
        Project targetBacklog = new Project();        
        targetBacklog.setId(128);        
        targetBacklog.setBacklogItems(new HashSet<BacklogItem>());
                
        BacklogItem bli = new BacklogItem();
        bli.setId(66);
        bli.setBacklog(origBacklog);
        origBacklog.getBacklogItems().add(bli);
        
        IterationGoal ig = new IterationGoal();
        ig.setId(987);
        ig.setIteration(origBacklog);
        ig.setBacklogItems(new HashSet<BacklogItem>());
        bli.setIterationGoal(ig);
        
        backlogAction.setBacklogId(128);
        backlogAction.setBacklogItemId(66);
        
        // Record expected behavior
        expect(iterationGoalDAO.get(987)).andReturn(ig);
        expect(backlogBusiness.get(128)).andReturn(targetBacklog);
        expect(backlogItemBusiness.getBacklogItem(66)).andReturn(bli);
        bli.getBacklog().getBacklogItems().remove(bli);
        targetBacklog.getBacklogItems().add(bli);
        bli.setBacklog(targetBacklog);
        bli.getIterationGoal().getBacklogItems().remove(bli);
        bli.setIterationGoal(null);
        backlogItemBusiness.store(bli);
        
        replay(backlogBusiness);
        replay(backlogItemBusiness);
        
        assertSame(iterationGoalDAO.get(987), bli.getIterationGoal());        
        assertEquals("editProject", backlogAction.moveBacklogItem());
        assertNull(bli.getIterationGoal());
        
        verify(backlogBusiness);
        verify(backlogItemBusiness);
    }
    
    /**
     * Test with invalid backlog id.
     */
    public void testMoveBacklogItem_invalidBacklogId() {
        backlogBusiness = createMock(BacklogBusiness.class);
        backlogItemBusiness = createMock(BacklogItemBusiness.class);
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemBusiness(backlogItemBusiness);
        
        BacklogItem bli = new BacklogItem();
        bli.setId(66);
        
        backlogAction.setBacklogId(-100);
        backlogAction.setBacklogItemId(66);
        
        expect(backlogBusiness.get(-100)).andReturn(null);
        expect(backlogItemBusiness.getBacklogItem(66)).andReturn(bli);
        
        replay(backlogBusiness);
        replay(backlogItemBusiness);
        
        assertEquals(Action.ERROR, backlogAction.moveBacklogItem());
        
        verify(backlogBusiness);
        verify(backlogItemBusiness);
    }
    
    /**
     * Test with invalid bli id.backlogItemIds
     */
    public void testMoveBacklogItem_invalidBliId() {
        backlogBusiness = createMock(BacklogBusiness.class);
        backlogItemBusiness = createMock(BacklogItemBusiness.class);
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemBusiness(backlogItemBusiness);
        
        Backlog backlog = new Project();
        backlog.setId(66);
        
        backlogAction.setBacklogId(66);
        backlogAction.setBacklogItemId(-100);
        
        expect(backlogBusiness.get(66)).andReturn(backlog);
        expect(backlogItemBusiness.getBacklogItem(-100)).andReturn(null);
        
        replay(backlogBusiness);
        replay(backlogItemBusiness);
        
        assertEquals(Action.ERROR, backlogAction.moveBacklogItem());
        
        verify(backlogBusiness);
        verify(backlogItemBusiness);
    }
    
    /**
     * Test moving selected items.
     */
    public void testDoActionOnMultipleBacklogItems() {
        backlogBusiness = createMock(BacklogBusiness.class);
        backlogItemBusiness = createMock(BacklogItemBusiness.class);
        
        // Generate test data
        Backlog origBacklog = new Iteration();
        origBacklog.setId(255);
        Backlog targetBacklog = new Project();
        targetBacklog.setId(100);
        
        BacklogItem bli1 = new BacklogItem();
        bli1.setId(15);
        bli1.setState(State.NOT_STARTED);
        bli1.setPriority(Priority.UNDEFINED);
        
        BacklogItem bli2 = new BacklogItem();
        bli2.setId(16);
        bli2.setState(State.STARTED);
        bli2.setPriority(Priority.BLOCKER);
        
        BacklogItem bli3 = new BacklogItem();
        bli3.setId(17);
        bli3.setState(State.PENDING);
        bli3.setPriority(Priority.TRIVIAL);
        
        BacklogItem bli4 = new BacklogItem();
        bli4.setId(18);
        bli4.setState(State.DONE);
        bli4.setPriority(Priority.MAJOR);
        
        int[] selected = {15, 17, 18};
        Set<Integer> responsibleIds = new HashSet<Integer>();
        responsibleIds.add(3);
        responsibleIds.add(5);
        responsibleIds.add(6);
        
        // Set the attributes for the backlogAction
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemBusiness(backlogItemBusiness);
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemIds(selected);
        
        backlogAction.setTargetPriority(3);
        backlogAction.setTargetBacklog(100);
        backlogAction.setTargetIterationGoalId(-1);
        backlogAction.setTargetState(2);
        backlogAction.setBacklogId(255);
        backlogAction.setItemAction("ChangeSelected");
        backlogAction.setUserIds(responsibleIds);
        backlogAction.setKeepResponsibles(0);
        backlogAction.setKeepThemes(1);
        
        // The test
        expect(backlogBusiness.get(255)).andReturn(origBacklog);
        try {
            backlogBusiness.changePriorityOfMultipleItems(selected,
                Priority.values()[3]);
            backlogBusiness.changeStateOfMultipleItems(selected,
                    State.values()[2]);
            backlogBusiness.setResponsiblesForMultipleBacklogItems(selected,
                    responsibleIds);
            backlogBusiness.moveMultipleBacklogItemsToBacklog(selected, 100);
        }
        catch (ObjectNotFoundException e) {
            fail(e.getMessage());
        }
        
        replay(backlogBusiness);
        replay(backlogItemBusiness);
        
        assertEquals("editIteration", backlogAction.doActionOnMultipleBacklogItems());
        
        verify(backlogBusiness);
        verify(backlogItemBusiness);
        
    }

    /**
     * Test moving selected items.
     */
    public void testDoActionOnMultipleBacklogItemsWithThemes() {
        backlogBusiness = createMock(BacklogBusiness.class);
        backlogItemBusiness = createMock(BacklogItemBusiness.class);
        
        // Generate test data
        Iteration origIteration = new Iteration();
        Project origProject = new Project();
        Product origProduct = new Product();
        origProject.setId(555);
        origProduct.setId(556);
        origIteration.setId(255);
        origProduct.setProjects(new ArrayList<Project>());
        origProject.setProduct(origProduct);
        origProject.getIterations().add(origIteration);
        origProject.setIterations(new ArrayList<Iteration>());
        origIteration.setProject(origProject);
        origProduct.getProjects().add(origProject);
               
        origIteration.setBacklogItems(new HashSet<BacklogItem>());
        
        Project targetProject = new Project();
        Product targetProduct = new Product();
        targetProject.setId(100);
        targetProject.setProduct(targetProduct);
        targetProduct.setId(129);
        targetProduct.setProjects(new ArrayList<Project>());
        targetProject.setBacklogItems(new HashSet<BacklogItem>());
        targetProduct.getProjects().add(targetProject);
        
        BusinessTheme theme = new BusinessTheme();
        theme.setId(1);
        theme.setProduct(origProduct);        
        origProduct.setBusinessThemes(new HashSet<BusinessTheme>());
        origProduct.getBusinessThemes().add(theme);
                                
        BacklogItem bli1 = new BacklogItem();
        bli1.setId(15);       
        origIteration.getBacklogItems().add(bli1);
        bli1.setBusinessThemes(new HashSet<BusinessTheme>());
        bli1.getBusinessThemes().add(theme);
        
        BacklogItem bli2 = new BacklogItem();
        bli2.setId(16);
        origIteration.getBacklogItems().add(bli2);
        bli2.setBusinessThemes(new HashSet<BusinessTheme>());
        bli2.getBusinessThemes().add(theme);        
        
        BacklogItem bli3 = new BacklogItem();
        bli3.setId(17);
        origIteration.getBacklogItems().add(bli3);
        bli3.setBusinessThemes(new HashSet<BusinessTheme>());
        bli3.getBusinessThemes().add(theme);
        
        BacklogItem bli4 = new BacklogItem();
        bli4.setId(18);
        origIteration.getBacklogItems().add(bli4);
        bli4.setBusinessThemes(new HashSet<BusinessTheme>());
        bli4.getBusinessThemes().add(theme);
        
        int[] selected = {15, 17, 18};                
        
        // Set the attributes for the backlogAction
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemBusiness(backlogItemBusiness);
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemIds(selected);
                
        backlogAction.setTargetBacklog(100);                
        backlogAction.setBacklogId(255);
        backlogAction.setItemAction("ChangeSelected");
        backlogAction.setTargetPriority(-1);
        backlogAction.setTargetIterationGoalId(-1);
        backlogAction.setTargetState(-1);
        backlogAction.setKeepResponsibles(1);
        backlogAction.setKeepThemes(1);
        
        // The test
        expect(backlogBusiness.get(255)).andReturn(origIteration);
        bli1.getBusinessThemes().clear();
        bli2.getBusinessThemes().clear();
        bli3.getBusinessThemes().clear();
        bli4.getBusinessThemes().clear();
        try {           
            backlogBusiness.moveMultipleBacklogItemsToBacklog(selected, 100);
        }
        catch (ObjectNotFoundException e) {
            fail(e.getMessage());
        }
        
        replay(backlogBusiness);
        replay(backlogItemBusiness);
        
        assertEquals("editIteration", backlogAction.doActionOnMultipleBacklogItems());
        assertEquals(0, bli1.getBusinessThemes().size());
        assertEquals(0, bli2.getBusinessThemes().size());
        assertEquals(0, bli3.getBusinessThemes().size());
        assertEquals(0, bli4.getBusinessThemes().size());
        
        verify(backlogBusiness);
        verify(backlogItemBusiness);
        
    }
    
    /**
     * Move an item with a theme under another product. 
     */
    public void testMoveBacklogItemWithBusinessThemeUnderDifferentProduct() {
        backlogBusiness = createMock(BacklogBusiness.class);
        backlogItemBusiness = createMock(BacklogItemBusiness.class);
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemBusiness(backlogItemBusiness);
        
        // Generate test data
        Iteration origIteration = new Iteration();
        Project origProject = new Project();
        Product origProduct = new Product();
        origProject.setId(555);
        origProduct.setId(556);
        origIteration.setId(135);
        origProduct.setProjects(new ArrayList<Project>());
        origProject.setProduct(origProduct);
        origProject.getIterations().add(origIteration);
        origProject.setIterations(new ArrayList<Iteration>());
        origIteration.setProject(origProject);
        origProduct.getProjects().add(origProject);
               
        origIteration.setBacklogItems(new HashSet<BacklogItem>());
        
        Project targetProject = new Project();
        Product targetProduct = new Product();
        targetProject.setId(128);
        targetProject.setProduct(targetProduct);
        targetProduct.setId(129);
        targetProduct.setProjects(new ArrayList<Project>());
        targetProject.setBacklogItems(new HashSet<BacklogItem>());
        targetProduct.getProjects().add(targetProject);
        
        BacklogItem bli = new BacklogItem();
        bli.setId(66);
        bli.setBacklog(origIteration);
        origIteration.getBacklogItems().add(bli);                
        
        BusinessTheme theme = new BusinessTheme();
        theme.setId(1);
        theme.setProduct(origProduct);        
        origProduct.setBusinessThemes(new HashSet<BusinessTheme>());
        origProduct.getBusinessThemes().add(theme);
        bli.setBusinessThemes(new HashSet<BusinessTheme>());
        bli.getBusinessThemes().add(theme);
        
        backlogAction.setBacklogId(128);
        backlogAction.setBacklogItemId(66);
        
        // Record expected behavior        
        expect(backlogBusiness.get(128)).andReturn(targetProject);
        expect(backlogItemBusiness.getBacklogItem(66)).andReturn(bli);
        bli.getBacklog().getBacklogItems().remove(bli);
        targetProject.getBacklogItems().add(bli);
        bli.getBusinessThemes().clear();        
        bli.setBacklog(targetProject);        
        backlogItemBusiness.store(bli);
        
        replay(backlogBusiness);
        replay(backlogItemBusiness);
                 
        assertEquals("editProject", backlogAction.moveBacklogItem());
        assertEquals(0, bli.getBusinessThemes().size());
        
        verify(backlogBusiness);
        verify(backlogItemBusiness);
    }
    
    /**
     * Move an item with a theme under another product. 
     */
    public void testMoveBacklogItemWithBusinessThemeUnderSameProduct() {
        backlogBusiness = createMock(BacklogBusiness.class);
        backlogItemBusiness = createMock(BacklogItemBusiness.class);
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setBacklogItemBusiness(backlogItemBusiness);
        
        // Generate test data
        Iteration origIteration = new Iteration();
        Project origProject = new Project();
        Product origProduct = new Product();
        origProject.setId(555);
        origProduct.setId(556);
        origIteration.setId(135);
        origProduct.setProjects(new ArrayList<Project>());
        origProject.setProduct(origProduct);
        origProject.getIterations().add(origIteration);
        origProject.setIterations(new ArrayList<Iteration>());
        origIteration.setProject(origProject);
        origProduct.getProjects().add(origProject);
               
        origIteration.setBacklogItems(new HashSet<BacklogItem>());
        
        Project targetProject = new Project();
        Product targetProduct = origProduct;
        targetProject.setId(128);
        targetProject.setProduct(targetProduct);                
        targetProject.setBacklogItems(new HashSet<BacklogItem>());
                
        BacklogItem bli = new BacklogItem();
        bli.setId(66);
        bli.setBacklog(origIteration);
        origIteration.getBacklogItems().add(bli);                
        
        BusinessTheme theme = new BusinessTheme();
        theme.setId(1);
        theme.setProduct(origProduct);        
        origProduct.setBusinessThemes(new HashSet<BusinessTheme>());
        origProduct.getBusinessThemes().add(theme);
        bli.setBusinessThemes(new HashSet<BusinessTheme>());
        bli.getBusinessThemes().add(theme);
        
        backlogAction.setBacklogId(128);
        backlogAction.setBacklogItemId(66);
        
        // Record expected behavior        
        expect(backlogBusiness.get(128)).andReturn(targetProject);
        expect(backlogItemBusiness.getBacklogItem(66)).andReturn(bli);
        bli.getBacklog().getBacklogItems().remove(bli);
        targetProject.getBacklogItems().add(bli);              
        bli.setBacklog(targetProject);        
        backlogItemBusiness.store(bli);
        
        replay(backlogBusiness);
        replay(backlogItemBusiness);
                 
        assertEquals("editProject", backlogAction.moveBacklogItem());
        assertEquals(1, bli.getBusinessThemes().size());
        
        verify(backlogBusiness);
        verify(backlogItemBusiness);
    }
    
    /**
     * Test delete multiple backlogItems.
     */
    public void testDeleteMultiple() {
        backlogBusiness = createMock(BacklogBusiness.class);
        backlogItemBusiness = createMock(BacklogItemBusiness.class);
        backlogAction.setBacklogItemBusiness(backlogItemBusiness);
        backlogAction.setBacklogBusiness(backlogBusiness);
        backlogAction.setItemAction("DeleteSelected");
        backlogAction.setBacklogId(10);
        
        // Create test data
        Backlog project = new Project();
        project.setId(10);
        BacklogItem bli1 = new BacklogItem();
        BacklogItem bli2 = new BacklogItem();
        BacklogItem bli3 = new BacklogItem();
        bli1.setId(15);
        bli2.setId(16);
        bli3.setId(17);
        int[] selected = {15, 17};
        
        backlogAction.setBacklogItemIds(selected);
        
        expect(backlogBusiness.get(10)).andReturn(project);
        try {
            backlogBusiness.deleteMultipleItems(10, selected);
        }
        catch (ObjectNotFoundException e) {
            fail(e.getMessage());
        }
        
        replay(backlogBusiness);
        replay(backlogItemBusiness);
        
        backlogAction.doActionOnMultipleBacklogItems();
        
        verify(backlogBusiness);
        verify(backlogItemBusiness);
    }
}
