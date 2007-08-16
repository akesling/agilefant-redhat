<%@ include file="./inc/_taglibs.jsp" %>
<%@ include file="./inc/_header.jsp" %>
<aef:menu navi="product" /> 
	
<h2>No backlog selected</h2>

<p>
Please select a backlog from left hand side panel or create a new one from links below
</p>

<p>
	<ww:url id="createLink" action="createProduct" includeParams="none"/>
	<ww:a href="%{createLink}">Create a new product  &raquo;</ww:a>
	<br/>
	<ww:url id="createLink" action="createDeliverable" includeParams="none"/>
	<ww:a href="%{createLink}">Create a new project  &raquo;</ww:a>
	<br/>
	<ww:url id="createLink" action="createIteration" includeParams="none"/>
	<ww:a href="%{createLink}">Create a new iteration  &raquo;</ww:a>
	<br/>
	<ww:url id="createLink" action="createBacklogItem" includeParams="none"/>
	<ww:a href="%{createLink}">Create a new backlog item  &raquo;</ww:a>
	<br/>
	<ww:url id="createLink" action="createIterationGoal" includeParams="none"/>
	<ww:a href="%{createLink}">Create a new iteration goal  &raquo;</ww:a>
</p>




<%@ include file="./inc/_footer.jsp" %>