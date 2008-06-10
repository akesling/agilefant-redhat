<%@ include file="./_taglibs.jsp"%>
<aef:currentUser />
<aef:enabledUserList/>
<aef:userEffortSum user="${currentUser}" timeInterval="Today" id="todayEffortSum" />
<aef:userEffortSum user="${currentUser}" timeInterval="Yesterday" id="yesterdayEffortSum" />
<aef:userEffortSum user="${currentUser}" timeInterval="This week" id="weekEffortSum" />
<aef:userEffortSum user="${currentUser}" timeInterval="This month" id="monthEffortSum" />
<script type="text/javascript">
function updatePastEffort(immediate) {
	var data = new Object();
	if(immediate) { 
		data["startDate"] = $("#effStartDate").val();
		data["endDate"] = $("#effEndDate").val();
		if(data["startDate"].length < 3) {
			alert("From date -field is empty!"); 
			return;
		}
		if(data["endDate"].length < 3) {
			alert("To date -field is empty!");
			return;
		}
		$("#hourDisplay").load("getHourSum.action",data);
	} else {
		if($("#pastEffortInterval").is(":checked")) {
			$("#pastEffortChoosers").show();
		} else {
			$("#pastEffortChoosers").hide();
		}
	}
}

function showOldEffort() {
	if($("#showPastEffort").is(":checked")) {
		$("#pastEffortShower").show();
	} else {
		$("#pastEffortShower").hide();
	}
}


function saveAndClose() {
	if(!checkEstimateFormat('hourEntry.timeSpent')) {
		return false;
	}
	var data = $("#hourModalForm").serializeArray();
	var target = $("#hourModalForm").attr("action");
	jQuery.post(target,data);
	jQuery("#closeModal").click();
	return false;
}
</script>
<div target="AJAX-MODAL" style="width: 690px; height: 20px; padding: 5px; border-bottom: 1px solid black; background: #ccc;">
	<span style="float: left;">
	<c:choose>
		<c:when test="${hourEntryId == 0}">
			<b>Log effort</b>
		</c:when>
		<c:otherwise>
			<b>Edit log entry</b>
		</c:otherwise>
	</c:choose>
	</span>
	<span style="float: right;" >
		<img id="closeModal" class="jqmClose" src="static/img/delete.png" alt="Close dialog" style="cursor: pointer;">
	</span>
