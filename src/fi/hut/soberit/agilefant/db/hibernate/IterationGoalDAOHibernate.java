package fi.hut.soberit.agilefant.db.hibernate;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import fi.hut.soberit.agilefant.db.IterationGoalDAO;
import fi.hut.soberit.agilefant.model.AFTime;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.Iteration;
import fi.hut.soberit.agilefant.model.IterationGoal;
import fi.hut.soberit.agilefant.model.State;
import fi.hut.soberit.agilefant.util.IterationGoalMetrics;

/**
 * Hibernate implementation of IterationGoalDAO interface using
 * GenericDAOHibernate.
 */
public class IterationGoalDAOHibernate extends
        GenericDAOHibernate<IterationGoal> implements IterationGoalDAO {

    public IterationGoalDAOHibernate() {
        super(IterationGoal.class);
    }

    
    @SuppressWarnings("unchecked")
    public List<IterationGoal> getGoalsByIteration(Iteration iter) {
        DetachedCriteria crit = DetachedCriteria.forClass(IterationGoal.class);
        crit.add(Restrictions.eq("iteration", iter));
        List<IterationGoal> goals = this.getHibernateTemplate().findByCriteria(crit);
        this.getHibernateTemplate().evict(goals);
        return goals;
    }

    public IterationGoalMetrics loadIterationGoalMetrics(
            IterationGoal iterationGoal, Iteration iteration) {
        //effort left & OE and total blis
        DetachedCriteria crit = DetachedCriteria.forClass(BacklogItem.class);
        ProjectionList sums = Projections.projectionList();
        sums.add(Projections.sum("originalEstimate"));
        sums.add(Projections.sum("effortLeft"));
        sums.add(Projections.count("id"));
        crit.setProjection(sums);
        if(iterationGoal == null) {
            crit.add(Restrictions.isNull("iterationGoal"));
        } else {
            crit.add(Restrictions.eq("iterationGoal", iterationGoal));
        }
        crit.add(Restrictions.eq("backlog", iteration));
        Object[] sumData = (Object[])this.getHibernateTemplate().findByCriteria(crit).get(0);
        
        //done blis
        DetachedCriteria doneCrit = DetachedCriteria.forClass(BacklogItem.class);
        ProjectionList doneProj = Projections.projectionList();
        doneProj.add(Projections.count("id"));
        doneCrit.setProjection(doneProj);
        if(iterationGoal == null) {
            doneCrit.add(Restrictions.isNull("iterationGoal"));
        } else {
            doneCrit.add(Restrictions.eq("iterationGoal", iterationGoal));
        }
        doneCrit.add(Restrictions.eq("backlog", iteration));
        doneCrit.add(Restrictions.eq("state", State.DONE));
        Object doneData = this.getHibernateTemplate().findByCriteria(doneCrit).get(0);
        
        //spent effort
        DetachedCriteria spentEffCrit = DetachedCriteria.forClass(BacklogItem.class);
        ProjectionList spentEffProj = Projections.projectionList();
        spentEffProj.add(Projections.sum("he.timeSpent"));
        spentEffCrit.createAlias("hourEntries", "he");
        spentEffCrit.setProjection(spentEffProj);
        if(iterationGoal == null) {
            spentEffCrit.add(Restrictions.isNull("iterationGoal"));
        } else {
            spentEffCrit.add(Restrictions.eq("iterationGoal", iterationGoal));
        }
        spentEffCrit.add(Restrictions.eq("backlog", iteration));
        Object spentEffData = this.getHibernateTemplate().findByCriteria(spentEffCrit).get(0);
        
        IterationGoalMetrics metrics = new IterationGoalMetrics();
        metrics.setOriginalEstimate((AFTime)sumData[0]);
        metrics.setEffortLeft((AFTime)sumData[1]);
        metrics.setEffortSpent((AFTime)spentEffData);
        metrics.setTotalTasks((Integer)sumData[2]);
        metrics.setDoneTasks((Integer)doneData);
        
        return metrics;
    }

}
