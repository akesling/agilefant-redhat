package fi.hut.soberit.agilefant.business;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import junit.framework.TestCase;
import fi.hut.soberit.agilefant.business.impl.BacklogBusinessImpl;
import fi.hut.soberit.agilefant.db.AssignmentDAO;
import fi.hut.soberit.agilefant.db.BacklogDAO;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.UserDAO;
import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.Assignment;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.IterationGoal;
import fi.hut.soberit.agilefant.model.Priority;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.User;
import fi.hut.soberit.agilefant.util.BacklogLoadData;

/**
 * A spring test case for testing the Backlog business layer.
 * 
 * @author hhaataja, rstrom
 * 
 */

public class BacklogBusinessTest extends TestCase {

    private BacklogBusinessImpl backlogBusiness = new BacklogBusinessImpl();
    private HistoryBusiness historyBusiness;
    private BacklogItemDAO bliDAO;
    private BacklogDAO backlogDAO;
    private AssignmentDAO assignmentDAO;
    private UserDAO userDAO;

    public void testChangePriorityOfMultipleItems() throws Exception {
        bliDAO = createMock(BacklogItemDAO.class);
        backlogBusiness.setBacklogItemDAO(bliDAO);
        BacklogItem bli = new BacklogItem();

        // Record expected behavior
        expect(bliDAO.get(68)).andReturn(bli);
        replay(bliDAO);
        // run method under test
        int ids[] = { 68 };
        backlogBusiness.changePriorityOfMultipleItems(ids, Priority.BLOCKER);
        assertEquals(Priority.BLOCKER, bli.getPriority());

        // verify behavior
        verify(bliDAO);
    }

    public void testCreateBacklogItemToBacklog() {
        backlogDAO = createMock(BacklogDAO.class);
        backlogBusiness.setBacklogDAO(backlogDAO);
        Backlog backlog = new Iteration();

        // Record expected behavior
        expect(backlogDAO.get(68)).andReturn(backlog);
        replay(backlogDAO);
        // run method under test
        BacklogItem bli = backlogBusiness.createBacklogItemToBacklog(68);
        assertEquals(backlog, bli.getBacklog());
        assertTrue(backlog.getBacklogItems().contains(bli));

        // verify behavior
        verify(backlogDAO);
    }

    public void testCreateBakclogItemToBacklog_notFound() {
        backlogDAO = createMock(BacklogDAO.class);
        backlogBusiness.setBacklogDAO(backlogDAO);
        Backlog backlog = new Iteration();

        // Record expected behavior
        expect(backlogDAO.get(-100)).andReturn(null);
        replay(backlogDAO);
        // run method under test
        BacklogItem bli = backlogBusiness.createBacklogItemToBacklog(-100);
        assertEquals(null, bli);
        assertEquals(0, backlog.getBacklogItems().size());

        // verify behavior
        verify(backlogDAO);
    }

    public void testDeleteMultipleItems() {
        backlogDAO = createMock(BacklogDAO.class);
        backlogBusiness.setBacklogDAO(backlogDAO);
        bliDAO = createMock(BacklogItemDAO.class);
        backlogBusiness.setBacklogItemDAO(bliDAO);
        historyBusiness = createMock(HistoryBusiness.class);
        backlogBusiness.setHistoryBusiness(historyBusiness);

        Backlog backlog = new Iteration();
        backlog.setId(100);
        BacklogItem bli = new BacklogItem();
        bli.setId(68);
        bli.setBacklog(backlog);
        ArrayList<BacklogItem> blis = new ArrayList<BacklogItem>();
        blis.add(bli);
        backlog.setBacklogItems(blis);

        // Record expected behavior
        expect(backlogDAO.get(backlog.getId())).andReturn(backlog);
        bliDAO.remove(bli.getId());
        historyBusiness.updateBacklogHistory(backlog.getId());
        replay(backlogDAO);
        replay(bliDAO);
        replay(historyBusiness);

        // run method under test
        int[] bliIds = { bli.getId() };
        try {
            backlogBusiness.deleteMultipleItems(backlog.getId(), bliIds);
        } catch (ObjectNotFoundException e) {
            fail();
        }
        assertFalse(backlog.getBacklogItems().contains(bli));

        // verify behavior
        verify(backlogDAO);
        verify(bliDAO);
        verify(historyBusiness);
    }

