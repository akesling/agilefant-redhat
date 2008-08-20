<%@ include file="./inc/_taglibs.jsp"%>
<%@ include file="./inc/_header.jsp"%>

<c:choose>
	<c:when test="${!empty project.id}">
		<c:set var="currentProjectId" value="${project.id}" scope="page" />
		<c:if test="${project.id != previousProjectId}">
			<c:set var="previousProjectId" value="${project.id}" scope="session" />
		</c:if>
	</c:when>
	<c:otherwise>
		<c:set var="currentProjectId" value="${previousProjectId}"
			scope="page" />
	</c:otherwise>
</c:choose>

<c:choose>
	<c:when test="${project.id == 0}">
		<aef:bct productId="${productId}" />
	</c:when>
	<c:otherwise>
		<aef:bct projectId="${projectId}" />
	</c:otherwise>
</c:choose>

<c:set var="divId" value="1336" scope="page" />
<aef:menu navi="backlog" pageHierarchy="${pageHierarchy}" />
<ww:actionerror />
<ww:actionmessage />
<script type="text/javascript">
<!--
$(document).ready(function() {
	var iterationThemes = [<c:forEach items="${iterationThemes}" var="bind">${bind.businessTheme.id},</c:forEach>-1];
	var getThemeData = function() {
		var ret = {};
		var data = jsonDataCache.get('themesByProduct',{data: {productId: ${project.product.id}}},${project.product.id});
		jQuery.each(data,function() {
			if(this.active === true && jQuery.inArray(this.id,iterationThemes) == -1) {
				ret[this.id] = this.name;
			}
		});
		return ret;
	};
	$('#businessThemeTable').inlineTableEdit({add: '#addProjectBusinessTheme', 
											  submit: '#backlogThemeSave',
											  submitParam: 'bindingId',
											  deleteaction: 'removeThemeFromBacklog.action',
											  fields: {
											  	businessThemeIds: {cell: 0,type: 'select', data: getThemeData},
											  	plannedSpendings: {cell: 1, type: 'text' },
											  	reset: {cell: 2, type: 'reset'}
											  }
											 });
});
//-->
</script>
<ww:date name="%{new java.util.Date()}" id="start"
	format="%{getText('webwork.shortDateTime.format')}" />
<ww:date name="%{new java.util.Date()}" id="end"
	format="%{getText('webwork.shortDateTime.format')}" />

<c:if test="${project.id > 0}">
	<ww:date name="%{project.startDate}" id="start"
		format="%{getText('webwork.shortDateTime.format')}" />
	<ww:date name="%{project.endDate}" id="end"
		format="%{getText('webwork.shortDateTime.format')}" />
</c:if>

