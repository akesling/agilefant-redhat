<%@ include file="./inc/_taglibs.jsp" %>
<%@ include file="./inc/_header.jsp" %>			
<c:set var="did" value="1336" scope="page"/>

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
			View | <ww:a href="%{editIterationLink}">Edit </ww:a>		

		</p>
		
		<aef:productList/>
		<ww:form action="contextView">
		<ww:hidden name="contextName" value="iteration"/> 
			<p>
				<select name="contextObjectId">
					<c:forEach items="${productList}" var="product">
						<c:forEach items="${product.deliverables}" var="deliverable">
							<c:forEach items="${deliverable.iterations}" var="iter">
								<c:choose>
									<c:when test="${iteration.id == iter.id}">
										<option selected="selected" value="${iter.id}">${aef:out(iter.name)}</option>
									</c:when>
									<c:otherwise>
										<option value="${iter.id}">${aef:out(iter.name)}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</c:forEach>						
					</c:forEach>				
			<ww:submit value="Select iteration"/>
				</select>
			</p>			
		</ww:form>
				
			
			

			
<c:if test="${!empty iteration}">
			
		<c:if test="${iteration.id > 0}">
			<h2>Iteration ${iteration.name}  <ww:date name="iteration.startDate" /> -  <ww:date name="iteration.endDate" /></h2>
			
			
		<p>	
			<img src="drawChart.action?iterationId=${iteration.id}"/>
		</p>

<table><tr><td>

			<div id="subItems">
		<div id="subItemHeader">
			Subitems
		</div>
		<div id="subItemContent">
	</c:if>
			


	<c:if test="${!empty iteration.backlogItems}"> 

		<aef:currentUser/>

		<p>
			Backlog items
		</p>
		<p>
			<display:table name="iteration.backlogItems" id="row" requestURI="viewIteration.action">
				<display:column sortable="true" title="Name" property="name"/>


				<display:column title="Tasks" sortable="false">
				<c:if test="${!empty row.tasks}"> 

							<c:set var="did" value="${did + 1}" scope="page"/>
							
							<a href="javascript:toggleDiv(${did});" title="Click to expand">${fn:length(row.tasks)} tasks, ??% complete</a>

		<table cellspacing="0" cellpadding="0" border="0" class="chartTable">
		<tr>
		<td height="5" width="10%" class="notStarted"><img height="5" src="static/img/clear.gif"></td>
		<td title="asdasdf" height="5" width="40%"  class="started"><img height="5" src="static/img/clear.gif"></td>
		<td  height="5" width="10%" class="implemented"><img height="5" src="static/img/clear.gif"></td>
		<td  height="5" width="20%" class="done"><img height="5" src="static/img/clear.gif"></td>
		<td  height="5" width="20%" class="blocked"><img height="5" src="static/img/clear.gif"></td>
		</tr>
		</table>
							<div id="${did}" style="display:none;">
							<c:forEach items="${row.tasks}" var="task">
								<ww:url id="editLink" action="editTask" includeParams="none">
									<ww:param name="taskId" value="${task.id}"/>
								</ww:url>
								<ww:a href="%{editLink}">${aef:out(task.name)} - ${task.status}</ww:a>								
								<br/>
							</c:forEach>
							</div>

							


				    </c:if>

				</display:column>
<%-- 
				<display:column sortable="true" title="Effort estimate" sortProperty="remainingEffortEstimate.time">
					${row.remainingEffortEstimate}
				</display:column>
				<display:column sortable="true" title="Effort in tasks" sortProperty="taskEffortLeft.time">
					${row.taskEffortLeft}
				</display:column>
--%>
				<display:column sortable="false" title="Assignee" >
					${row.assignee.fullName}
				</display:column>
				<display:column sortable="false" title="Priority" >
					${row.priority}
				</display:column>
				<display:column sortable="true" title="Iteration Goal">
					${row.iterationGoal.name}
				</display:column>
				<display:column sortable="true" title="Effort">
					${row.performedEffort}
				</display:column>
				<display:column sortable="true" title="Estimate">
					${row.effortEstimate}
				</display:column>
				

				<display:column sortable="false" title="Actions">
					<ww:url id="editLink" action="editBacklogItem" includeParams="none">
						<ww:param name="backlogItemId" value="${row.id}"/>
					</ww:url>
                    <ww:url id="deleteLink" action="deleteBacklogItem" includeParams="none"> 
                            <ww:param name="backlogItemId" value="${row.id}"/> 
                    </ww:url> 
                    <ww:a href="%{editLink}">Edit</ww:a>|<ww:a href="%{deleteLink}">Delete</ww:a> 					
					
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
				<display:column sortable="true" title="Name" property="name"/>
				<display:column sortable="true" title="Status" >
					${row.status}
				</display:column>
				<display:column sortable="true" title="Description" property="description"/>
				<display:column sortable="true" title="Priority" property="priority"/>
				<display:column sortable="false" title="Actions">
					<ww:url id="editLink" action="editIterationGoal" includeParams="none">
						<ww:param name="iterationGoalId" value="${row.id}"/>
					</ww:url>
					<ww:a href="%{editLink}">Edit</ww:a>
				</display:column>
			</display:table>
		</p>
	</c:if>

</c:if>


	
		<c:if test="${iteration.id > 0}">

</div>
</div>
</td></tr></table>

	</c:if>

<%@ include file="./inc/_footer.jsp" %>
