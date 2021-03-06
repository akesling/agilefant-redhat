<%@ include file="../inc/_taglibs.jsp"%>

<aef:productList/>

<ww:date name="%{new java.util.Date()}" id="start"
        format="%{getText('webwork.shortDateTime.format')}" />
    <ww:date name="%{new java.util.Date()}" id="end"
        format="%{getText('webwork.shortDateTime.format')}" />

<div id="editIterationForm" class="validateWrapper validateNewIteration">
<ww:form method="post" action="storeNewIteration">
	<ww:hidden name="iterationId" value="${iteration.id}" />
	

	<table class="formTable">
		<tr>
			<td>Name</td>
			<td>*</td>
			<td colspan="2"><ww:textfield size="60" name="iteration.name" /></td>
		</tr>
		<tr>
			<td>Description</td>
			<td></td>
			<td colspan="2"><ww:textarea cols="70" rows="10"
				cssClass="useWysiwyg" name="iteration.description" /></td>
		</tr>
		<tr>
			<td>Project</td>
			<td>*</td>
			<td colspan="2"><select name="projectId">
				<option class="inactive" value="">(select project)</option>
				<c:forEach items="${productList}" var="product">
					<option value="" class="inactive productOption">${aef:out(product.name)}</option>
					<c:forEach items="${product.projects}" var="project">
						<c:choose>
							<c:when test="${project.id == currentProjectId}">
								<option selected="selected" value="${project.id}"
									class="projectOption" title="${project.name}">${aef:out(project.name)}</option>
							</c:when>
							<c:otherwise>
								<option value="${project.id}" title="${project.name}"
									class="projectOption">${aef:out(project.name)}</option>
							</c:otherwise>
						</c:choose>
					</c:forEach>
				</c:forEach>
			</select></td>
		</tr>
		<tr>
			<td>Planned iteration size</td>
			<td></td>
			<td colspan="2"><ww:textfield size="10" id="iteration.backlogSize"
				name="iteration.backlogSize" /> (total man hours)</td>
		</tr>
		<tr>
			<td>Start date</td>
			<td>*</td>
			<td colspan="2"><aef:datepicker
				id="create_iteration_start_date" name="startDate"
				format="%{getText('webwork.shortDateTime.format')}"
				value="%{#start}" /></td>
		</tr>
		<tr>
			<td>End date</td>
			<td>*</td>
			<td colspan="2"><aef:datepicker
				id="create_iteration_end_date" name="endDate"
				format="%{getText('webwork.shortDateTime.format')}" value="%{#end}" />

			</td>
		</tr>
		<tr>
            <td></td>
            <td></td>
            <td><ww:submit value="Create" id="createButton" /></td>
            <td class="deleteButton"><ww:reset value="Cancel"
                cssClass="closeDialogButton" /></td>
        </tr>
	</table>
</ww:form></div>