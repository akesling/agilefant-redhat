package fi.hut.soberit.agilefant.business;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.ArrayList;

import junit.framework.TestCase;
import fi.hut.soberit.agilefant.business.impl.BacklogItemBusinessImpl;
import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.State;
import fi.hut.soberit.agilefant.model.User;

public class BacklogItemBusinessTest extends TestCase {

    private BacklogItemBusinessImpl bliBusiness = new BacklogItemBusinessImpl();
    private BacklogItemDAO bliDAO;
    private HistoryBusiness historyBusiness = createMock(HistoryBusiness.class);

    public void testRemoveBacklogItem_found() {
        bliDAO = createMock(BacklogItemDAO.class);
        bliBusiness.setBacklogItemDAO(bliDAO);
        bliBusiness.setHistoryBusiness(historyBusiness);
        Backlog backlog = new Iteration();
        backlog.setId(100);
        BacklogItem bli = new BacklogItem();
        bli.setBacklog(backlog);

        // Record expected behavior
        expect(bliDAO.get(68)).andReturn(bli);
        bliDAO.remove(bli);
        replay(bliDAO);
        historyBusiness.updateBacklogHistory(100);
        replay(historyBusiness);
        // run method under test

        try {
            bliBusiness.removeBacklogItem(68);
        } catch (ObjectNotFoundException onfe) {
            fail();
        }
        // verify behavior
        verify(bliDAO);
        verify(historyBusiness);
    }

    public void testRemoveBacklogItem_notfound() {
        bliDAO = createMock(BacklogItemDAO.class);
        bliBusiness.setBacklogItemDAO(bliDAO);

        // Record expected behavior
        expect(bliDAO.get(68)).andReturn(null);
        replay(bliDAO);
        // run method under test
        try {
            bliBusiness.removeBacklogItem(68);
            fail();
        } catch (ObjectNotFoundException onfe) {

        }

        // verify behavior
        verify(bliDAO);
    }

    private void testUpdateBacklogItemStateAndEffortLeft_parameterized(
            State oldState, AFTime oldOriginalEstimate, AFTime oldEffortLeft,
            State newState, AFTime expectedOriginalEstimate,
            AFTime newEffortLeft, AFTime expectedEffortLeft) {

        // Declare test values
        final int bliId = 68;
        final int backlogId = 99;

        // Create bli mock
        bliDAO = createMock(BacklogItemDAO.class);
        bliBusiness.setBacklogItemDAO(bliDAO);
        bliBusiness.setHistoryBusiness(historyBusiness);

        // Create new backlog item and set test values to it
        BacklogItem bli = new BacklogItem();
        bli.setId(bliId);
        bli.setEffortLeft(oldEffortLeft);
        bli.setOriginalEstimate(oldOriginalEstimate);
        bli.setState(oldState);

        // Create backlog for backlog item
        Backlog backlog = new Iteration();
        backlog.setId(backlogId);
        bli.setBacklog(backlog);

        // Record expectations for mocks
        // bliDao mock
        expect(bliDAO.get(bliId)).andReturn(bli);
        bliDAO.store(bli);
        replay(bliDAO);

        // historyBusiness mock
        historyBusiness.updateBacklogHistory(backlogId);
        replay(historyBusiness);

        try {
            bliBusiness.updateBacklogItemStateAndEffortLeft(bliId, newState,
                    newEffortLeft);
        } catch (ObjectNotFoundException onfe) {
            fail("Object not found.");
        }

        assertEquals(newState, bli.getState());
        assertEquals(expectedEffortLeft, bli.getEffortLeft());
        assertEquals(expectedOriginalEstimate, bli.getOriginalEstimate());

        verify(bliDAO);
        verify(historyBusiness);
    }

    /**
     * Basic test to test the common functionality, when the backlog item is
     * given a new state and effort left.
     */

    public void testUpdateBacklogItemStateAndEffortLeft_basic() {
        testUpdateBacklogItemStateAndEffortLeft_parameterized(
                State.NOT_STARTED, new AFTime("10h"), new AFTime("3h 20min"),
                State.IMPLEMENTED, new AFTime("10h"), new AFTime("1h"),
                new AFTime("1h"));
    }

    /**
     * Expect the new original estimate to be the same as new effort left if it
     * is originally null
     */

    public void testUpdateBacklogItemStateAndEffortLeft_nullOriginalEstimate() {

        testUpdateBacklogItemStateAndEffortLeft_parameterized(
                State.NOT_STARTED, null, new AFTime("3h 20min"),
                State.IMPLEMENTED, new AFTime("1h"), new AFTime("1h"),
                new AFTime("1h"));
    }

    /**
     * Check that the effort left is zeroed when the the status is set to DONE.
     * Even if the business method is given a new non-zero effort left value as
     * parameter.
     */
    public void testUpdateBacklogItemStateAndEffortLeft_newStateDone() {

        testUpdateBacklogItemStateAndEffortLeft_parameterized(
                State.NOT_STARTED, new AFTime("10h"), new AFTime("3h 20min"),
                State.DONE, new AFTime("10h"), new AFTime("100h"), new AFTime(
                        "0h"));
    }

    /**
     * Test the getResponsibleInitialsString method of BacklogItemBusiness.
     */
    public void testGetResponsibleInitialsString() {
        bliDAO = createMock(BacklogItemDAO.class);
        bliBusiness.setBacklogItemDAO(bliDAO);
        
        User user1 = new User();
        user1.setInitials("US1");
        user1.setId(1);
        User user2 = new User();
        user2.setInitials("US2");
        user2.setId(2);
        
        BacklogItem bli1 = new BacklogItem();
        bli1.setId(5);
        BacklogItem bli2 = new BacklogItem();
        bli2.setId(6);
        
        bli1.setResponsibles(new ArrayList<User>());
        bli2.setResponsibles(new ArrayList<User>());
        
        bli1.getResponsibles().add(user1);
        bli2.getResponsibles().add(user1);
        bli2.getResponsibles().add(user2);
        
        // Record the behavior
        expect(bliDAO.get(5)).andReturn(bli1);
        expect(bliDAO.get(6)).andReturn(bli2);
        bliBusiness.getResponsibleInitialsString(bli1).equals("US1");
        bliBusiness.getResponsibleInitialsString(bli2).equals("US1, US2");
        replay(bliDAO);
        
        bli1 = bliDAO.get(5);
        bli2 = bliDAO.get(6);
        
        bliBusiness.getResponsibleInitialsString(bli1);
        bliBusiness.getResponsibleInitialsString(bli2);
        
        verify(bliDAO);
    }
}