</div>
<div style="padding: 12px;">
<ww:form id="hourModalForm" action="storeHourEntry" onsubmit="javascript: return checkEstimateFormat('hourEntry.timeSpent');">
	<ww:hidden name="hourEntryId" />
	<ww:hidden name="backlogItemId" />
	<ww:hidden name="backlogId" />
	<ww:hidden name="iterationId" />
	
	<table class="formTable">
	<tr>
		<td colspan="4">
		
			<input type="checkbox" name="showPastEffort" id="showPastEffort" onclick="javascript:showOldEffort();" /> Show Past Effort
			<div id="pastEffortShower" style="display: none; border: 1px solid #A0A0A0;">
			
				<table>	
				<tr>
				<td colspan="2">
					<table>
						<tr><td>Today:</td><td>${todayEffortSum}</td><td style="width: 50px;"></td>
						<td>This week:</td><td>${weekEffortSum}</td>
						
						</tr><tr>
						<td>Yesterday:</td><td>${yesterdayEffortSum}</td>
						<td></td>
						<td>This month:</td><td>${monthEffortSum}</td></tr>
					</table>
				</td>
				</tr>
				<tr>
				<td>
			 
				<input type="checkbox" name="pastEffortInterval" id="pastEffortInterval" onclick="javascript:updatePastEffort();" /> Custom interval
				<div id="pastEffortChoosers" style="display: none;">
					<table>
					<!-- Jarnon requ: T�m� rivi pois
					<tr>
					<td colspan="4">Specify the time interval:</td>
					</tr>
					-->
					<tr>
						<td>
					<%--
					<ww:datepicker size="15" showstime="false"
                       format="%{getText('webwork.datepicker.format')}" id="effStartDate" name="effStartDate" />
					--%>
					<aef:datepicker id="effStartDate" name="effStartDate" format="%{getText('webwork.shortDateTime.format')}" value="" />	
						</td>
						<td style="width: 30px; text-align: center;"> - </td>
						<td>
               		<%--
               		<ww:datepicker size="15" showstime="false"
                       format="%{getText('webwork.datepicker.format')}" id="effEndDate" name="effEndDate" />
					--%>
					<aef:datepicker id="effEndDate" name="effEndDate" format="%{getText('webwork.shortDateTime.format')}" value="" />
						</td>
						<td>
                	<input type="button" value="Update" onclick="javascript:updatePastEffort(true);"/>     
						</td>
						<td></td>
						<td><div id="hourDisplay"></div></td>
					</tr>
					<!-- Jarnon requ: Rivi pois
					<tr>
						<td>Reported hours</td>
						<td><div id="hourDisplay"></div></td>
						<td></td>
						<td></td>
					</tr>
					-->
					</table>
				</div>
				
			</td>
			</tr>
			
			</table>
		
			</div>

				<br />
				<br />

		</td>
			
	</tr>
	<tr>
			<td>Effort spent</td>
			<td></td>
			<td colspan="2">
					<ww:textfield name="hourEntry.timeSpent" />(e.g. "2h 30min" or "2.5")
			</td>
		</tr>
		</tr>
		<tr>
			<td>When</td>
			<td></td>
			<td>
						<ww:date name="%{hourEntry.date}" id="date" format="%{getText('webwork.shortDateTime.format')}" />
						<%--
						<ww:datepicker value="%{#date}" size="15"
                                        showstime="true"
                                        format="%{getText('webwork.datepicker.format')}"
                                        name="date" />
                       --%>
                       <aef:datepicker id="he_date" name="date" format="%{getText('webwork.shortDateTime.format')}" value="%{#date}" />                
        	</td>
		</tr>

		<tr>
			
			<td>By whom</td>
			<td></td>
			<td colspan="2">
			<c:choose>
			<c:when test="${hourEntryId == 0}">
				<div id="assigneesLink"><a href="javascript:toggleDiv('assigneeSelect2');"><img src="static/img/users.png" />
					<c:choose>
						<c:when test="${aef:listContains(target.responsibles, currentUser)}">
							<%-- old code, might be needed
							<c:set var="count" value="0" scope="page" />
							<c:set var="comma" value="," scope="page" />
							<c:forEach items="${target.responsibles}" var="responsible">
								<c:set var="unassigned" value="0" scope="page" />
									<c:if test="${count == listSize - 1}" >
										<c:set var="comma" value="" scope="page" />
									</c:if>
									<c:if test="${!empty target.project}" >
										<c:set var="unassigned" value="1" scope="page" />
										<c:forEach items="${target.project.responsibles}" var="projectResponsible">
											<c:if test="${responsible.id == projectResponsible.id}" >
												<c:set var="unassigned" value="0" scope="page" />
											</c:if>
										</c:forEach>
									</c:if>
									<c:choose>
										<c:when test="${unassigned == 1}">
											<span><c:out value="${responsible.initials}" /></span><c:out value="${comma}" />
										</c:when>
										<c:otherwise>
											<c:out value="${responsible.initials}" /><c:out value="${comma}" />
										</c:otherwise>
									</c:choose>
								<c:set var="count" value="${count + 1}" scope="page" />
							</c:forEach>
							--%>
							<c:out value="${currentUser.initials}" />
						</c:when>
						<c:otherwise>
							<c:out value="none" />
						</c:otherwise>
					</c:choose>
				</a>
                </div>
  	            <div id="assigneeSelect2" class="userSelector" style="display: none;">           
                   	<div id="userselect2" class="userSelector">
                   		<div class="left">
                   			<c:set var="listSize" value="${fn:length(target.responsibles)}" scope="page" />
                   			<c:if test="${listSize > 0}">
                   				<label>Responsible</label>
                   				<ul class="users_0" />
                   			</c:if>
                   			<c:if test="${!aef:isProduct(target.backlog)}">
								<label>Assigned to project</label>
                   				<ul class="users_1" />
                   			</c:if>
                   			<c:if test="${listSize > 0 || !aef:isProduct(target.backlog)}">
 	              				<label>Other users</label>
    	               		</c:if>
               				<ul class="users_2" />
                   		</div>
                       	<div class="right">
                           	<label>Teams</label>
                           	<ul class="groups" />
                   		</div>
                   		<script type="text/javascript">
                       		$(document).ready( function() {
                           		<aef:teamList />
                           		<aef:enabledUserList />
                           		<aef:currentUser />
                           		<ww:set name="teamList" value="#attr.teamList" />
                           		<ww:set name="userList" value="#attr.enabledUserList" />
                           		<ww:set name="currentUser" value="#attr.currentUser" />
                           		<c:choose>
									<c:when test="${target.project != null}">
                           				var other = [<aef:userJson items="${aef:listSubstract(aef:listSubstract(userList, target.project.responsibles), target.responsibles)}" />];
                           				var project = [<aef:userJson items="${aef:listSubstract(target.project.responsibles, target.responsibles)}" />];
                           			</c:when>
                           			<c:otherwise>
                           				var other = [<aef:userJson items="${aef:listSubstract(userList, target.responsibles)}" />];
                           				var project = [];
                           			</c:otherwise>
                           		</c:choose>
                           		var preferred = [<aef:userJson items="${target.responsibles}" />];
                           		var teams = [<aef:teamJson items="${teamList}"/>];
                           		<c:choose>
                           		<c:when test="${aef:listContains(target.responsibles, currentUser)}" >
                           		var selected = ["${currentUser.id}"];
                           		</c:when>
                           		<c:otherwise>
                           		var selected = [];
                           		</c:otherwise>
                           		</c:choose>
                           		$('#userselect2').multiuserselect({users: [preferred, project, other], groups: teams, root: $('#userselect2')}).selectusers(selected);
                       		});
                       	</script>
                   	</div>
	            </div>
            </c:when>
			<c:otherwise>
				<select name="userId">
				<c:forEach items="${enabledUserList}" var="user">
					<c:choose>
						<c:when test="${user.id == hourEntry.user.id}">
							<option value="${user.id}" selected="selected"><c:out value="${user.fullName}" /></option>
						</c:when>
						<c:otherwise>
							<option value="${user.id}"><c:out value="${user.fullName}" /></option>
						</c:otherwise>
					</c:choose>
				</c:forEach>
				</select>

			</c:otherwise>
			</c:choose>
			</td>
		</tr>
		<tr>
			<td>Comment</td>
			<td></td>
			<td colspan="2"><ww:textfield size="60" name="hourEntry.description" /></td>
		</tr>
		<tr>
			<td></td>
			<td></td>				
			<td>
			<ww:submit value="Store"/>
			 </td>
		</tr>
	</table>
</ww:form>
</div>



