<%@ include file="./_taglibs.jsp"%>

<c:if test="${!empty projects}">

<h2>All items assigned to <c:out value="${user.fullName}" /> from ongoing projects</h2>

<div id="subItems">

<c:forEach items="${projects}" var="pro">


	<div id="subItemHeader">
		<ww:url id="parentActionUrl" action="editProduct" includeParams="none">
			<ww:param name="productId" value="${pro.product.id}" />
		</ww:url>
		<ww:a href="%{parentActionUrl}&contextViewName=dailyWork">
			<c:out value="${pro.product.name}" />
		</ww:a> &nbsp;&mdash;&nbsp;
		
		<ww:url id="parentActionUrl" action="editProject" includeParams="none">
			<ww:param name="projectId" value="${pro.id}" />
		</ww:url>
		<ww:a href="%{parentActionUrl}&contextViewName=dailyWork">
			<c:out value="${pro.name}" />
		</ww:a>
		
		<c:out value="(${pro.projectType.name})" /></div>
		
		<c:if test="${!aef:isUserAssignedTo(pro, user)}">
			<p style="color:#ff0000;">
			<img src="static/img/unassigned.png"
				title="The user has not been assigned to this project."
				alt="The user has not been assigned to this project." />
			<c:out value="${user.fullName}" /> has not been assigned to this project.
			</p>
		</c:if>


<div id="subItemContent">
<p>

<table class="dailyWorkBacklogItems">
<tr>
				<td class="backlogItemList"><display:table class="dailyWorkProject"
					name="${bliMap[pro]}" id="item2"
					requestURI="${currentAction}.action">

					<display:column sortable="true" sortProperty="name" title="Name"
						class="shortNameColumn">
						<ww:url id="editLink" action="editBacklogItem"
							includeParams="none">
							<ww:param name="backlogItemId" value="${item2.id}" />
						</ww:url>
						<div><ww:a href="%{editLink}&contextViewName=dailyWork">
			${aef:html(item2.name)}
		</ww:a></div>
					</display:column>

					<display:column sortable="true" title="Responsibles" class="responsibleColumn">
					<div><aef:responsibleColumn backlogItemId="${item2.id}"/></div>
					</display:column>

					<display:column sortable="true" defaultorder="descending"
						title="Priority">
						<ww:text name="backlogItem.priority.${item2.priority}" />
					</display:column>

					<display:column title="State" sortable="false" class="taskColumn">
						<c:set var="divId" value="${divId + 1}" scope="page" />
						<c:choose>
							<c:when test="${!(empty item2.tasks)}">
								<a href="javascript:toggleDiv(${divId});"
									title="Click to expand"> <c:out
									value="${fn:length(item2.tasks)}" /> tasks, <aef:percentDone
									backlogItemId="${item2.id}" />% done<br />
								<aef:stateList backlogItemId="${item2.id}" id="tsl" /> <ww:url
									id="imgUrl" action="drawExtendedBarChart" includeParams="none">
									<ww:param name="notStarted" value="${tsl['notStarted']}" />
									<ww:param name="started" value="${tsl['started']}" />
									<ww:param name="pending" value="${tsl['pending']}" />
									<ww:param name="blocked" value="${tsl['blocked']}" />
									<ww:param name="implemented" value="${tsl['implemented']}" />
									<ww:param name="done" value="${tsl['done']}" />
								</ww:url> <img src="${imgUrl}" /> </a>

								<aef:tasklist backlogItem="${item2}"
									contextViewName="${currentAction}"
									contextObjectId="${backlog.id}" divId="${divId}" />
							</c:when>
							<c:otherwise>
								<a href="javascript:toggleDiv(${divId});"
									title="Click to expand"> <ww:text
									name="task.state.${item2.state}" /><br />

								<c:choose>
									<c:when test="${item2.state == 'NOT_STARTED'}">
										<ww:url id="imgUrl" action="drawExtendedBarChart"
											includeParams="none">
											<ww:param name="notStarted" value="1" />
										</ww:url>
										<img src="${imgUrl}" />
									</c:when>
									<c:when test="${item2.state == 'STARTED'}">
										<ww:url id="imgUrl" action="drawExtendedBarChart"
											includeParams="none">
											<ww:param name="started" value="1" />
										</ww:url>
										<img src="${imgUrl}" />
									</c:when>
									<c:when test="${item2.state == 'PENDING'}">
										<ww:url id="imgUrl" action="drawExtendedBarChart"
											includeParams="none">
											<ww:param name="pending" value="1" />
										</ww:url>
										<img src="${imgUrl}" />
									</c:when>
									<c:when test="${item2.state == 'BLOCKED'}">
										<ww:url id="imgUrl" action="drawExtendedBarChart"
											includeParams="none">
											<ww:param name="blocked" value="1" />
										</ww:url>
										<img src="${imgUrl}" />
									</c:when>
									<c:when test="${item2.state == 'IMPLEMENTED'}">
										<ww:url id="imgUrl" action="drawExtendedBarChart"
											includeParams="none">
											<ww:param name="implemented" value="1" />
										</ww:url>
										<img src="${imgUrl}" />
									</c:when>
									<c:when test="${item2.state == 'DONE'}">
										<ww:url id="imgUrl" action="drawExtendedBarChart"
											includeParams="none">
											<ww:param name="done" value="1" />
										</ww:url>
										<img src="${imgUrl}" />
									</c:when>
								</c:choose> </a>
								<aef:tasklist backlogItem="${item2}"
									contextViewName="${currentAction}"
									contextObjectId="${backlog.id}" divId="${divId}" />
							</c:otherwise>
						</c:choose>
					</display:column>

					<display:column sortable="true" sortProperty="effortLeft"
						defaultorder="descending" title="Effort Left<br/>">
						<span style="white-space: nowrap"> <c:choose>
							<c:when test="${item2.effortLeft == null}">&mdash;
					</c:when>
							<c:otherwise>${item2.effortLeft}
					</c:otherwise>
						</c:choose> </span>
					</display:column>

					<display:column sortable="true" sortProperty="originalEstimate"
						defaultorder="descending" title="Original estimate">
						<c:choose>
							<c:when test="${item2.originalEstimate == null}">&mdash;</c:when>
							<c:otherwise>
								<c:out value="${item2.originalEstimate}" />
							</c:otherwise>
						</c:choose>
					</display:column>

					<display:footer>
						<tr>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
							<td>&nbsp;</td>
							<td><c:out value="${effortSums[pro]}" /></td>
							<td><c:out value="${originalEstimates[pro]}" /></td>
						</tr>
					</display:footer>
					
					


				</display:table></td>
				
				<td class="smallBurndownColumn">
						<div>
							<img src="drawSmallProjectChart.action?projectId=${pro.id}"/>
						</div>
					</td>

	</tr>
</table>



</div>

</c:forEach>
</div>

</c:if>