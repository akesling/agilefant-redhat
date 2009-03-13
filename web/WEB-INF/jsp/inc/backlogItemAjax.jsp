<%@ include file="_taglibs.jsp"%>

<aef:hourReporting id="hourReport" />
<aef:currentUser />
<aef:userList />
<aef:teamList />
<aef:iterationGoalList id="iterationGoals" backlogId="${backlogId}" />
<aef:productList />

<div class="ajaxWindowTabsDiv">
<ul class="ajaxWindowTabs">
	<li><a href="#backlogItemEditTab-${backlogItemId}-${bliListContext}"><span><img src="static/img/edit.png" alt="Edit" /> Edit</span></a></li>
	<li><a href="#backlogItemProgressTab-${backlogItemId}-${bliListContext}"><span><img src="static/img/progress.png" alt="Progress" /> Progress</span></a></li>
<c:if test="${hourReport == true}">
	<li><a href="#backlogItemSpentEffTab-${backlogItemId}-${bliListContext}"><span><img src="static/img/timesheets.png" alt="Spent Effort" /> Spent Effort</span></a></li>
</c:if>
</ul>

<div id="backlogItemEditTab-${backlogItemId}-${bliListContext}" class="backlogItemNaviTab">

<script type="text/javascript">
$(document).ready(function() {
    $('#userChooserLink-${backlogItemId}-${bliListContext}').userChooser({
        backlogItemId: ${backlogItemId},
        backlogIdField: '#backlogSelect-${backlogItemId}-${bliListContext}',
        userListContainer: '#userListContainer-${backlogItemId}-${bliListContext}'
    }); 
    $('#themeChooserLink-${backlogItemId}-${bliListContext}').themeChooser({
        backlogId: '#backlogSelect-${backlogItemId}-${bliListContext}',
        themeListContainer: '#themeListContainer-${backlogItemId}-${bliListContext}'
    });
});

</script>

