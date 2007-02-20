package fi.hut.soberit.agilefant.web.tag;

import javax.servlet.jsp.JspTagException;

import fi.hut.soberit.agilefant.db.BacklogItemDAO;
import fi.hut.soberit.agilefant.db.TaskDAO;
import fi.hut.soberit.agilefant.model.BacklogItem;
import fi.hut.soberit.agilefant.model.TaskStatus;

public class PercentDoneTag extends SpringTagSupport {
	
	private static final long serialVersionUID = 2586151152192294611L;
	private int backlogItemId;
	
	@Override
    public int doEndTag() throws javax.servlet.jsp.JspTagException
    {

		TaskDAO dao = (TaskDAO)super.getApplicationContext().getBean("taskDAO");
		BacklogItemDAO bliDao = (BacklogItemDAO)super.getApplicationContext().getBean("backlogItemDAO");
		BacklogItem bli = bliDao.get(backlogItemId);
		
		int done = dao.getTasksByStatusAndBacklogItem(bli, new TaskStatus[]{TaskStatus.DONE})
																	.size();
		int total = bli.getTasks()
						  .size();
		int percentDone = Math.round(done*100/total);
		
		try
        {
            super.getPageContext().getOut().write( String.valueOf(percentDone) );
        }
        catch(java.io.IOException e)
        {
            throw new JspTagException("IO Error: " + e.getMessage());
        }
        return EVAL_PAGE;
    }

	public void setBacklogItemId(int backlogItemId) {
		this.backlogItemId = backlogItemId;
	}
	
}