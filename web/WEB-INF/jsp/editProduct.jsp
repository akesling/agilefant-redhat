<%@ include file="./inc/_taglibs.jsp"%>
<%@ include file="./inc/_header.jsp"%>

<c:if test="${product.id > 0}">
	<aef:bct productId="${product.id}" />
</c:if>

<c:set var="divId" value="1336" scope="page" />
<aef:menu navi="backlog" pageHierarchy="${pageHierarchy}" />
<ww:actionerror />
<ww:actionmessage />

<aef:openDialogs context="businessTheme" id="openThemes" />

<aef:openDialogs context="project" id="openProjects" />

<script type="text/javascript">

function setThemeActivityStatus(themeId,status) {
	var url = "";
	if(status == true) {
		url = "ajaxActivateBusinessTheme.action";
	} else {
		url = "ajaxDeactivateBusinessTheme.action";
	}
	$.post(url,{businessThemeId: themeId},function(data,status) {
		reloadPage();
	});
}

function deleteTheme(themeId) {
	var confirm = confirmDelete();
	var url = "ajaxDeleteBusinessTheme.action";			
	if (confirm) {
		$.post(url,{businessThemeId: themeId},function(data) {
			reloadPage();
		});
	}
}

$(document).ready(function() {
    <c:forEach items="${openThemes}" var="openTheme">
        handleTabEvent("businessThemeTabContainer-${openTheme[0]}", "businessTheme", ${openTheme[0]}, ${openTheme[1]});
    </c:forEach>
    
    <c:forEach items="${openProjects}" var="openProject">
        handleTabEvent("projectTabContainer-${openProject[0]}", "project", ${openProject[0]}, ${openProject[1]});
    </c:forEach>
});



/* Initialize the SimileAjax object */
var SimileAjax = {
    loaded:                 false,
    loadingScriptsCount:    0,
    error:                  null,
    params:                 { bundle:"true" }
};
SimileAjax.Platform = new Object();
</script>
<script type="text/javascript" src="static/js/timeline/simile-ajax-bundle.js"></script>

<!-- Include timeline -->
<script type="text/javascript">
var productId = ${product.id};
</script>
<script type="text/javascript" src="static/js/timeline/timeline-load.js"></script>
<script type="text/javascript" src="static/js/timeline/timeline-bundle.js"></script>
<script type="text/javascript" src="static/js/timeline/timeline-custom.js"></script>

<h2><c:out value="${product.name}" /></h2>
<table>
	<tbody>
		<tr>
			<td>
			<div class="subItems" style="margin-top: 0">
			<div class="subItemHeader"><script type="text/javascript">
			                function expandDescription() {
			                    document.getElementById('descriptionDiv').style.maxHeight = "1000em";
			                    document.getElementById('descriptionDiv').style.overflow = "visible";
			                }
			                function collapseDescription() {
			                    document.getElementById('descriptionDiv').style.maxHeight = "12em";
			                    document.getElementById('descriptionDiv').style.overflow = "hidden";
			                }
			                function editProduct() {
			                	toggleDiv('editProductForm'); toggleDiv('descriptionDiv'); showWysiwyg('productDescription'); return false;
			                }
			                </script>


			<table cellspacing="0" cellpadding="0">
				<tr>
					<td class="header">Details <a href=""
						onclick="return editProduct();">Edit &raquo;</a></td>
					<td class="icons"><a href=""
						onclick="expandDescription(); return false;"> <img
						src="static/img/plus.png" width="18" height="18" alt="Expand"
						title="Expand" /> </a> <a href=""
						onclick="collapseDescription(); return false;"> <img
						src="static/img/minus.png" width="18" height="18" alt="Collapse"
						title="Collapse" /> </a></td>
				</tr>
			</table>
			</div>
			<div class="subItemContent">
			<div id="descriptionDiv" class="descriptionDiv"
				style="display: block;">
			<table class="infoTable" cellpadding="0" cellspacing="0">

				<tr>
					<td colspan="2" class="description">${product.description}</td>
					<td class="info4">&nbsp;</td>
				</tr>
			</table>
			</div>

			<div id="editProductForm" style="display: none;">
			<div class="validateWrapper validateExistingProduct">
			<ww:form id="productEditForm"
				action="storeProduct" method="post">
				<ww:hidden name="productId" value="${product.id}" />

				<table class="formTable">
					<tr>
						<td>Name</td>
						<td>*</td>
						<td colspan="2"><ww:textfield size="60" name="product.name" /></td>
					</tr>
					<tr>
						<td>Description</td>
						<td></td>
						<td colspan="2"><ww:textarea name="product.description"
							id="productDescription" cols="70" rows="10"
							value="${aef:nl2br(product.description)}" /></td>
					</tr>
					<tr>
						<td></td>
						<td></td>
						<c:choose>
							<c:when test="${productId == 0}">
								<td><ww:submit value="Create" /></td>
							</c:when>
							<c:otherwise>
								<td><ww:submit value="Save" />
								<td class="deleteButton"><ww:submit
									onclick="return confirmDelete()" action="deleteProduct"
									value="Delete" /></td>
							</c:otherwise>
						</c:choose>
					</tr>
				</table>
			</ww:form></div></div>
			</div>
			</td>
		</tr>
	</tbody>
