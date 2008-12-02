package fi.hut.soberit.agilefant.web.tag;

import javax.servlet.jsp.JspException;

import fi.hut.soberit.agilefant.model.Backlog;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.BusinessTheme;
import fi.hut.soberit.agilefant.model.IterationGoal;

public class ResolveObjectNamespaceTag extends SpringTagSupport {
    
    private static final long serialVersionUID = -921028995149059552L;
    
    /**
     * Where to set the resolved name-space.
     */
    private String var = "";
    /**
     * Item to be resolved to a name-space.
     */
    private Object item;
    
    @Override
    public int doStartTag() throws JspException {

        String namespace = "";
        int oid;
        
        if(this.var == null || this.var.length() < 1) {
            return SKIP_BODY;
        }
        if(this.item == null) {
            return SKIP_BODY;
        } else if(this.item instanceof Backlog) {
            namespace = "BL";
            oid = ((Backlog)this.item).getId();
        } else if(this.item instanceof BusinessTheme) {
            namespace = "TH";
            oid = ((BusinessTheme)this.item).getId();
        } else if(this.item instanceof BacklogItem) {
            namespace = "BLI";
            oid = ((BacklogItem)this.item).getId();
        } else if(this.item instanceof IterationGoal) {  
            namespace = "IG";
            oid = ((IterationGoal)this.item).getId();
        } else {
            return SKIP_BODY;
        }
        String unique = namespace + ":" + oid;
        
        this.getPageContext().setAttribute(this.var, unique);
        return EVAL_BODY_INCLUDE;
    }
    
    @Override
    public int doEndTag() throws JspException {
        if(this.var != null && this.var.length() > 0) {
            this.getPageContext().removeAttribute(this.var);
        }
        return super.doEndTag();
    }

    //AUTOGENERATED 

    public Object getItem() {
        return item;
    }

    public void setItem(Object item) {
        this.item = item;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

}
