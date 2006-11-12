<%@ include file="./inc/_taglibs.jsp" %>
<html>
<head>  <link rel="stylesheet" href="/agilefant/static/css/aef07.css" type="text/css">
<ww:head/>
<title>Deliverable list - AgilEfant</title>
</head>
<%@ include file="./inc/_header.jsp" %>
<%@ include file="./inc/_navi_left.jsp" %>
    <div id="upmenu">

      <li class="normal"><a>Help</a>

      </li>
    </div>
	<p>
		<c:choose>
			<c:when test="${empty deliverables}">
				No deliverables were found.
			</c:when>
			<c:otherwise>
				<c:forEach items="${deliverables}" var="deliverable">
				<p>
					<ww:url id="editLink" action="editDeliverable" includeParams="none">
						<ww:param name="deliverableId" value="${deliverable.id}"/>
					</ww:url>
					<ww:url id="deleteLink" action="deleteDeliverable" includeParams="none">
						<ww:param name="deliverableId" value="${deliverable.id}"/>
					</ww:url>
					${deliverable.name} - <ww:a href="%{editLink}">Edit</ww:a>|<ww:a href="%{deleteLink}">Delete</ww:a>
					</p>
				</c:forEach>
			</c:otherwise>
		</c:choose>			
	</p>
	<p>
		<ww:url id="createDeliverableLink" action="createDeliverable" includeParams="none"/>
		<ww:a href="%{createDeliverableLink}">Create new</ww:a>
	</p>
<%@ include file="./inc/_footer.jsp" %>
