package fi.hut.soberit.agilefant.business.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.support.PropertyComparator;

import fi.hut.soberit.agilefant.business.BacklogBusiness;
import fi.hut.soberit.agilefant.business.BacklogItemBusiness;
import fi.hut.soberit.agilefant.business.JSONBusiness;
import fi.hut.soberit.agilefant.business.TeamBusiness;
import fi.hut.soberit.agilefant.business.UserBusiness;
import fi.hut.soberit.agilefant.exception.ObjectNotFoundException;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.Assignment;
import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.Project;
import fi.hut.soberit.agilefant.model.Team;
import fi.hut.soberit.agilefant.model.User;
import flexjson.JSONSerializer;

public class JSONBusinessImpl implements JSONBusiness {

    private UserBusiness userBusiness;
    private BacklogBusiness backlogBusiness;
    private BacklogItemBusiness backlogItemBusiness;
    private TeamBusiness teamBusiness;

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    public String getUserChooserJSON(int backlogItemId, int backlogId) {
        BacklogItem bli = null;
        Backlog backlog = null;
        Collection<Integer> assignments = new ArrayList<Integer>();
        Collection<Integer> responsibles = new ArrayList<Integer>();
        Map<Integer, AFTime> deltaOverheads = new HashMap<Integer, AFTime>();

        String backlogJson = "";
        String userJson = "";
        String teamJson = "";
        String assignmentJson = "";
        String responsibleJson = "";
        String overheadJson = "";

        try {
            bli = backlogItemBusiness.getBacklogItem(backlogItemId);
            if (backlogId > 0) {
                backlog = backlogBusiness.getBacklog(backlogId);
            }
            else if (bli != null) {
                backlog = bli.getBacklog();
            }
        } catch (ObjectNotFoundException onfe) {
        }

        /*
         * Get the assignments.
         */
        if (backlog != null) {
            Project proj = null;
            if (backlog instanceof Iteration) {
                proj = ((Iteration) backlog).getProject();
            } else if (backlog instanceof Project) {
                proj = (Project) backlog;
            }
            if (proj != null) {
                for (Assignment ass : proj.getAssignments()) {
                    assignments.add(ass.getUser().getId());
                    deltaOverheads.put(ass.getUser().getId(), ass.getDeltaOverhead());
                }
            }
        }
        /*
         * Get the bli's responsibles.
         */
        if (bli != null) {
            for (User user : bli.getResponsibles()) {
                responsibles.add(user.getId());
            }
        }
        /*
         * Get all teams and users as json
         */
        List<User> users = userBusiness.getAllUsers();
        Collections.sort(users, new PropertyComparator("fullName", false, true));
        List<Team> teams = teamBusiness.getAllTeams();
        Collections.sort(teams, new PropertyComparator("name", false, true));
        
        userJson = new JSONSerializer().include("id").include("fullName")
                .include("initials").include("enabled").exclude("*").serialize(users);
        teamJson = new JSONSerializer().include("users.id").exclude("users.*")
                .serialize(teams);

        /* Get the other jsons */
        assignmentJson = new JSONSerializer().serialize(assignments);
        responsibleJson = new JSONSerializer().include("id").exclude("*")
                .serialize(responsibles);
        overheadJson = new JSONSerializer().serialize(deltaOverheads);
        backlogJson = new JSONSerializer().include("id").include("defaultOverhead").exclude("*").serialize(backlog);

        return "{users:" + userJson + ",teams:" + teamJson + ",assignments:"
                + assignmentJson + ",responsibles:" + responsibleJson + "," +
                "overheads:" + overheadJson + "," +
                "backlog:" + backlogJson + "}";
    }
    
    /** {@inheritDoc}} */
    public String objectToJSON(Object object) {
        return new JSONSerializer().serialize(object);
    }

    /*
     * AUTOGENERATED LIST OF GETTERS AND SETTERS
     */
    public void setUserBusiness(UserBusiness userBusiness) {
        this.userBusiness = userBusiness;
    }

    public void setBacklogBusiness(BacklogBusiness backlogBusiness) {
        this.backlogBusiness = backlogBusiness;
    }

    public void setBacklogItemBusiness(BacklogItemBusiness backlogItemBusiness) {
        this.backlogItemBusiness = backlogItemBusiness;
    }

    public void setTeamBusiness(TeamBusiness teamBusiness) {
        this.teamBusiness = teamBusiness;
    }
}