    public void testSetAssignments() {
        backlogDAO = createMock(BacklogDAO.class);
        userDAO = createMock(UserDAO.class);
        assignmentDAO = createMock(AssignmentDAO.class);
        backlogBusiness.setBacklogDAO(backlogDAO);
        backlogBusiness.setUserDAO(userDAO);
        backlogBusiness.setAssignmentDAO(assignmentDAO);

        Backlog backlog = new Project();
        backlog.setId(100);
        // create users
        User user1, user2;
        user1 = new User();
        user1.setLoginName("user1");
        user2 = new User();
        user2.setLoginName("user2");
        user1.setId(1);
        user2.setId(2);
        Assignment assignment1 = new Assignment(user1, backlog);
        Assignment assignment2 = new Assignment(user2, backlog);

        // Record expected behavior
        expect(userDAO.get(user1.getId())).andReturn(user1);
        assignmentDAO.store(assignment1);
        userDAO.store(user1);
        backlogDAO.store(backlog);

        expect(userDAO.get(user2.getId())).andReturn(user2);
        assignmentDAO.store(assignment2);
        userDAO.store(user2);
        backlogDAO.store(backlog);

        replay(userDAO);

        // run method under test
        int[] selectedUserIds = { user1.getId(), user2.getId() };
        assertEquals(0, backlog.getAssignments().size());
        backlogBusiness.setAssignments(selectedUserIds,
                new HashMap<String, Assignment>(), backlog);
        assertEquals(2, backlog.getAssignments().size());

        // verify behavior
        verify(userDAO);

    }

