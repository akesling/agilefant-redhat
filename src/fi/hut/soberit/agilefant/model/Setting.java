package fi.hut.soberit.agilefant.model;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;

import fi.hut.soberit.agilefant.web.page.PageItem;

/**
 * Hibernate entity bean representing a Setting.
 * <p>
 * @todo Add description here
 */
@BatchSize(size=20)
@Entity
@Table(name = "setting")
public class Setting implements PageItem {

    private int id;
    
    private String name;
    
    private String description;
    
    private String value;
    
    /**
     * Get the id of this object.
     */
    // tag this field as the id
    @Id
    // generate automatically
    @GeneratedValue(strategy = GenerationType.AUTO)
    // not nullable
    @Column(nullable = false)
    public int getId() {
        return id;
    }
    
    /**
     * Set the id of this object.
     * <p>
     * You shouldn't normally call this.
     */
    public void setId(int id) {
        this.id = id;
    }

    @Type(type = "escaped_truncated_varchar")
    @Column(unique = true, nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Type(type = "escaped_truncated_varchar")
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Type(type = "escaped_truncated_varchar")
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value; 
    }
    
    @Transient
    public PageItem getParent() {
        return null;
    }

    @Transient
    public Collection<PageItem> getChildren() {
        return null;
    }
    
    @Transient
    public boolean hasChildren() {
        return false;
    }
}