</table>

<c:if test="${product.id > 0}">
<table>
    <!-- The timeline -->
    <tr>
    <td>
    <div id="subItems">
    <div id="subItemHeader">
    <table cellspacing="0" cellpadding="0">
                            <tr>
                                <td class="header">Product roadmap</td>
                                <td style="width: 100px;">
                                	<select id="productTimelinePeriod" onchange="updateTimelinePeriod(this);">
                                		<option value="1" selected="selected">display quartal</option>
                                		<option value="2">display 6 months</option>
                                		<option value="3">display one year</option>
                                		<option value="4">display three years</option>
                                	</select>
                                </td>
                            </tr>
                        </table>
    </div>
    
       
    <div id="productTimeline"></div>
    
    <div id="timelineLegend" style="width:100%; text-align:center; margin-bottom: 10px;">
    <table style="margin: auto; border: 1px solid #ccc;" cellpadding="2" cellspacing="2">
        <tr>
            <td><div class="timeline-band-project-ok" style="display:block;width:50px;height:5px;margin:2px;">&nbsp;</div>
            <div class="timeline-band-project-challenged" style="display:block;width:50px;height:5px;margin:2px;">&nbsp;</div>
            <div class="timeline-band-project-critical" style="display:block;width:50px;height:5px;margin:2px;">&nbsp;</div></td>
            <td>Project</td>
            <td><div class="timeline-band-iteration" style="display:block;width:50px;height:5px;margin:2px;">&nbsp;</div></td>
            <td>Iteration</td>
            
        </tr>
    </table>
    </div>
    
    </div>
    </td>
    </tr>

	<tr>
		<td>
			<div class="subItems">
				<div class="subItemHeader">
				<table cellspacing="0" cellpadding="0">
	                <tr>
	                    <td class="header">
	                    Projects
	                    <ww:url id="createLink" action="ajaxCreateProject" includeParams="none">
						  <ww:param name="productId" value="${product.id}" />
						</ww:url>
						<ww:a href="%{createLink}" title="Create a new project" cssClass="openCreateDialog openProjectDialog">
						Create new &raquo;</ww:a>
					</td>
					</tr>
				</table>
				</div>

				<c:if test="${!empty product.projects}">
				<div class="subItemContent">
				<display:table class="listTable" name="product.projects"
					id="row" requestURI="editProduct.action">
							
					<display:column sortable="false" title="St." class="statusColumn">
						<%@ include file="./inc/_projectStatusIcon.jsp"%>
						<div id="projectTabContainer-${row.id}" style="overflow:visible; white-space: nowrap; width: 15px;"></div>
					</display:column>		
														
					<display:column sortable="true" sortProperty="name" title="Name">
						<ww:url id="editLink" action="editProject" includeParams="none">
							<ww:param name="productId" value="${product.id}" />
							<ww:param name="projectId" value="${row.id}" />
						</ww:url>
						<ww:a
							href="%{editLink}&contextViewName=editProduct&contextObjectId=${product.id}">
							${aef:html(row.name)}
						</ww:a>
					</display:column>					
					
					<display:column sortable="true" sortProperty="projectType.name"
						title="Project type">
						<div>
						<c:choose>
						<c:when test="${(!empty row.projectType)}">
							${aef:html(row.projectType.name)}
						</c:when>
						<c:otherwise>
							undefined
						</c:otherwise>
						</c:choose>
						</div>
					</display:column>
					
					<%--
					<display:column sortable="true" sortProperty="projectType.name"
						title="Project type" property="projectType.name" />
					--%>
					<display:column sortable="false" title="Iter. info">
						<c:out value="${row.metrics.numberOfOngoingIterations}" /> / 
						<c:out value="${row.metrics.numberOfAllIterations}" />
					</display:column>
					
					<display:column sortable="false" title="Assignees">
						<c:out value="${row.metrics.assignees}" />
					</display:column>					
															
					<display:column sortable="true" title="Start date">
						<ww:date name="#attr.row.startDate" />
					</display:column>

					<display:column sortable="true" title="End date">
						<ww:date name="#attr.row.endDate" />
					</display:column>
												
					<display:column sortable="false" title="Actions">
						<img src="static/img/edit.png" alt="Edit" title="Edit project" style="cursor: pointer;" onclick="handleTabEvent('projectTabContainer-${row.id}','project',${row.id},0); return false;" />
						<%--<img src="static/img/backlog.png" alt="Iterations" title="Iterations" style="cursor: pointer;" onclick="handleTabEvent('projectTabContainer-${row.id}','project',${row.id},1);" />--%>
						<ww:url id="deleteLink" action="deleteProject"
							includeParams="none">
							<ww:param name="productId" value="${product.id}" />
							<ww:param name="projectId" value="${row.id}" />
						</ww:url>
						<ww:a href="%{deleteLink}&contextViewName=editProduct&contextObjectId=${product.id}"
							onclick="return confirmDelete()">
							<img src="static/img/delete_18.png" alt="Delete project" title="Delete project" style="cursor: pointer;"/>
						</ww:a>
					</display:column>
				</display:table>
				</div>
				</c:if>
			</div>
		</td>
	</tr>
