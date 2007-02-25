<%@ include file="./inc/_taglibs.jsp" %>
<%@ include file="./inc/_header.jsp" %>			
<c:set var="divId" value="1336" scope="page"/>

		<c:choose>
			<c:when test="${!empty iteration.id}">
				<c:set var="currentIterationId" value="${iteration.id}" scope="page"/>
				<c:if test="${iteration.id != previousIterationId}">
					<c:set var="previousIterationId" value="${iteration.id}" scope="session"/>
				</c:if>
			</c:when>
			<c:otherwise>
				<c:set var="currentIterationId" value="${previousIterationId}" scope="page"/>
			</c:otherwise>
		</c:choose>			

<aef:bct deliverableId="${deliverableId}"/>

<aef:menu navi="${contextName}"  pageHierarchy="${pageHierarchy}"/> 
	<ww:actionerror/>
	<ww:actionmessage/>
		<p>
			<ww:url id="editIterationLink" action="editIteration" includeParams="none">
				<ww:param name="iterationId" value="${iteration.id}"/>
			</ww:url>
			View | <ww:a href="%{editIterationLink}&contextViewName=viewIteration&contextObjectId=${iteration.id}">Edit </ww:a>		

		</p>
<aef:iterationmenu iterationId="${iteration.id}"/>
			
<h2>Iteration</h2>

			
<c:if test="${!empty iteration}">
			
		<c:if test="${iteration.id > 0}">
			<h2>Iteration ${iteration.name}  <ww:date name="iteration.startDate" /> -  <ww:date name="iteration.endDate" /></h2>
			
<table><tr><td>

			<div id="subItems">
		<div id="subItemHeader">
			Subitems
		</div>
		<div id="subItemContent">
	</c:if>

	<p>Backlog items
		<ww:url id="createBacklogItemLink" action="createBacklogItem" includeParams="none">
			<ww:param name="backlogId" value="${iteration.id}"/>
		</ww:url>
		<ww:a href="%{createBacklogItemLink}&contextViewName=viewIteration&contextObjectId=${iteration.id}">Create new &raquo;</ww:a>
	</p>

	<c:if test="${!empty iteration.backlogItems}"> 

		<aef:currentUser/>

		<p>
			<display:table name="iteration.backlogItems" id="row" requestURI="viewIteration.action">
			<display:column sortable="true" title="Name">
				${aef:outTitle(row.name)}
			</display:column>


				<display:column title="Tasks" sortable="false">
				<c:if test="${!empty row.tasks}"> 

							<c:set var="divId" value="${divId + 1}" scope="page"/>
							
							<a href="javascript:toggleDiv(${divId});" title="Click to expand">
								${fn:length(row.tasks)} tasks, <aef:percentDone backlogItemId="${row.id}"/> % done<br/>
   								<aef:taskStatusList backlogItemId="${row.id}" id="tsl"/>							   
 	<img src="drawExtendedBarChart.action?notStarted=${tsl['notStarted']}&started=${tsl['started']}&blocked=${tsl['started']}&implemented=${tsl['implemented']}&done=${tsl['done']}"/> 
							</a>
							<aef:tasklist tasks="${row.tasks}"  contextViewName="viewIteration"  contextObjectId="${iteration.id}" divId="${divId}"/>

							


				    </c:if>

				</display:column>
				<display:column sortable="false" title="Assignee" >
					${aef:out(row.assignee.fullName)}
				</display:column>
				<display:column sortable="false" title="Priority" >
				<ww:text name="backlogItem.priority.${row.priority}"/>

				</display:column>
				<display:column sortable="true" title="Iteration Goal">
					${aef:out(row.iterationGoal.name)}
				</display:column>
				<display:column sortable="true" title="Effort done">
					${row.performedEffort}
				</display:column>
				<display:column sortable="true" title="Estimate">
					<c:choose>
						<c:when test="!empty ${row.effortEstimate}">
							${row.effortEstimate}
						</c:when>
						<c:otherwise>
							${row.allocatedEffort}
						</c:otherwise>
					</c:choose>
				</display:column>
				

				<display:column sortable="false" title="Actions">
					<ww:url id="editLink" action="editBacklogItem" includeParams="none">
						<ww:param name="backlogItemId" value="${row.id}"/>
					</ww:url>
                    <ww:url id="deleteLink" action="deleteBacklogItem" includeParams="none"> 
                            <ww:param name="backlogItemId" value="${row.id}"/> 
                    </ww:url> 
                    <ww:a href="%{editLink}&contextViewName=viewIteration&contextObjectId=${iteration.id}">Edit</ww:a>|<ww:a href="%{deleteLink}&contextViewName=viewIteration&contextObjectId=${iteration.id}">Delete</ww:a> 					
					
				</display:column>
			  <display:footer>
			  	<tr>
			  		<td>Total:</td>
			  		<td>&nbsp;</td>
			  		<td>&nbsp;</td>
			  		<td>&nbsp;</td>
			  		<td>&nbsp;</td>
			  		<td><c:out value="${iteration.performedEffort}" /></td>
			  		<td><c:out value="${iteration.effortEstimate}" /></td>
			  	<tr>
			  </display:footer>				


			</display:table>
		</p>
	</c:if>

	<c:if test="${!empty iteration.iterationGoals}">
		<p>
			Iteration goals
		</p>
		<p>
			<display:table name="iteration.iterationGoals" id="row" requestURI="editIteration.action">
			<display:column sortable="true" title="Name">
				${aef:outTitle(row.name)}
			</display:column>
				<display:column sortable="true" title="Status" >
					${row.status}
				</display:column>
				<display:column sortable="true" title="Description">
					${aef:out(row.description)}
				</display:column>
					
				<display:column sortable="true" title="Priority" property="priority"/>
				<display:column sortable="false" title="Actions">
					<ww:url id="editLink" action="editIterationGoal" includeParams="none">
						<ww:param name="iterationGoalId" value="${row.id}"/>
					</ww:url>
					<ww:a href="%{editLink}&contextViewName=viewIteration&contextObjectId=${iteration.id}">Edit</ww:a>
				</display:column>
			</display:table>
		</p>
	</c:if>

</c:if>


	
		<c:if test="${iteration.id > 0}">

</div>
</div>
</td></tr></table>

		<p>	
			<img src="drawChart.action?iterationId=${iteration.id}"/>
		</p>

	</c:if>

<%@ include file="./inc/_footer.jsp" %>