<%--  TODO: fiksumpi virheenkäsittely --%>
<c:choose>
	<c:when test="${empty projectTypes}">
		<ww:url id="workTypeLink" action="createProjectType"
			includeParams="none" />	
				No project types available. <ww:a href="%{workTypeLink}">Create a new project type &raquo;</ww:a>
	</c:when>
	<c:otherwise>
		<aef:productList />
			<h2><c:out value="${project.name}" /></h2>
				<table>
					<tbody>
						<tr>
							<td>
								<div class="subItems" style="margin-top: 0">
									<div class="subItemHeader">
										<script type="text/javascript">
											function expandDescription() {
												document.getElementById('descriptionDiv').style.maxHeight = "1000em";
												document.getElementById('descriptionDiv').style.overflow = "visible";
											}
											function collapseDescription() {
												document.getElementById('descriptionDiv').style.maxHeight = "12em";
												document.getElementById('descriptionDiv').style.overflow = "hidden";
											}
											function editProject() {
												toggleDiv('editProjectForm'); toggleDiv('descriptionDiv'); showWysiwyg('projectDescription'); return false;
											}
										</script>
										<table cellspacing="0" cellpadding="0">
											<tr>
												<td class="header">Details <a href="" onclick="return editProject();">Edit &raquo;</a></td>
												<td class="icons">
													<%--<a href="" onclick="toggleDiv('editProjectForm'); toggleDiv('descriptionDiv'); return false;">
														<img src="static/img/edit.png" width="18" height="18" alt="Edit" title="Edit" />
													</a>--%>
													<a href="" onclick="expandDescription(); return false;">
														<img src="static/img/plus.png" width="18" height="18" alt="Expand" title="Expand" />
													</a>
													<a href="" onclick="collapseDescription(); return false;">
														<img src="static/img/minus.png" width="18" height="18" alt="Collapse" title="Collapse" />
													</a>
												</td>
											</tr>
										</table>
									</div>
									<div class="subItemContent">
										<div id="descriptionDiv" class="descriptionDiv" style="display: block;">
											<table class="infoTable" cellpadding="0" cellspacing="0">
												<tr>
								    				<th class="info1">Status</th>
								    				<td class="info3" ondblclick="return editProject();">
								    				<c:choose>
														<c:when test="${project.status == 'OK'}">
															<img src="static/img/status-green.png" alt="OK" title="OK"/>
														</c:when>
														<c:when test="${project.status == 'CHALLENGED'}">
															<img src="static/img/status-yellow.png" alt="Challenged" title="Challenged"/>
														</c:when>
														<c:when test="${project.status == 'CRITICAL'}">
															<img src="static/img/status-red.png" alt="Critical" title="Critical"/>
														</c:when>
													</c:choose>
													<ww:text name="project.status.${project.status}" />
								    				</td>
								    				<!--  <td class="info3" ondblclick="return editProject();"><ww:text name="project.status.${project.status}" /></td> -->
								    				
								    				<td class="info4" rowspan="7">
	                                					<c:if test="${(!empty project.backlogItems) && (empty project.iterations)}">
	                                    					<div class="smallBurndown"><a href="#bigChart">
	                                    						<img src="drawSmallProjectChart.action?projectId=${project.id}"/>
	                                    					</a></div>
	                                					
	                                					
                    									<table>
										                  <tr>
											                 <th>Velocity</th>
											                 <td><c:out value="${projectMetrics.dailyVelocity}" /> /
											                 day</td>
										                  </tr>
										                  <c:if test="${projectMetrics.backlogOngoing}">
										                  <tr>
											                 <th>Schedule variance</th>
											                 <td><c:choose>
												                    <c:when test="${projectMetrics.scheduleVariance != null}">
													                   <c:choose>
                                                                       <c:when test="${projectMetrics.scheduleVariance > 0}">
                                                                            <span class="red">+
                                                                       </c:when>
                                                                        <c:otherwise>
                                                                            <span>
                                                                        </c:otherwise>
                                                                        </c:choose>
													                   <c:out value="${projectMetrics.scheduleVariance}" /> days
													                   </span>
                                                                </c:when>
												                    <c:otherwise>
                                                                    unknown
                                                                </c:otherwise>
											                 </c:choose></td>
										                  </tr>
										                  <tr>
											                 <th>Scoping needed</th>
											                 <td><c:choose>
												                    <c:when test="${projectMetrics.scopingNeeded != null}">
													                   <c:out value="${projectMetrics.scopingNeeded}" />
												                    </c:when>
												                    <c:otherwise>
                                                                    unknown
                                                                </c:otherwise>
											                 </c:choose></td>
										                  </tr>
										                  </c:if>
										                  <tr>
											                 <th>Completed</th>
											                 <td><c:out value="${projectMetrics.percentDone}" />% (<c:out
												                    value="${projectMetrics.completedItems}" /> / <c:out
												                    value="${projectMetrics.totalItems}" />)</td>
										                  </tr>
									                   </table>
									                   </c:if>
									                </td>							
												</tr>
												<tr>
													<th class="info1">Project type</th>
													<td class="info3" ondblclick="return editProject();"><c:out value="${project.projectType.name}" /></td>													
												</tr>
												<tr>
								    				<th class="info1">Default overhead</th>
								    				<td class="info3" ondblclick="return editProject();"><c:out value="${project.defaultOverhead}"/> / person / week</td>							
												</tr>
																				
										         <tr>
													<th class="info1">Planned project size</th>
													<td><c:out value="${project.backlogSize}"/></td>
												</tr>	
												<tr>
	                                				<th class="info1">Timeframe</th>
	                                				<td class="info3" ondblclick="return editProject();"><c:out value="${project.startDate.date}.${project.startDate.month + 1}.${project.startDate.year + 1900}" /> - 
	                                					<c:out value="${project.endDate.date}.${project.endDate.month + 1}.${project.endDate.year + 1900}" /></td>
												</tr>
												<tr>
								    				<th class="info1">Assignees</th>
								    				<td class="info3"><c:set var="listSize"
	                                        			value="${fn:length(project.responsibles)}" scope="page" />
	                                        			<c:choose>
	                                        				<c:when test="${listSize > 0}">
	                                            				<c:set var="count" value="0" scope="page" />
	                                            				<c:forEach items="${project.responsibles}" var="responsible">
	                                                				<ww:url id="userDailyWorkLink" action="dailyWork"
	                                                            		includeParams="none">
	                                                            		<ww:param name="userId" value="${responsible.id}"/>
	                                                				</ww:url>
	                                                				<c:choose>
	                                                    				<c:when test="${count < listSize - 1}">
	                                                        				<a href="${userDailyWorkLink}"><c:out value="${responsible.initials}" /></a>,
	                                                    				</c:when>
	                                                    				<c:otherwise>
	                                                        				<ww:a href="${userDailyWorkLink}">
	                                                            				<c:out value="${responsible.initials}" />
	                                                        				</ww:a>
	                                                    				</c:otherwise>
	                                                				</c:choose>
	                                                				<c:set var="count" value="${count + 1}" scope="page" />
	                                            				</c:forEach>
	                                        				</c:when>
	                                        				<c:otherwise>
	                                            				<c:out value="none" />
	                                        				</c:otherwise>
	                                    				</c:choose>
	                                    			</td>
												</tr>
												<tr>
								    				<td colspan="2" class="description">${project.description}</td>
												</tr>
											</table>
										</div>
										<div id="editProjectForm" style="display: none;" class="validateWrapper validateProject">
											<ww:form id="projectEditForm" action="storeProject" method="post">
												<ww:hidden name="projectId" value="${project.id}" />
												<table class="formTable">
													<tr>
														<td>Name</td>
														<td>*</td>
														<td colspan="2"><ww:textfield size="60" name="project.name" /></td>
													</tr>
													<tr>
														<td>Product</td>
														<td>*</td>
														<td colspan="2">
															<select name="productId">
																<option class="inactive" value="">(select product)</option>
																	<c:forEach items="${productList}" var="product">
																		<c:choose>
																			<c:when test="${product.id == currentProductId}">
																				<option selected="selected" value="${product.id}"
																					title="${product.name}">${aef:out(product.name)}</option>
																			</c:when>
																			<c:otherwise>
																				<option value="${product.id}" title="${product.name}">${aef:out(product.name)}</option>
																			</c:otherwise>
																		</c:choose>
																	</c:forEach>
															</select>
														</td>
													</tr>
													<tr>
														<td>Project type</td>
														<td></td>
														<td colspan="2">
															<ww:select name="projectTypeId"
																list="#attr.projectTypes" listKey="id" listValue="name"
																value="${project.projectType.id}" />
														</td>
													</tr>
													<tr>
														<td>Status</td>
														<td></td>
														<td colspan="2">															
															<ww:select name="project.status"
																id="statusSelect"
																value="project.status.name"
																list="@fi.hut.soberit.agilefant.model.Status@values()" listKey="name"
																listValue="getText('project.status.' + name())"	/>
														</td>
													</tr>
													<tr>
														<td>Default Overhead</td>
														<td></td>
														<td colspan="2"><ww:textfield size="10" name="project.defaultOverhead" /> / person / week</td>
													</tr>
						                			<tr>
														<td>Planned project size</td>
														<td></td>
														<td colspan="2"><ww:textfield size="10" id="project.backlogSize" name="project.backlogSize" /> (total man hours)
														</td>
													</tr>													
													<tr>
														<td>Start date</td>
														<td>*</td>
														<td colspan="2">
														<%--<ww:datepicker value="%{#start}" size="15"
															showstime="true" format="%{getText('webwork.datepicker.format')}"
															name="startDate" />--%>
														<aef:datepicker id="start_date" name="startDate" format="%{getText('webwork.shortDateTime.format')}" value="%{#start}" />
														</td>
													</tr>
													<tr>
														<td>End date</td>
														<td>*</td>
														<td colspan="2">
														<%--<ww:datepicker value="%{#end}" size="15"
															showstime="true" format="%{getText('webwork.datepicker.format')}"
															name="endDate" />--%>
														<aef:datepicker id="end_date" name="endDate" format="%{getText('webwork.shortDateTime.format')}" value="%{#end}" />
														</td>
													</tr>
													<tr>
														<td>Assigned Users</td>
														<td></td>
														<td colspan="2">
															<c:set var="divId" value="1" scope="page" />
															<div id="assigneesLink">
																<a href="javascript:toggleDiv(${divId});">
																	<img src="static/img/users.png" />
																	<c:set var="listSize" value="${fn:length(project.responsibles)}" scope="page" />
																	<c:choose>
																		<c:when test="${listSize > 0}">
																			<c:set var="count" value="0" scope="page" />
																			<c:forEach items="${project.responsibles}" var="responsible">
																				<c:choose>
																					<c:when test="${count < listSize - 1}">
																						<c:out value="${responsible.initials}" /><c:out value="${','}" />
																					</c:when>
																					<c:otherwise>
																						<c:out value="${responsible.initials}" />
																					</c:otherwise>
																				</c:choose>
																				<c:set var="count" value="${count + 1}" scope="page" />
																			</c:forEach>
																		</c:when>
																		<c:otherwise>
																			<c:out value="none" />
																		</c:otherwise>
																	</c:choose>
																</a>
															</div>
															<div id="${divId}" style="display: none;">
																<display:table name="${assignableUsers}" id="user" class="projectUsers"
																	defaultsort="2">
																	<display:column title="">
																		<%--<c:forEach var="usr" items="${assignedUsers}">
																			<c:if test="${usr.id == user.id}">
																				<c:set var="flag" value="1" scope="request" />
																			</c:if>
																		</c:forEach>--%>
								
																		<%-- Test, if the user should be checked --%>
																		<c:set var="flag" value="0" scope="request" />
																		<c:if test="${aef:listContains(assignedUsers, user)}">
	                                										<c:set var="flag" value="1" scope="request" />
																		</c:if>
																		<c:choose>
																			<c:when test="${flag == 1}">
																				<input type="checkbox" name="selectedUserIds"
																					value="${user.id}" checked="checked" class="user_${user.id}" onchange="toggleDiv('${user.id}')"/>
																			</c:when>
																			<c:otherwise>
																				<input type="checkbox" name="selectedUserIds"
																					value="${user.id}" class="user_${user.id}" onchange="toggleDiv('${user.id}')"/>
																			</c:otherwise>
																		</c:choose>
																	</display:column>
																	<display:column title="User" sortProperty="fullName">
	    					    										<c:choose>
							    											<c:when test="${unassignedHasWork[user] == 1}">
							      												<span style="color: red">					    
							    											</c:when>
							    											<c:otherwise>
							      												<span>
							    											</c:otherwise>
							    										</c:choose>
							    										<c:out value="${user.fullName}" />
		                        										<%-- Test, if the user is not enabled --%>					    
							    										<c:if test="${aef:listContains(disabledUsers, user)}">
	                                										<img src="static/img/disable_user_gray.png" alt="The user is disabled" title="The user is disabled" />
	                            										</c:if>
																		</span>
																	</display:column>
																	<display:column title="Overhead +/-">
																	<!-- Check whether user is assigned. If is assigned -> show overhead -->
																		<c:choose>	
																		<c:when test="${flag == 1}"> 
																			<div id="${user.id}" class="overhead">
																		</c:when>
																		<c:otherwise>
																			<div id="${user.id}" class="overhead" Style="display: none;">
																		</c:otherwise>
																		</c:choose>
																		<c:choose>
																		<c:when test="${empty project.defaultOverhead}">
																			<ww:label value="0h" />
																		</c:when>
																		<c:otherwise>
																			<ww:label value="${project.defaultOverhead}" />
																		</c:otherwise>
																		</c:choose>																		
																		+
																		<ww:hidden name="assignments['${user.id}'].user.id" 
																			value="${user.id}"  />																			
																		<ww:textfield size="3"  name="assignments['${user.id}'].deltaOverhead" /> =
																		
																		<c:choose>
																		<c:when test="${!empty totalOverheads[user.id]}">	
																			<ww:label value="${totalOverheads[user.id]}" />
																		</c:when>
																		<c:otherwise>																			
																			<c:choose>
																				<c:when test="${empty project.defaultOverhead}">
																					<ww:label value="0h" />
																				</c:when>
																				<c:otherwise>
																					<ww:label value="${project.defaultOverhead}" />
																				</c:otherwise>
																			</c:choose>	
																		</c:otherwise>
																		</c:choose>																																																
																		
																		</div>																		
																	</display:column>
																</display:table>
																<div id="userselect" class="projectTeams">
																	<div class="right">
																		<label>Teams</label>
																		<ul class="groups" />
																	</div>
																	<script type="text/javascript">
																		$(document).ready( function() {
																			<aef:teamList />
																			<ww:set name="teamList" value="#attr.teamList" />
																			var teams = [<aef:teamJson items="${teamList}"/>]
																			$('#userselect').multiuserselect({groups: teams, root: $('#user')});
																		});
																	</script>
																</div>
															</div>
														</td>
													</tr>
	                								<tr>
	                    								<td>Description</td>
	                    								<td></td>
	                    								<td colspan="2"><ww:textarea cols="70" rows="10" id="projectDescription"
	                        								name="project.description" value="${aef:nl2br(project.description)}" /></td>
	                								</tr>
													<tr>
														<td></td>
														<td></td>
														<c:choose>
															<c:when test="${projectId == 0}">
																<td><ww:submit value="Create" /></td>
															</c:when>
															<c:otherwise>
																<td><ww:submit value="Save" id="saveButton" /></td>
																<td class="deleteButton"><ww:submit
																	onclick="return confirmDelete()" action="deleteProject"
																	value="Delete" /></td>
															</c:otherwise>
														</c:choose>
													</tr>
												</table>
											</ww:form>
										</div>
									</div>
									
									<%---Link for entering a new hour entry---%>
									<aef:hourReporting id="hourReport"/>
									<c:if test="${hourReport == 'true'}">
										<div id="subItemHeader" style="border:none; border-top:1px solid #ccc; background: none;">
											<table cellpadding="0" cellspacing="0">
												<tbody>
													<tr>
				   										<td class="header">
				   											<ww:url id="createLink" action="ajaxCreateHourEntry" includeParams="none">
				   												<ww:param name="backlogId" value="${projectId}" />
				   											</ww:url>
					   										<ww:a cssClass="openCreateDialog openUserDialog" title="Log effort" href="%{createLink}">Log effort &raquo;</ww:a>
					   									</td>
													</tr>
												</tbody>
											</table>
										</div>
									</c:if>
									
								</div>
							</td>
						</tr>
					</tbody>
				</table>
		<table>
			<tr>
				<td>

								<c:if test="${projectId != 0}">
									<div class="subItems">
										<div class="subItemHeader">
										    <table cellspacing="0" cellpadding="0">
					            			    <tr>
					            			    	<td class="header">Themes <a id="addProjectBusinessTheme" href="#">Attach theme &raquo;</a></td>
												</tr>
											</table>
										</div>
										<div class="subItemContent">
										<ww:form action="storeBacklogThemebinding" method="post">
										<ww:hidden name="backlogId" value="${project.id}"/>
										<input type="hidden" name="contextViewName" value="project" />
										<div class="businessThemeTableWrapper">
										<c:choose>
										<c:when test="${!empty project.businessThemeBindings}">
										<display:table htmlId="businessThemeTable" class="listTable" name="project.businessThemeBindings" id="row" requestURI="editIteration.action">

											<display:column sortable="true" title="Name" sortProperty="businessTheme.name">
												<span style="display: none;">${row.businessTheme.id}</span>
												<c:out value="${row.businessTheme.name}"/>
											</display:column>
											
											<display:column sortable="true" sortProperty="boundEffort" title="Planned spending">
												<c:choose>
													<c:when test="${row.relativeBinding == true}">
														<span style="display:none;">${row.percentage}</span>
														<c:out value="${row.boundEffort}"/>
														(<c:out value="${row.percentage}"/>%)
													</c:when>
													<c:otherwise><c:out value="${row.fixedSize}"/></c:otherwise>
												</c:choose>
											</display:column>
											<display:column sortable="false" title="Actions">
												<span class="uniqueId" style="display: none;">${row.id}</span>
												<img style="cursor: pointer;" class="table_edit_edit" src="static/img/edit.png" title="Edit" />
												<img style="cursor: pointer;" class="table_edit_delete" src="static/img/delete_18.png" title="Delete" />
											</display:column>
										</display:table>
											</c:when>
											<c:otherwise>
												<table id="businessThemeTable" style="display:none;" class="listTable">
													<tr><th class="sortable">Name</th><th class="sortable">Planned spending</th><th>Actions</th></tr>
												</table>
											</c:otherwise>
											</c:choose>
											<input id="backlogThemeSave" style="display: none; margin-left: 2px;" type="submit" value="Save" />
											</div>
											</ww:form>				
											</div>
											<c:if test="${!empty iterationThemes}">
											<div class="businessThemeTableWrapper">
											<h4>Iteration themes</h4>
												<display:table htmlId="businessThemeTable" class="listTable" name="iterationThemes" id="row" requestURI="editProject.action">
						
													<display:column sortable="true" title="Name" sortProperty="businessTheme.name">
														<c:out value="${row.businessTheme.name}"/>
													</display:column>
													
													<display:column sortable="true" sortProperty="boundEffort" title="Planned spending">
														<c:choose>
															<c:when test="${row.relativeBinding == true}">
																<c:out value="${row.boundEffort}"/>
																(<c:out value="${row.percentage}"/>%)
															</c:when>
															<c:otherwise><c:out value="${row.fixedSize}"/></c:otherwise>
														</c:choose>
													</display:column>
													<display:column sortable="true" title="Iteration" sortProperty="backlog.name">
														<c:out value="${row.backlog.name}"/>
													</display:column>
												</display:table>
										    </div>
											</c:if>
											</div>
								</c:if>
								
				</td>
				</tr>
				<tr>
				<td>
					<c:if test="${project.id > 0}">
						<div class="subItems">
							<div class="subItemHeader">
								<table cellpadding="0" cellspacing="0">
									<tr>
					   					<td class="header">Iterations
					   						<ww:url id="createLink" action="ajaxCreateIteration" includeParams="none" >
						  						<ww:param name="projectId" value="${project.id}" />
					   						</ww:url>
					   						<ww:a
												href="%{createLink}&contextViewName=editProject&contextObjectId=${project.id}" cssClass="openCreateDialog openIterationDialog">Create new &raquo;</ww:a>
					   					</td>
									</tr>
								</table>
							</div>
							<c:if test="${!empty project.iterations}">
								<div class="subItemContent">
										<display:table class="listTable" name="project.iterations"
											id="row" requestURI="editProject.action">
											<display:column sortable="true" sortProperty="name" title="Name"
												class="shortNameColumn">
												<ww:url id="editLink" action="editIteration"
													includeParams="none">
													<ww:param name="iterationId" value="${row.id}" />
												</ww:url>
												<ww:a
													href="%{editLink}&contextViewName=editProject&contextObjectId=${project.id}">
													${aef:html(row.name)}
												</ww:a>
											</display:column>
											<display:column sortable="true" title="Items">
												${fn:length(row.backlogItems)}
											</display:column>
											<%-- REFACTOR THIS --%>
											<display:column sortable="true" title="Effort left"
												sortProperty="totalEffortLeftSum.time"
												defaultorder="descending">
												${effLeftSums[row]}
											</display:column>
											<display:column sortable="true" title="Original estimate"
												sortProperty="totalOriginalEstimateSum.time"
												defaultorder="descending">
												${origEstSums[row]}
											</display:column>

											<display:column sortable="true" title="Start date">
												<ww:date name="#attr.row.startDate" />
											</display:column>
											<display:column sortable="true" title="End date">
												<ww:date name="#attr.row.endDate" />
											</display:column>
											<display:column sortable="false" title="Actions">
												<ww:url id="deleteLink" action="deleteIteration"
													includeParams="none">
													<ww:param name="projectId" value="${project.id}" />
													<ww:param name="iterationId" value="${row.id}" />
												</ww:url>
												<ww:a
													href="%{deleteLink}&contextViewName=editProject&contextObjectId=${project.id}"
													onclick="return confirmDelete()"><img src="static/img/delete_18.png" alt="Delete" title="Delete" /></ww:a>
											</display:column>
										</display:table>
								</div>
							</c:if>
							</div>
							<div class="subItems">
							<div class="subItemHeader">
								<table cellpadding="0" cellspacing="0">
                    				<tr>
                       					<td class="header">Backlog items <ww:url
												id="createBacklogItemLink" action="ajaxCreateBacklogItem"
												includeParams="none">
												<ww:param name="backlogId" value="${project.id}" />
											</ww:url>
											<ww:a cssClass="openCreateDialog openBacklogItemDialog"
												href="%{createBacklogItemLink}&contextViewName=editProject&contextObjectId=${project.id}">Create new &raquo;</ww:a>
										</td>
									</tr>
								</table>
							</div>
							<c:if test="${!empty project.backlogItems}">
								<div class="subItemContent">
									<%@ include	file="./inc/_backlogList.jsp"%>
								</div>
							</c:if>
						</div>
						<c:if test="${!empty project.backlogItems}">
							<c:if test="${empty project.iterations}">
								<p>
									<img src="drawProjectChart.action?projectId=${project.id}" id="bigChart"
									   width="780" height="600" />
								</p>
							</c:if>
						</c:if>
					</c:if>
				</td>
			</tr>
		</table>
	</c:otherwise>
</c:choose>

<%-- Hour reporting here - Remember to expel David H. --%>

<aef:hourReporting id="hourReport"></aef:hourReporting>
<c:if test="${hourReport == 'true' && projectId != 0}">
	<c:set var="myAction" value="editProject" scope="session" />
	<%@ include file="./inc/_hourEntryList.jsp"%>
</c:if> <%-- Hour reporting on --%>

<%@ include file="./inc/_footer.jsp"%>