<table>
<tbody>
	<tr>
	<td>
	<div class="subItems" style="margin-top: 0px; width: 715px;"> 
	<div class="validateWrapper validateBacklogItem">
	<ww:form action="ajaxStoreBacklogItem" method="post">
		<ww:hidden name="backlogItemId" value="${backlogItem.id}" />	
		<ww:hidden name="effortLeft" value="${backlogItem.effortLeft}" />				
	
		<table class="formTable">	
			<tr>
				<td><ww:text name="general.uniqueId"/></td>
				<td></td>
				<td><aef:quickReference item="${backlogItem}" /></td>
			</tr>	
			<tr>						
				<td>Name</td>											
				<td>*</td>
				<td colspan="2"><ww:textfield size="60" name="backlogItem.name" /></td>
			</tr>
			
			<tr>
				<td>Description</td>
				<td></td>
				<td colspan="2">
					<ww:textarea cols="70" rows="10" cssClass="useWysiwyg" id="backlogItemDescription" 
					name="backlogItem.description" value="${aef:nl2br(backlogItem.description)}" /></td>
			</tr>
			<c:choose>
			<c:when test="${backlogItem.creator != null}">
			<tr>
				<td>Created by</td>
				<td></td>
				<td colspan="2">
	
				<div>
				<c:out value="${backlogItem.creator.fullName}" />
				<c:choose>
				<c:when test="${backlogItem.createdDate != null}">
				 on <c:out value="${aef:calendarAsString(backlogItem.createdDate)}"/>
				</c:when>
				</c:choose>
				</div>
				</td>
			</tr>
			</c:when>
			</c:choose>
			<c:choose>
				<c:when test="${backlogItem.originalEstimate == null}">
					<tr>
						<td>Original estimate</td>
						<td></td>
						<td colspan="2">
						<c:choose>
							<c:when test="${backlogItem.state.name != 'DONE'}">
								<ww:textfield size="10"
								name="backlogItem.originalEstimate"
								id="originalEstimateField_${backlogItem.id}-${bliListContext}" />
							</c:when>
							<c:otherwise>
								<ww:textfield size="10"
								name="backlogItem.originalEstimate"
								disabled="true"
								id="originalEstimateField_${backlogItem.id}-${bliListContext}" />
							</c:otherwise>
						</c:choose>
						<ww:label value="%{getText('webwork.estimateExample')}" /></td>
					</tr>
				</c:when>
				<c:otherwise>
				    <script type="text/javascript">
				    
                    </script>
					<tr>
						<td>Original estimate</td>
						<td></td>
						<td colspan="2"><%--<ww:label value="${backlogItem.originalEstimate}" />--%>
						    <ww:textfield name="backlogItem.originalEstimate"
						                  value="${backlogItem.originalEstimate}"
						                  disabled="true" size="10"/>
						<c:choose>
							<c:when test="${backlogItem.state.name == 'DONE'}">
								<span id="resetText_${backlogItem.id}-${bliListContext}" style="color: #666;">(reset)</span>
								<span id="resetLink_${backlogItem.id}-${bliListContext}" style="display: none;">
							</c:when>
							<c:otherwise>
							<span id="resetText_${backlogItem.id}-${bliListContext}" style="color: #666; display: none;">(reset)</span>
								<span id="resetLink_${backlogItem.id}-${bliListContext}">
							</c:otherwise>
						</c:choose>						
						<ww:a href="#" onclick="resetBLIOriginalEstimate(${backlogItem.id}, this); return false;">(reset)</ww:a>
						</span>
												
						</td>
					</tr>
					<tr>
						<td>Effort left</td>
						<td></td>
						<td colspan="2">
						<c:choose>
							<c:when test="${backlogItem.state.name != 'DONE'}">
								<ww:textfield size="10"
								name="backlogItem.effortLeft"
								id="effortLeftField_${backlogItem.id}-${bliListContext}" />
							</c:when>
							<c:otherwise>
								<ww:textfield size="10"
								name="backlogItem.effortLeft"
								disabled="true"
								id="effortLeftField_${backlogItem.id}-${bliListContext}" />
							</c:otherwise>
						</c:choose>
						<ww:label value="%{getText('webwork.estimateExample')}" />
						</td>
					</tr>
				</c:otherwise>
			</c:choose>
	
			<tr>
				<td>State</td>
				<td></td>
				<td colspan="2">
				<c:set var="hasUndoneTasks" value="${undoneTasks}" scope="request" />				
				<script type="text/javascript">
				function change_estimate_enabled(value, itemId, context) {
					var effLeftField = document.getElementById("effortLeftField_" + itemId + "-" + context);
					var origEstField = document.getElementById("originalEstimateField_" + itemId + "-" + context);
					var resetLink = document.getElementById("resetLink_" + itemId + "-" + context);
					var resetText = document.getElementById("resetText_" + itemId + "-" + context);
					if (value == 'DONE') {
						if (effLeftField != null) {
							effLeftField.disabled = true;
						}
						if (origEstField != null) {
							origEstField.disabled = true;
						}
						if (resetLink != null) {
							resetLink.style.display = "none";
						}
						if (resetText != null) {
							resetText.style.display = "";
						}
					}
					else {
						if (effLeftField != null) {
							effLeftField.disabled = false;
						}
						if (origEstField != null) {
							origEstField.disabled = false;
						}
						if (resetLink != null) {
							resetLink.style.display = "";
						}
						if (resetText != null) {
							resetText.style.display = "none";
						}
					}
				}
				
				<%-- If user changed the item's state to DONE and there are tasks not DONE, ask if they should be set to DONE as well. --%>				
				$(document).ready(function() {					
					$("#stateSelect_${backlogItem.id}-${bliListContext}").change(function() {
						change_estimate_enabled($(this).val(), ${backlogItem.id}, '${bliListContext}');						
						if ($(this).val() == 'DONE' && ${hasUndoneTasks}) {
							var prompt = window.confirm("Do you wish to set all the TODOs' states to Done as well?");
							if (prompt) {
								$("#tasksToDone_${backlogItem.id}-${bliListContext}").val('true');
							}						
						}
					});

					
				});
				</script>
				<%-- Tasks to DONE confirmation script ends. --%>
				<ww:hidden name="tasksToDone" value="${tasksToDone}" id="tasksToDone_${backlogItem.id}-${bliListContext}" />			
				<ww:select name="backlogItem.state"
					id="stateSelect_${backlogItem.id}-${bliListContext}"
					value="backlogItem.state.name"
					list="@fi.hut.soberit.agilefant.model.State@values()" listKey="name"
					listValue="getText('task.state.' + name())"  /></td>
			</tr>
	
			<tr>
				<td>Backlog</td>
				<td></td>
				<td colspan="2">
					<select name="backlogId" id="backlogSelect-${backlogItemId}-${bliListContext}"
					   onchange="getIterationGoals(this.value, '#iterationGoalSelectBLI-${backlogItemId}-${bliListContext}')">
	
					<%-- Generate a drop-down list showing all backlogs in a hierarchical manner --%>
					<option class="inactive" value="">(select backlog)</option>
					<c:forEach items="${productList}" var="product">
						<c:choose>
							<c:when test="${product.id == backlogItem.backlog.id}">
								<option selected="selected" value="${product.id}" class="productOption"
									title="${product.name}">${aef:out(product.name)}</option>
							</c:when>
							<c:otherwise>
								<option value="${product.id}" title="${product.name}" class="productOption">${aef:out(product.name)}</option>
							</c:otherwise>
						</c:choose>
						<c:forEach items="${product.projects}" var="project">
							<c:choose>
								<c:when test="${project.id == backlogItem.backlog.id}">
									<option selected="selected" value="${project.id}" class="projectOption"
										title="${project.name}">${aef:out(project.name)}</option>
								</c:when>
								<c:otherwise>
									<option value="${project.id}" title="${project.name}"  class="projectOption">${aef:out(project.name)}</option>
								</c:otherwise>
							</c:choose>
							<c:forEach items="${project.iterations}" var="iteration">
								<c:choose>
									<c:when test="${iteration.id == backlogItem.backlog.id}">
										<option selected="selected" value="${iteration.id}" class="iterationOption"
											title="${iteration.name}">${aef:out(iteration.name)}</option>
									</c:when>
									<c:otherwise>
										<option value="${iteration.id}" title="${iteration.name}"  class="iterationOption">${aef:out(iteration.name)}</option>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</c:forEach>
					</c:forEach>
				</select></td>
			</tr>
			<tr>
				<td>Iteration goal</td>
				<td></td>
				<%-- If iteration goals doesn't exist default value is 0--%>
                <td colspan="2">
                    <c:set var="goalId" value="0" scope="page" />
                    <c:if test="${iterationGoalId > 0}">
                        <c:set var="goalId" value="${iterationGoalId}" />
                    </c:if>
                    <c:if test="${!empty backlogItem.iterationGoal}">
                        <c:set var="goalId" value="${backlogItem.iterationGoal.id}"
                            scope="page" />
                    </c:if>
                    <c:choose>
                    	<c:when test="${!empty iterationGoals}">
                    <ww:select headerKey="0" headerValue="(none)"
                        id="iterationGoalSelectBLI-${backlogItemId}-${bliListContext}"
                        name="iterationGoalId" list="#attr.iterationGoals"
                        listKey="id" listValue="name" value="${goalId}" />
                        <span style="display: none;">(none)</span>
                     </c:when>
                     <c:otherwise>
                     	<select id="iterationGoalSelectBLI-${backlogItemId}-${bliListContext}" name="iterationGoalId" style="display: none;"></select>
                     	<span>(none)</span>
                     </c:otherwise>
                    </c:choose>
                </td>
			</tr>
			<tr>
				<td>Priority</td>
				<td></td>
				<td colspan="2"><ww:select name="backlogItem.priority"
					value="backlogItem.priority.name"
					list="#@java.util.LinkedHashMap@{'UNDEFINED':'undefined', 'BLOCKER':'+++++', 'CRITICAL':'++++', 'MAJOR':'+++', 'MINOR':'++', 'TRIVIAL':'+'}" /></td>
				<%--
			If you change something about priorities, remember to update conf/classes/messages.properties as well!
			--%>
			</tr>
			
			<tr>
				<td>Responsibles</td>
				<td></td>
				<td colspan="2">
	
				<div>
				<a id="userChooserLink-${backlogItemId}-${bliListContext}" href="#" class="assigneeLink">
				    <img src="static/img/users.png"/>
                    <span id="userListContainer-${backlogItemId}-${bliListContext}">
                    <c:set var="count" value="0" />
                    <c:set var="listLength" value="${fn:length(backlogItem.responsibles)}"/>
                    <c:choose>
                        <c:when test="${listLength > 0}">
                            <c:forEach items="${backlogItem.responsibles}" var="resp">
                                <input type="hidden" name="userIds[${resp.id}]" value="${resp.id}"/>
                                <c:set var="count" value="${count + 1}" />
                                <c:out value="${resp.initials}" /><c:if test="${count != listLength}">, </c:if>
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
				<td>Themes</td>
				<td></td>
				<td colspan="2">
	
				<div>
				<a id="themeChooserLink-${backlogItemId}-${bliListContext}" href="#" class="assigneeLink">
				    <img src="static/img/theme.png"/>
                    <span id="themeListContainer-${backlogItemId}-${bliListContext}">
                    <c:set var="count" value="0" />
                    <c:set var="listLength" value="${fn:length(backlogItem.businessThemes)}"/>
                    <c:choose>
                        <c:when test="${listLength > 0}">
                            <c:forEach items="${backlogItem.businessThemes}" var="bt">
                                <input type="hidden" name="themeIds" value="${bt.id}" />
			            	   <c:choose>
			            	       <c:when test="${bt.global}">
			            	           <span class="businessTheme globalThemeColors" style="float: none;"><c:out value="${bt.name}"/></span>
			            	       </c:when>
			            	       <c:otherwise>
			            	           <span class="businessTheme" style="float: none;"><c:out value="${bt.name}"/></span>   
			            	       </c:otherwise>
			            	   </c:choose>                                
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
				<td></td>
				<td></td>
				<td><ww:submit value="Save" id="saveButton" />
				<ww:submit name="SaveClose" value="Save & Close" id="saveClose"  /></td>
				<td class="deleteButton">
				<ww:submit value="Delete" action="deleteBacklogItem" />
				<ww:reset value="Cancel"/>	
				</td>
			</tr>
		</table>
	
	</ww:form>
