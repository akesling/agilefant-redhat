<%@ include file="./inc/_taglibs.jsp"%>
<%@ include file="./inc/_header.jsp"%>


<aef:projectTypeList id="projectTypes"/>
<aef:openDialogs context="iteration" id="openIterations" />

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
<aef:menu navi="backlog" pageHierarchy="${pageHierarchy}" title="${project.name}"/>
<ww:actionerror />
<ww:actionmessage />
<aef:hourReporting id="hourReport" />
<script type="text/javascript">
<!--
$(document).ready(function() {
	<c:forEach items="${openIterations}" var="openIteration">
        handleTabEvent("iterationTabContainer-${openIteration[0]}", "iteration", ${openIteration[0]}, ${openIteration[1]});
    </c:forEach>

	var iterationThemes = [<c:forEach items="${iterationThemes}" var="bind">${bind.businessTheme.id},</c:forEach>-1];
	var getThemeData = function() {
		var ret = {};
		var data = jsonDataCache.get('themesByProduct',{data: {productId: ${project.product.id}, includeGlobalThemes: true}},${project.product.id});
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
											  	plannedSpendings: {cell: 1, type: 'text'},											  											  	
											  	reset: {cell: 2, type: 'reset'}
											  }
											 });
    $('#userChooserLink-editProject').userChooser({
        backlogIdField: '#editProject-projectId',
        userListContainer: '#userListContainer-editProject',
        renderFor: 'project',
        validation: {
            selectAtLeast: 0,
            aftime: true
        },
        backlogItemId: 0
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

<%--  TODO: fiksumpi virheenk�sittely --%>
		<aef:productList />
			<h2><c:out value="${project.name}" /></h2>
				<table>
					<tbody>
						<tr>
							<td>
								<div class="subItems" style="margin-top: 0" id="subItems_editProjectDetails">
									<div class="subItemHeader">
										<script type="text/javascript">
											function editProject() {
												toggleDiv('editProjectForm'); toggleDiv('descriptionDiv'); showWysiwyg('projectDescription'); return false;
											}
										</script>
										  <table cellspacing="0" cellpadding="0">
											<tr>
                                                <td class="iconsbefore">
						                        </td>
												<td class="header">Details</td>
												<td class="icons">
												<table cellpadding="0" cellspacing="0">
												<tr>
												
                                                <c:if test="${hourReport}">
                                                <td>
                                                        <ww:url id="createLink" action="ajaxCreateHourEntry" includeParams="none">
                                                            <ww:param name="backlogId" value="${projectId}" />
                                                        </ww:url>
                                                    <ww:a cssClass="openCreateDialog openUserDialog logEffortLink"
                                                    onclick="return false;" title="Log effort" href="%{createLink}">
												    </ww:a>
												    </td>
                                                  </c:if>
                                               <td>
                                                  <a href="#" onclick="editProject(); return false;"
                                                    class="editLink" title="Edit project details" /> 
                                                </td>
                                                </tr>
                                                </table>
						                        </td>
											</tr>
										</table>
									</div>
									<div class="subItemContent">
										<div id="descriptionDiv" class="descriptionDiv" style="display: block;">
											<table class="infoTable" cellpadding="0" cellspacing="0">
												<tr>
													<th class="info1"><ww:text name="general.uniqueId"/></th>
													<td class="info3"><aef:quickReference item="${project}" /></td>
													<td class="info4" rowspan="8">
                                                        <c:if test="${(!empty project.backlogItems) && (projectBurndown || (empty project.iterations))}">
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
                                                             <th>Done</th>
                                                             <td><c:out value="${projectMetrics.percentDone}" />% (<c:out
                                                                    value="${projectMetrics.completedItems}" /> / <c:out
                                                                    value="${projectMetrics.totalItems}" />)</td>
                                                          </tr>
                                                       </table>
                                                       </c:if>
                                                    </td>
												</tr>
												<tr>
								    				<th class="info1">Status</th>
								    				<td class="info3" ondblclick="return editProject();">
								    				<c:choose>
														<c:when test="${project.status == 'GREEN'}">
															<img src="static/img/status-green.png" alt="Green" title="Green"/>
														</c:when>
														<c:when test="${project.status == 'YELLOW'}">
															<img src="static/img/status-yellow.png" alt="Yellow" title="Yellow"/>
														</c:when>
														<c:when test="${project.status == 'RED'}">
															<img src="static/img/status-red.png" alt="Red" title="Red"/>
														</c:when>
														<c:when test="${project.status == 'GREY'}">
															<img src="static/img/status-grey.png" alt="Grey" title="Grey"/>
														</c:when>
														<c:when test="${project.status == 'BLACK'}">
															<img src="static/img/status-black.png" alt="Black" title="Black"/>
														</c:when>
													</c:choose>
													<ww:text name="project.status.${project.status}" />
								    				</td>
								    				<td></td>						
												</tr>
												<tr>
													<th class="info1">Project type</th>
													<td class="info3" ondblclick="return editProject();">
													<c:choose>
													<c:when test="${(!empty project.projectType)}">
														<c:out value="${project.projectType.name}" />
													</c:when>
													<c:otherwise>
														undefined
													</c:otherwise>
													</c:choose>
													</td>													
												</tr>
												<c:if test="${(project.defaultOverhead != null) && (project.defaultOverhead.time > 0)}">
												<tr>
								    				<th class="info1">Baseline load</th>
								    				<td class="info3" ondblclick="return editProject();">
								    					<c:choose>
								    					<c:when test="${(!empty project.defaultOverhead)}">
								    						<c:out value="${project.defaultOverhead}"/> / person / week
								    					</c:when>
								    					<c:otherwise>
								    						-
								    					</c:otherwise>
								    					</c:choose>
								    				</td>							
												</tr>
											     </c:if>				
										         <tr>
													<th class="info1">Planned project size</th>
													<td class="info3" ondblclick="return editProject();">
														<c:choose>
														<c:when test="${(!empty project.backlogSize)}">
															<c:out value="${project.backlogSize}"/>h
														</c:when>
														<c:otherwise>
															-
														</c:otherwise>
														</c:choose>
													</td>
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
	                                                        				<%--<a href="${userDailyWorkLink}"><c:out value="${responsible.initials}" /></a>,--%>
	                                                        				<c:out value="${responsible.initials}" />, 
	                                                    				</c:when>
	                                                    				<c:otherwise>
	                                                        				<%--<ww:a href="${userDailyWorkLink}"> --%>
	                                                            				<c:out value="${responsible.initials}" />
	                                                        				<%-- </ww:a>--%>
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
								    				<td colspan="2" class="description">
								    				<div class="backlogDescription">${project.description}</div></td>
												</tr>
											</table>
										</div>
										<div id="editProjectForm" style="display: none;" class="validateWrapper validateProject">
											<ww:form id="projectEditForm" action="storeProject" method="post">
												<ww:hidden id="editProject-projectId" name="projectId" value="${project.id}" />
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
														<%-- If project types don't exist default value is 0--%>
														<td colspan="2">
														<c:choose>
															<c:when test="${!empty projectTypes}">
																<c:set var="typeId" value="0" scope="page" />
																<c:if test="${projectTypeId > 0}">
																	<c:set var="typeId" value="${projectTypeId}" />
																</c:if>
																<c:if test="${!empty project.projectType}">
																	<c:set var="typeId" value="${project.projectType.id}"
																		scope="page" />
																</c:if>
																<ww:select headerKey="0" headerValue="(undefined)"
																	name="project.projectType.id" list="#attr.projectTypes"
																	listKey="id" listValue="name" value="${typeId}" />
															</c:when>
															<c:otherwise>
																(undefined)
															</c:otherwise>
														</c:choose>
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
														<td>Baseline load</td>
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
														<aef:datepicker id="start_date" name="startDate" format="%{getText('webwork.shortDateTime.format')}" value="%{#start}" />
														</td>
													</tr>
													<tr>
														<td>End date</td>
														<td>*</td>
														<td colspan="2">
														<aef:datepicker id="end_date" name="endDate" format="%{getText('webwork.shortDateTime.format')}" value="%{#end}" />
														</td>
													</tr>
													<tr>
														<td>Assigned Users</td>
														<td></td>
														<td colspan="2">
														<div>
									                        <a id="userChooserLink-editProject" href="#" class="assigneeLink">
									                            <img src="static/img/users.png"/>
									                            <span id="userListContainer-editProject">
									                            <c:set var="count" value="0" />
											                    <c:set var="listLength" value="${fn:length(project.assignments)}"/>
											                    <c:choose>
											                        <c:when test="${listLength > 0}">
											                            <c:forEach items="${project.assignments}" var="ass">
											                                <input type="hidden" name="selectedUserIds" value="${ass.user.id}"/>
											                                <input type="hidden" name="assignments['${ass.user.id}'].user.id" value="${ass.user.id}"/>
											                                <input type="hidden" name="assignments['${ass.user.id}'].deltaOverhead" value="${ass.deltaOverhead}"/>
											                                <c:set var="count" value="${count + 1}" />
											                                <c:out value="${ass.user.initials}" /><c:if test="${count != listLength}">, </c:if>
											                            </c:forEach>    
											                        </c:when>
											                        <c:otherwise>
											                            (none)
											                        </c:otherwise>
											                    </c:choose>
									                            </span>
									                        </a>
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
																<td><ww:submit value="Create"
															    disabled="disabled" cssClass="undisableMe"/></td>
															</c:when>
															<c:otherwise>
																<td><ww:submit value="Save" id="saveButton" /></td>
																<td class="deleteButton"><ww:submit
																	action="deleteProject"
																	disabled="disabled" cssClass="undisableMe"
																	value="Delete" /></td>
															</c:otherwise>
														</c:choose>
													</tr>
												</table>
											</ww:form>
										</div>
									</div>
									
								</div>
							</td>
						</tr>
					</tbody>
				</table>
		<table>
			<tr>
				<td>

								<c:if test="${projectId != 0}">
									<div class="subItems" id="subItems_editProjectThemesList">
										<div class="subItemHeader">
										    <table cellspacing="0" cellpadding="0">
					            			    <tr>
					            			    	<td class="header">Themes</td>
												<td class="icons">
							                        <table cellspacing="0" cellpadding="0">
							                            <tr>
							                            <td>
							                          <a href="#" title="Attach theme" onclick="return false;" class="attachLink" id="addProjectBusinessTheme" />
							                            </td>
							                            </tr>
							                            </table>      
							                        </td>
												</tr>
												
											</table>
										</div>
										<div class="subItemContent">
										<div class="validateWrapper validateEmpty">
										<ww:form action="storeBacklogThemebinding" method="post">
										<ww:hidden name="backlogId" value="${project.id}"/>
										<input type="hidden" name="contextViewName" value="project" />
										
										<c:choose>
										<c:when test="${!empty project.businessThemeBindings}">
										<div class="businessThemeTableWrapper">
										<display:table htmlId="businessThemeTable" class="businessThemeTable listTable" name="project.businessThemeBindings" id="row" requestURI="editIteration.action">

											<display:column sortable="true" title="Name" sortProperty="businessTheme.name"
											     class="businessThemeNameColumn">
												<span style="display: none;">${row.businessTheme.id}</span>
												<a style="cursor: pointer; color: #0055AA;" class="table_edit_edit">
													<c:out value="${row.businessTheme.name}"/>
												</a>												
											</display:column>
											
											<display:column sortable="true" sortProperty="boundEffort" title="Planned spending">
												<c:choose>
													<c:when test="${row.relativeBinding == true}">
														<span style="display:none;">${row.percentage}%</span>
														<c:out value="${row.boundEffort}"/>
														(<c:out value="${row.percentage}"/>%)
													</c:when>
													<c:otherwise><c:out value="${row.fixedSize}"/></c:otherwise>
												</c:choose>
											</display:column>
											<display:column sortable="false" title="Actions">
												<span class="uniqueId" style="display: none;">${row.id}</span>
												<img style="cursor: pointer;" class="table_edit_edit" src="static/img/edit.png" title="Edit" />
												<img style="cursor: pointer;" class="table_edit_delete" src="static/img/delete_18.png" title="Remove from project" />
											</display:column>
										</display:table>
										</div>
											</c:when>
											<c:otherwise>
												<table id="businessThemeTable" style="display:none;" class="listTable">
													<tr><th class="sortable" class="businessThemeNameColumn">Name</th><th class="sortable">Planned spending</th><th>Actions</th></tr>
												</table>
											</c:otherwise>
											</c:choose>
											<div id="backlogThemeSave" style="display: none;">
											<ww:label id="themeLabel" value="Planned spending may be entered as time (e.g. 2h 30min) or a percentage
                                                (e.g. 40%)." cssStyle="margin: 3px; padding: 3px; display: block; border: 1px solid #ccc;"/>
											<input id="backlogThemeSave" style="margin-left: 2px;" type="submit" value="Save" />
											
											</div>
											</ww:form>	
											</div>			
											</div>
											<c:if test="${!empty iterationThemes}">
											<div class="businessThemeTableWrapper">
											<h4>Iteration themes</h4>
												<display:table htmlId="businessThemeTableIterations" class="businessThemeTable listTable" name="iterationThemes" id="row" requestURI="editProject.action">
						
													<display:column sortable="true" title="Name" sortProperty="businessTheme.name"
													       class="businessThemeNameColumn">														
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
														<ww:url id="editLink" action="editIteration" includeParams="none">
															<ww:param name="iterationId" value="${row.backlog.id}" />
														</ww:url>
														<ww:a href="%{editLink}&contextViewName=editProduct&contextObjectId=${project.id}">						
															<c:out value="${row.backlog.name}"/>
														</ww:a>
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
						<div class="subItems" id="subItems_editProjectIterations">
							<div class="subItemHeader">
								<table cellpadding="0" cellspacing="0">
									<tr>
					   					<td class="header">Iterations</td>
					   					<td class="icons">
					   					<table cellpadding="0" cellspacing="0">
					   					<tr>
					   					<td>
					   						<ww:url id="createLink" action="ajaxCreateIteration" includeParams="none" >
						  						<ww:param name="projectId" value="${project.id}" />
					   						</ww:url>
					   						<ww:a
												href="%{createLink}" cssClass="openCreateDialog openIterationDialog"
												onclick="return false;" title="Create a new iteration">
                                            </ww:a>
                                        </td>
                                        </tr>
                                        </table>
					   					</td>
									</tr>
								</table>
							</div>
							<c:if test="${!empty project.iterations}">
								<div class="subItemContent">
										<display:table class="listTable" name="project.iterations"
											id="row" requestURI="editProject.action">
											
											<display:column sortable="true" sortProperty="name" title="Name">
												<div style="overflow:hidden; width: 170px;">																								
													<ww:url id="editLink" action="editIteration"
													includeParams="none">
														<ww:param name="iterationId" value="${row.id}" />
													</ww:url>
													<ww:a href="%{editLink}&contextViewName=editProject&contextObjectId=${project.id}">
														${aef:html(row.name)}
													</ww:a>												
												</div>
												<div id="iterationTabContainer-${row.id}" class="tabContainer" style="overflow:visible; white-space: nowrap; width: 0px;"></div>
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
												<img src="static/img/edit.png" alt="Edit" title="Edit" style="cursor: pointer;" onclick="handleTabEvent('iterationTabContainer-${row.id}', 'iteration', ${row.id}, 1);" />
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
		<div class="subItems" id="subItems_editProjectBacklogItems">
		<div class="subItemHeader">
			<table cellspacing="0" cellpadding="0">
                    <tr>
                        <td class="header">
                        Stories
                        </td>
                        <td class="icons">
                        <table cellspacing="0" cellpadding="0">
                            <tr>
                            <td>
			                        <ww:url
			                    id="createBacklogItemLink" action="ajaxCreateStory"
			                    includeParams="none">
			                    <ww:param name="backlogId" value="${project.id}" />
			                </ww:url> <ww:a cssClass="openCreateDialog openBacklogItemDialog"
			                    href="%{createBacklogItemLink}" onclick="return false;"
			                    title="Create a new story">
			                    </ww:a>
			                    </td>
			                    </tr>
			                    </table>
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
						<c:if test="${(!empty project.backlogItems) && (projectBurndown || (empty project.iterations))}">
							<p>
								<img src="drawProjectChart.action?projectId=${project.id}" id="bigChart"
								   width="780" height="600" />
							</p>
						</c:if>
					</c:if>
				</td>
			</tr>
		</table>

<%-- Hour reporting here - Remember to expel David H. --%>

<aef:hourReporting id="hourReport"></aef:hourReporting>
<c:if test="${hourReport == 'true' && projectId != 0}">
	<c:set var="myAction" value="editProject" scope="session" />
	<%@ include file="./inc/_hourEntryList.jsp"%>
</c:if> <%-- Hour reporting on --%>

<%@ include file="./inc/_footer.jsp"%>