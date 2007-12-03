<%@ include file="./inc/_taglibs.jsp"%>
<%@ include file="./inc/_header.jsp"%>

<!-- Author:	aptoivon
	 Version:	1.3.1
-->

<aef:menu navi="3" />
<ww:actionerror />
<ww:actionmessage />

<h2>Edit Project Rank</h2>

<h4>Ranked Projects</h4>
<p><display:table name="${ongoingRankedProjects}" id="row">
	<display:column title="Project Rank">
		<c:out value="${row_rowNum}" />
	</display:column>
	<display:column title="Project Name">
		<ww:a
			href="contextView.action?contextObjectId=${row.id}&resetContextView=true&contextName=project">
			<c:out value="${row.product.name}: ${row.name}" />
		</ww:a>
	</display:column>
	<display:column title="Action">
		<ww:url id="moveUpLink" action="moveProjectUp">
			<ww:param name="projectId" value="${row.id}" />
		</ww:url>
		<ww:a href="%{moveUpLink}">Up</ww:a>

		<ww:url id="moveDownLink" action="moveProjectDown">
			<ww:param name="projectId" value="${row.id}" />
		</ww:url>
		<ww:a href="%{moveDownLink}">Down</ww:a>

		<ww:url id="moveTopLink" action="moveProjectTop">
			<ww:param name="projectId" value="${row.id}" />
		</ww:url>
		<ww:a href="%{moveTopLink}">Top</ww:a>

		<ww:url id="moveBottomLink" action="moveProjectBottom">
			<ww:param name="projectId" value="${row.id}" />
		</ww:url>
		<ww:a href="%{moveBottomLink}">Bottom</ww:a>
	</display:column>
</display:table></p>

<h4>Unranked Projects</h4>
<p><display:table name="${ongoingUnrankedProjects}" id="row">
	<display:column title="Project Rank">
		<c:out value="?" />
	</display:column>
	<display:column title="Project Name">
		<ww:a
			href="contextView.action?contextObjectId=${row.id}&resetContextView=true&contextName=project">
			<c:out value="${row.product.name}: ${row.name}" />
		</ww:a>
	</display:column>
	<display:column title="Action">
		<ww:url id="moveTopLink" action="moveProjectTop">
			<ww:param name="projectId" value="${row.id}" />
		</ww:url>
		<ww:a href="%{moveTopLink}">Top</ww:a>

		<ww:url id="moveBottomLink" action="moveProjectBottom">
			<ww:param name="projectId" value="${row.id}" />
		</ww:url>
		<ww:a href="%{moveBottomLink}">Bottom</ww:a>
	</display:column>
</display:table></p>

<%@ include file="./inc/_footer.jsp"%>