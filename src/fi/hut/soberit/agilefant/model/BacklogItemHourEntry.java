package fi.hut.soberit.agilefant.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.BatchSize;
/**
 * Hibernate entity bean which represents an hour entry owned by a backlog item.
 * 
 * Represents a job effort logged for a specific backlog item. 
 *  
 * @see fi.hut.soberit.agilefant.model.HourEntry
 * @author User
 *
 */
@Entity
@BatchSize(size=20)
public class BacklogItemHourEntry extends HourEntry {
    private BacklogItem backlogItem;

    @ManyToOne(
            targetEntity = fi.hut.soberit.agilefant.model.BacklogItem.class
    )
    @JoinColumn(nullable = true)
    public BacklogItem getBacklogItem() {
        return backlogItem;
    }

    public void setBacklogItem(BacklogItem backlogItem) {
        this.backlogItem = backlogItem;
    }
}