</div>
</div>
</div>
</div>

</td>
</tr>
</tbody>
</table>
	
</div>
<!-- edit tab ends -->

<!-- tasks tab begins -->
<div id="backlogItemProgressTab-${backlogItemId}-${bliListContext}" class="backlogItemNaviTab">

<script type="text/javascript">
	$(document).ready( function() {
		// Task ranking
		$('.moveUp').click(function() {
			var me = $(this);
			$.get(me.attr('href'), null, function() {me.moveup();});
			return false;
		});
		$('.moveDown').click(function() {
			var me = $(this);
			$.get(me.attr('href'), null, function() {me.movedown();});
			return false;
		});
		$('.moveTop').click(function() {
			var me = $(this);
			$.get(me.attr('href'), null, function() {me.movetop();});
			return false;
		});
		$('.moveBottom').click(function() {
			var me = $(this);
			$.get(me.attr('href'), null, function() {me.movebottom();});
			return false;
		});				
	});

	function change_effort_enabled(value, bliId, context) {
		if (value == "DONE") {
			document.getElementById("effortBli_" + bliId + "-" + context).disabled = true;							
		}
		else {
			document.getElementById("effortBli_" + bliId + "-" + context).disabled = false;
		}
	}
	
	<%-- If user changed the item's state to DONE and there are tasks not DONE, ask if they should be set to DONE as well. --%>
		$(document).ready(function() {
			$("#stateSelectProgress_${backlogItem.id}-${bliListContext}").change(function() {
				change_effort_enabled($(this).val(), ${backlogItem.id}, '${bliListContext}');
				var tasksDone = true;
				$(".taskStateSelect_${backlogItem.id}-${bliListContext}").each(function() {
					if ($(this).val() != 'DONE') {
						tasksDone = false;
					}
				});
				if ($(this).val() == 'DONE' && !tasksDone) {
					var prompt = window.confirm("Do you wish to set all the tasks' states to Done as well?");
					if (prompt) {
						$("#todoTable-${backlogItemId}-${bliListContext}").find('select[name^=taskStates]').val('DONE');
					}					
				}
			});
			$('#todoTable-${backlogItemId}-${bliListContext}').inlineTableEdit({
						  add: '#addTodo-${backlogItemId}-${bliListContext}', 
						  useId: true,
						  deleteaction: 'deleteTask.action',
						  submitParam: 'taskId',
						  fields: {
						  	taskNames: {cell: 0, type: 'text', size: 50},
						  	taskStates: {cell: 1,type: 'select', data: {'NOT_STARTED': 'Not started', 'STARTED': 'Started', 'PENDING': 'Pending', 'BLOCKED': 'Blocked', 'IMPLEMENTED': 'Implemented', 'DONE': 'Done'}},											  	
						  	reset: {cell: 2, type: 'reset'}
						  }
			});
		});
	<%-- Tasks to DONE confirmation script ends. --%>