</table>

<table>	
	<tr>
		<td>
			<div class="subItems">
				<div class="subItemHeader">
					<table cellspacing="0" cellpadding="0">
						<tr>
							 <td class="header">
							 Themes
							 <ww:url id="createThemeLink" action="ajaxCreateBusinessTheme" includeParams="none">
							     <ww:param name="productId" value="${productId}"></ww:param>
							 </ww:url>
							 <ww:a href="%{createThemeLink}" cssClass="openCreateDialog openThemeDialog"
							     title="Create a new theme">Create new &raquo;</ww:a>
							 </td>
						</tr>
					</table>
				</div>
				
				
				
				<c:if test="${!empty activeBusinessThemes}">
				<div id="subItemContent">
				<display:table class="themeEditTable" name="activeBusinessThemes"
					id="row" defaultsort="1">
					<display:column title="Name" class="themeEditNameColumn">
						<c:out value="${row.name}" />					
						<div id="businessThemeTabContainer-${row.id}" style="overflow:visible; white-space: nowrap; width: 115px;"></div>
					</display:column>
					<display:column title="Description" class="themeDescriptionColumn">
					   <c:out value="${fn:substring(row.description, 0, 50)}" />
					</display:column>
					<display:column title="Completed BLIs">
						<c:out value="${businessThemeMetrics[row].donePercentage}" />% 
						(<c:out value="${businessThemeMetrics[row].numberOfDoneBlis}" /> /
						<c:out value="${businessThemeMetrics[row].numberOfBlis}" />)					
					</display:column>				
					<display:column title="Actions">
						<img src="static/img/edit.png" alt="Edit" title="Edit theme" style="cursor: pointer;" onclick="handleTabEvent('businessThemeTabContainer-${row.id}','businessTheme',${row.id},0); return false;" />
						<img src="static/img/disable.png" alt="Disable" title="Disable theme" style="cursor: pointer;" onclick="setThemeActivityStatus(${row.id},false); return false;" />
						<img src="static/img/delete_18.png" alt="Delete" title="Delete theme" style="cursor: pointer;" onclick="deleteTheme(${row.id}); return false;" />
					</display:column>
				</display:table>					
					
					
				</div>
				</c:if>
				
			</div>
		</td>
	</tr>