    public void testMoveMultipleBacklogItems() {
        backlogDAO = createMock(BacklogDAO.class);
        bliDAO = createMock(BacklogItemDAO.class);
        historyBusiness = createMock(HistoryBusiness.class);
        backlogBusiness.setBacklogDAO(backlogDAO);
        backlogBusiness.setBacklogItemDAO(bliDAO);
        backlogBusiness.setHistoryBusiness(historyBusiness);

        Backlog iteration = new Iteration();
        Backlog project = new Project();
        IterationGoal iterationGoal = new IterationGoal();
        iteration.setId(121);
        project.setId(124);

        BacklogItem bli1 = new BacklogItem();
        BacklogItem bli2 = new BacklogItem();
        BacklogItem bli3 = new BacklogItem();
        bli1.setId(13);
        bli2.setId(14);
        bli3.setId(15);
        bli1.setIterationGoal(iterationGoal);
        bli2.setIterationGoal(iterationGoal);
        bli1.setBacklog(iteration);
        bli2.setBacklog(iteration);
        bli3.setBacklog(iteration);

        expect(backlogDAO.get(124)).andReturn(project);
        expect(bliDAO.get(13)).andReturn(bli1);
        bliDAO.store(bli1);
        backlogDAO.store(iteration);
        expect(bliDAO.get(15)).andReturn(bli3);
        bliDAO.store(bli3);
        backlogDAO.store(iteration);

        backlogDAO.store(project);
        historyBusiness.updateBacklogHistory(iteration.getId());
        historyBusiness.updateBacklogHistory(project.getId());

        replay(backlogDAO);
        replay(bliDAO);

        int[] ids = { 13, 15 };
        try {
            backlogBusiness.moveMultipleBacklogItemsToBacklog(ids, 124);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertNull(bli1.getIterationGoal());
        assertNull(bli3.getIterationGoal());
        assertEquals(iterationGoal, bli2.getIterationGoal());

        verify(backlogDAO);
        verify(bliDAO);
    }

    /**
     * Test the getWeekdaysLeftInBacklog method.
     */
    @SuppressWarnings("deprecation")
    public void testGetWeekdaysLeftInBacklog() {
        Date from = new Date(103, GregorianCalendar.MARCH, 3);

        // Create an iteration with 9 days left
        Iteration iter1 = new Iteration();
        iter1.setStartDate(new Date(103, GregorianCalendar.FEBRUARY, 24));
        iter1.setEndDate(new Date(103, GregorianCalendar.MARCH, 13));

        // Create a past iteration
        Iteration iter2 = new Iteration();
        iter2.setStartDate(new Date(102, GregorianCalendar.FEBRUARY, 24));
        iter2.setEndDate(new Date(102, GregorianCalendar.MARCH, 13));

        // Create an upcoming iteration
        Iteration iter3 = new Iteration();
        iter3.setStartDate(new Date(104, GregorianCalendar.MARCH, 1));
        iter3.setEndDate(new Date(104, GregorianCalendar.MARCH, 13));

        // Assertions
        assertEquals(9, backlogBusiness.getWeekdaysLeftInBacklog(iter1, from));
        assertEquals(0, backlogBusiness.getWeekdaysLeftInBacklog(iter2, from));
        assertEquals(10, backlogBusiness.getWeekdaysLeftInBacklog(iter3, from));
    }

    /**
     * Test the getNumberOfDaysForBacklogOnWeek method.
     */
    @SuppressWarnings("deprecation")
    public void testGetNumberOfDaysForBacklogOnWeek() {
        Date week9 = new Date(103, GregorianCalendar.FEBRUARY, 24);
        Date week10 = new Date(103, GregorianCalendar.MARCH, 3);
        Date week11 = new Date(103, GregorianCalendar.MARCH, 10);

        // Create an iteration with 9 days left
        Iteration iter1 = new Iteration();
        iter1.setStartDate(new Date(103, GregorianCalendar.FEBRUARY, 27));
        iter1.setEndDate(new Date(103, GregorianCalendar.MARCH, 13));

        // Assertions
        assertEquals(2, backlogBusiness.getNumberOfDaysForBacklogOnWeek(iter1,
                week9));
        assertEquals(5, backlogBusiness.getNumberOfDaysForBacklogOnWeek(iter1,
                week10));
        assertEquals(4, backlogBusiness.getNumberOfDaysForBacklogOnWeek(iter1,
                week11));
        
        // Project
        Project proj = new Project();
        proj.setStartDate(new Date(107, GregorianCalendar.DECEMBER, 30));
        proj.setEndDate(new Date(108, GregorianCalendar.JANUARY, 22));
        
        Date week53 = new Date(107, GregorianCalendar.DECEMBER, 31);
        Date week1 = new Date(108, GregorianCalendar.JANUARY, 5);
        Date week2 = new Date(108, GregorianCalendar.JANUARY, 9);
        Date week4 = new Date(108, GregorianCalendar.JANUARY, 21);
        
        // Assertions
        assertEquals(5, backlogBusiness.getNumberOfDaysForBacklogOnWeek(proj,
                week53));
        assertEquals(5, backlogBusiness.getNumberOfDaysForBacklogOnWeek(proj,
                week1));
        assertEquals(5, backlogBusiness.getNumberOfDaysForBacklogOnWeek(proj,
                week2));
        assertEquals(2, backlogBusiness.getNumberOfDaysForBacklogOnWeek(proj,
                week4));
      
    }

    /**
     * Test the calculation method for BacklogLoadData.
     */
    @SuppressWarnings("deprecation")
    public void testCalculateBacklogLoadData() {
        backlogDAO = createMock(BacklogDAO.class);
        userDAO = createMock(UserDAO.class);
        bliDAO = createMock(BacklogItemDAO.class);
        assignmentDAO = createMock(AssignmentDAO.class);

        // Create test data
        GregorianCalendar cal = new GregorianCalendar();
        cal.set(1998, 0, 1);
        cal.setFirstDayOfWeek(GregorianCalendar.MONDAY);

        User user = new User();
        user.setId(3);
        user.setLoginName("jorma");
        List<User> responsibles = new ArrayList<User>();
        responsibles.add(user);

        Project project = new Project();
        project.setId(4);
        project.setStartDate(new Date(98, 0, 1));
        project.setEndDate(new Date(98, 0, 31));
        project.setDefaultOverhead(new AFTime("1h"));

        BacklogItem bli1 = new BacklogItem();
        bli1.setId(10);
        bli1.setBacklog(project);
        bli1.setOriginalEstimate(new AFTime("100h"));
        bli1.setEffortLeft(new AFTime("22h")); // 1h effort per day
        bli1.setResponsibles(responsibles);

        BacklogItem bli2 = new BacklogItem();
        bli2.setId(11);
        bli2.setBacklog(project);
        bli2.setOriginalEstimate(new AFTime("150h"));
        bli2.setEffortLeft(new AFTime("33h")); // 1,5h effort per day
        bli2.setResponsibles(responsibles);

        BacklogItem bli3 = new BacklogItem();
        bli3.setId(12);
        bli3.setBacklog(project);
        bli3.setOriginalEstimate(new AFTime("150h"));
        bli3.setEffortLeft(new AFTime("44h")); // 2h effort per day
        bli3.setResponsibles(responsibles);
        
        project.setBacklogItems(new HashSet<BacklogItem>());
        project.getBacklogItems().add(bli1);
        project.getBacklogItems().add(bli2);
        project.getBacklogItems().add(bli3);
        
        Assignment ass = new Assignment();
        ass.setId(57);
        ass.setBacklog(project);
        ass.setUser(user);
        ass.setDeltaOverhead(new AFTime("1h"));
        
        Collection<Assignment> asses = new HashSet<Assignment>();
        asses.add(ass);
        project.setAssignments(asses);
        user.setAssignments(asses);

        // Record expected behavior
        expect(userDAO.get(3)).andReturn(user);
        expect(backlogDAO.get(4)).andReturn(project);

        // Test the behavior
        replay(backlogDAO);
        replay(userDAO);
        replay(bliDAO);

        user = userDAO.get(3);
        project = (Project) backlogDAO.get(4);
        BacklogLoadData data = backlogBusiness.calculateBacklogLoadData(
                project, user, cal.getTime(), 6);

        assertEquals(new AFTime("9h"), data.getEfforts().get(
                cal.get(GregorianCalendar.WEEK_OF_YEAR)));
        assertEquals(new AFTime("22h 30min"), data.getEfforts().get(
                cal.get(GregorianCalendar.WEEK_OF_YEAR) + 3));
        
        // Check the overheads
        assertEquals(new AFTime("48min"), data.getOverheads().get(
                cal.get(GregorianCalendar.WEEK_OF_YEAR)));
        assertEquals(new AFTime("2h"), data.getOverheads().get(
                cal.get(GregorianCalendar.WEEK_OF_YEAR) + 2));
        
        // Check the totals
        int week = cal.get(GregorianCalendar.WEEK_OF_YEAR);
        AFTime expect = new AFTime(data.getEfforts().get(week).getTime());
        expect.add(data.getOverheads().get(week));
        assertEquals(expect, data.getWeeklyTotals().get(week));
                
        
        // Check invalid weeks
        assertNull(data.getEfforts().get(cal.get(GregorianCalendar.WEEK_OF_YEAR) - 1));
        assertNull(data.getEfforts().get(cal.get(GregorianCalendar.WEEK_OF_YEAR) + 21));
        assertNull(data.getOverheads().get(cal.get(GregorianCalendar.WEEK_OF_YEAR) - 50));
        assertNull(data.getOverheads().get(cal.get(GregorianCalendar.WEEK_OF_YEAR) + 12));

        verify(backlogDAO);
        verify(userDAO);
        verify(bliDAO);
    }
}