</script>

<div class="validateWrapper validateBLIProgressTab">
<ww:form action="quickStoreTaskList" validate="false" method="post">

<table>
<tbody>
	<tr>
	<td>
	<div class="subItems" style="margin-top: 0px; width: 725px;">

	<table class="progressTable">
	<tr>
	<td colspan="2">
	<table>
	<tbody>
		<tr>
			<td>State</td>
			<td>
				<ww:select name="state"
					id="stateSelectProgress_${backlogItem.id}-${bliListContext}" value="#attr.backlogItem.state.name"
					list="@fi.hut.soberit.agilefant.model.State@values()" listKey="name"
					listValue="getText('backlogItem.state.' + name())" />
            </td>
            <td>Effort left</td>
            <td>
				<ww:hidden name="backlogItemId" value="${backlogItem.id}" />
				<ww:hidden name="contextViewName" value="${contextViewName}" />
				<ww:hidden name="contextObjectId" value="${contextObjectId}" />
				<c:choose>
					<c:when test="${backlogItem.state.name != 'DONE'}">
						<ww:textfield size="5" name="effortLeft"
							value="${backlogItem.effortLeft}" id="effortBli_${backlogItem.id}-${bliListContext}" />	
					</c:when>
					<c:otherwise>
						<ww:textfield size="5" name="effortLeft"
							value="${backlogItem.effortLeft}" id="effortBli_${backlogItem.id}-${bliListContext}"
							disabled="true" />
					</c:otherwise>
				</c:choose>	
			</td>
			
		</tr>
		<c:if test="${hourReport}">
		<tr>
			<td>Log effort for <c:out value="${currentUser.initials}"/></td>
			<td>
			 <ww:textfield size="5" name="spentEffort" id="effortSpent_${backlogItem.id}-${bliListContext}"/>
			</td>
			<td>Comment:</td>
            <td>
             <ww:textfield size="30" name="spentEffortComment" id="effortSpentComment_${backlogItem.id}-${bliListContext}"/> 
	        </td>
	    </c:if>
	</td>
	</tr>
	</tbody>
	</table>
	
	</td>
	</tr>
	
	<tr>
	<td colspan="2">
	<!-- task table begins -->
	<table>
		<tr>
			<td>
				<div class="subItems" style="margin-top: 0px; width: 710px;">
				<a id="addTodo-${backlogItemId}-${bliListContext}" href="#">Add new TODO &raquo;</a>				
				<c:choose>
				<c:when test="${!empty backlogItem.tasks}">
					<div class="subItemContent">										
					<p>
					<display:table htmlId="todoTable-${backlogItemId}-${bliListContext}" class="listTable" name="backlogItem.tasks"
						id="row">
						
						<display:column sortable="false" title="Name"
							class="shortNameColumn">
							<ww:textfield size="50" name="taskNames[${row.id}]" value="${row.name}" />												
						</display:column>
														
						<display:column sortable="false" title="State">											
							<ww:select cssClass="taskStateSelect_${backlogItem.id}-${bliListContext}"
								name="taskStates[${row.id}]" value="#attr.row.state.name"
								list="@fi.hut.soberit.agilefant.model.State@values()" listKey="name"
								listValue="getText('task.state.' + name())" id="taskStateSelect_${row.id}-${bliListContext}"/>														
						</display:column>
											
						<display:column sortable="false" title="Actions" style="width:102px;">
							<ww:url id="moveTaskTopLink" action="moveTaskTop" includeParams="none">
								<ww:param name="taskId" value="${row.id}" />
							</ww:url>
							<ww:a cssClass="moveTop" href="%{moveTaskTopLink}">
								<img src="static/img/arrow_top.png" alt="Send to top"
									title="Send to top" />
							</ww:a>
	
							<ww:url id="moveTaskUpLink" action="moveTaskUp" includeParams="none">
								<ww:param name="taskId" value="${row.id}" />
							</ww:url>
							<ww:a cssClass="moveUp" href="%{moveTaskUpLink}">
								<img src="static/img/arrow_up.png" alt="Move up" title="Move up" />
							</ww:a>
	
							<ww:url id="moveTaskDownLink" action="moveTaskDown" includeParams="none">
								<ww:param name="taskId" value="${row.id}" />
							</ww:url>
							<ww:a cssClass="moveDown" href="%{moveTaskDownLink}">
								<img src="static/img/arrow_down.png" alt="Move down"
									title="Move down" />
							</ww:a>
	
							<ww:url id="moveTaskBottomLink" action="moveTaskBottom" includeParams="none">
								<ww:param name="taskId" value="${row.id}" />
							</ww:url>
							<ww:a cssClass="moveBottom" href="%{moveTaskBottomLink}">
								<img src="static/img/arrow_bottom.png" alt="Send to bottom"
									title="Send to bottom" />
							</ww:a>
	                        <span style="display:none;" class="uniqueId">${row.id}</span>
						    <img src="static/img/delete_18.png" alt="Delete" title="Delete" class="table_edit_delete" style="cursor: pointer;"/>
						</display:column>
	
					</display:table></p>				
					</div>
				</c:when> 
				<c:otherwise>
				<%-- No tasks: container --%>
					<table id="todoTable-${backlogItemId}-${bliListContext}" style="display: none;" class="listTable"><tr><th>Name</th><th>State</th><th>Actions</th></tr></table>
				</c:otherwise>
				</c:choose>
				</div>
			</td>
		</tr>
	</table>
	<!-- task table ends -->
	</td>
	</tr>
	
	<tr>
	<td>
		<ww:submit value="Save" action="quickStoreTaskList" />
		<ww:submit value="Save & Close" id="saveCloseTaskList" name="SaveClose" />
	</td>
	<td class="deleteButton">
		<ww:reset value="Cancel" onclick="$('#todoTable-${backlogItemId}-${bliListContext}').resetTableEdit();"/>				
	</td>
	</tr>
	
	</table>
			
	</div>
	</td>
	</tr>		