</table>

<!-- Show non-active themes table only if there are any. -->
<c:if test="${!empty nonActiveBusinessThemes}">
<table>	
	<tr>
		<td>
			<div id="subItems">
				<div id="subItemHeader">
					<table cellspacing="0" cellpadding="0">
						<tr>
							 <td class="header">
							 Deactivated themes
							 </td>
						</tr>
					</table>
				</div>
								
				<div id="subItemContent">
				<display:table class="themeEditTable" name="nonActiveBusinessThemes"
					id="row" defaultsort="1">
					<display:column title="Name" class="themeEditNameColumn">
						<c:out value="${row.name}" />					
						<div id="businessThemeTabContainer-${row.id}" style="overflow:visible; white-space: nowrap; width: 115px;"></div>
					</display:column>
					<display:column title="Description" class="themeDescriptionColumn">
                       <c:out value="${fn:substring(row.description, 0, 50)}" />
                    </display:column>
					<display:column title="Completed BLIs">
						<c:out value="${businessThemeMetrics[row].donePercentage}" />% 
						(<c:out value="${businessThemeMetrics[row].numberOfDoneBlis}" /> /
						<c:out value="${businessThemeMetrics[row].numberOfBlis}" />)					
					</display:column>								
					<display:column title="Actions">
						<img src="static/img/edit.png" alt="Edit" title="Edit theme" style="cursor: pointer;" onclick="handleTabEvent('businessThemeTabContainer-${row.id}','businessTheme',${row.id},0); return false;" />
						<img src="static/img/enable.png" alt="Enable" title="Enable theme" style="cursor: pointer;" onclick="setThemeActivityStatus(${row.id},true); return false;" />
						<img src="static/img/delete_18.png" alt="Delete" title="Delete theme" style="cursor: pointer;" onclick="deleteTheme(${row.id}); return false;" />
					</display:column>
				</display:table>															
				</div>								
			</div>
		</td>
	</tr>
</table>
</c:if>

<table>	
	<tr>
		<td>
			<div class="subItems">
			<div class="subItemHeader">
				<table cellspacing="0" cellpadding="0">
	                <tr>
	                    <td class="header">
	                    Backlog items <ww:url
					id="createBacklogItemLink" action="ajaxCreateBacklogItem"
					includeParams="none">
					<ww:param name="backlogId" value="${product.id}" />
				</ww:url> <ww:a cssClass="openCreateDialog openBacklogItemDialog"
					href="%{createBacklogItemLink}&contextViewName=editProduct&contextObjectId=${product.id}">Create new &raquo;</ww:a>
					</td>
					</tr>
				</table>
			</div>			

			<c:if test="${!empty product.backlogItems}">
			<div class="subItemContent">
				<%@ include file="./inc/_backlogList.jsp"%>
			</div>
			</c:if>
			</div>
		</td>
	</tr>
</table>

</c:if>
<%@ include file="./inc/_footer.jsp"%>