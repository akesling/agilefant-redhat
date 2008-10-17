<%@ include file="./_taglibs.jsp"%>

<aef:openDialogs context="bliWorkInProgress" id="openBacklogItemTabs" />

<!-- context variable for backlog item ajax to know its context -->
<c:set var="bliListContext" value="workInProgress" scope="session" />

<c:set var="dialogContext" value="bliWorkInProgress" scope="session" />

<script language="javascript" type="text/javascript">

$(document).ready(function() {        
    <c:forEach items="${openBacklogItemTabs}" var="openBacklogItem">
        handleTabEvent("backlogItemTabContainer-${openBacklogItem[0]}-${bliListContext}", "bliWorkInProgress", ${openBacklogItem[0]}, ${openBacklogItem[1]}, '${bliListContext}');
    </c:forEach>
});

</script>

<c:if test="${!(empty backlogItemsForUserInProgress)}">
<div class="subItems" id="subItems_backlogItemsForUserInProgress">

<div class="subItemContent">

<display:table name="backlogItemsForUserInProgress" id="row" requestURI="dailyWork.action">	
	
	<!-- Display the backlog row name -->
	<display:column sortable="true" sortProperty="name" title="Name">				
		<div style="overflow:hidden; width: 150px; max-height: 3.7em;">
		<c:forEach items="${row.businessThemes}" var="businessTheme">
            <a href="#" onclick="handleTabEvent('backlogItemTabContainer-${row.id}-${bliListContext}','bli',${row.id},3, '${bliListContext}'); return false;">
                <span class="businessTheme" title="${businessTheme.description}"><c:out value="${businessTheme.name}"/></span>
            </a>
        </c:forEach>
		<a class="nameLink" onclick="handleTabEvent('backlogItemTabContainer-${row.id}-${bliListContext}','bliWorkInProgress',${row.id},0,'${bliListContext}'); return false;">
			${aef:html(row.name)}
		</a>
		</div>
		<div id="backlogItemTabContainer-${row.id}-${bliListContext}" class="tabContainer" style="overflow:visible; white-space: nowrap; width: 0px;"></div>		
	</display:column>

	<!-- Display progress -->
	<display:column title="Progress" sortable="false" class="progressColumn">
		<div class="tinyfier">
			<aef:backlogItemProgressBar backlogItem="${row}" bliListContext="${bliListContext}" dialogContext="${dialogContext}" hasLink="${true}"/>
		</div>		
	</display:column>

	<!-- Display estimates -->
	<display:column title="Estimate" sortable="false" class="taskColumn">
		<div style="overflow:hidden; width: 55px; max-height: 3.7em;">
		<c:choose>	
			<c:when test="${row.effortLeft == null}">&mdash;</c:when>
			<c:otherwise><span style="color: #000">${row.effortLeft}</span></c:otherwise>
		</c:choose>	
		<br/>
		<c:choose>
			<c:when test="${row.originalEstimate == null}">&mdash;</c:when>
			<c:otherwise>
				<span style="color: #999">
				<c:out value="${row.originalEstimate}" />
				</span>
			</c:otherwise>
		</c:choose>
		</div>	
	</display:column>

</display:table>
</div>
</div>
</c:if>