</tbody>
</table>

</ww:form>
</div>
</div>
<!-- Tasks tab ends -->
<c:if test="${hourReport == true}">
<div id="backlogItemSpentEffTab-${backlogItemId}-${bliListContext}" class="backlogItemNaviTab">

<aef:hourEntries id="hourEntries" target="${backlogItem}" />

<script type="text/javascript">
$(document).ready(function() {
	var allUsers = function() {
		var users = jsonDataCache.get("allUsers");
		var ret = {};
		jQuery.each(users,function() {if(this.enabled) {ret[this.id] = this.fullName; } });
		return ret;
	};
	$('#spentEffort-${backlogItemId}-${bliListContext}').inlineTableEdit({
				  submit: '#saveSpentEffort-${backlogItemId}-${bliListContext}',
				  useId: true,
				  deleteaction: 'deleteHourEntry.action',
                  submitParam: 'hourEntryId',
				  fields: {
				  	efforts: {cell: 2, type: 'text'},
				  	dates: {cell: 0, type: 'date'},
				  	userIdss: {cell: 1, type: 'select', data: allUsers},
				  	descriptions: {cell: 3, type: 'text'},
				  	reset: {cell: 4, type: 'reset'}
				  	}
	});

});
</script>								
				
<div class="subItemContent">
<div class="subItems validateWrapper validateEmpty" style="margin-top: 0; margin-left: 3px; width: 710px;">
	<ww:url id="createLink" action="ajaxCreateHourEntry" includeParams="none">
		<ww:param name="backlogItemId" value="${backlogItemId}" />
	</ww:url>
	<ww:a cssClass="openCreateDialog openHourEntryDialog" title="Log effort"
		href="%{createLink}" onclick="return false;">
		Log effort &raquo;
	</ww:a>
	<c:if test="${!empty hourEntries}">		
	<ww:form action="updateMultipleHourEntries.action" method="post">		
	<display:table name="${hourEntries}" htmlId="spentEffort-${backlogItemId}-${bliListContext}" id="row" defaultsort="1" defaultorder="descending" requestURI="${currentAction}.action"
	   style="width: 700px;">
						
		<display:column sortable="false" title="Date" style="white-space:nowrap; width: 140px;">
			<ww:date name="#attr.row.date" format="yyyy-MM-dd HH:mm" />
		</display:column>
						
		<display:column sortable="false" title="User">
			<span style="display: none;">${row.user.id}</span>
			${aef:html(row.user.fullName)}
		</display:column>
						
		<display:column sortable="false" title="Spent effort" sortProperty="timeSpent">
			${aef:html(row.timeSpent)}
		</display:column>
						
		<display:column sortable="false" title="Comment">
			<c:out value="${row.description}"/>
		</display:column>
						
		<display:column sortable="false" title="Action">	
			<span class="uniqueId" style="display: none;">${row.id}</span>
			<img src="static/img/edit.png" class="table_edit_edit" alt="Edit" title="Edit" style="cursor: pointer;" />
            <img src="static/img/delete_18.png" alt="Delete" title="Delete" class="table_edit_delete" style="cursor: pointer;"/>								
		</display:column>
	</display:table>
	<input type="submit" value="Save" style="display: none;" id="saveSpentEffort-${backlogItemId}-${bliListContext}" />
	</ww:form>
	</c:if> <%-- No entries --%>				
	</div>
	</div>					
</div>
</c:if>

</div>