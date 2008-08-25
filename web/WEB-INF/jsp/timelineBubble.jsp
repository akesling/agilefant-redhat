<%@ include file="./inc/_taglibs.jsp"%>
<div class="timelineBubble">
<div class="bubbleTitle">
<c:choose>
	<c:when test="${aef:isProject(backlog)}"><a href="editProject.action?projectId=${backlog.id}"></c:when>
	<c:when test="${aef:isIteration(backlog)}"><a href="editIteration.action?iterationId=${backlog.id}"></c:when>
</c:choose>
<c:out value="${backlog.name}" /></a></div>
	<table cellpadding="0" cellspacing="0">
	  			<th class="titleCol">Planned size</th>
	  			<td class="dataCol">
	  				<c:choose>
	    				<c:when test="${backlog.backlogSize != null}">
	  						<c:out value="${backlog.backlogSize}" />				
	  					</c:when>
	  					<c:otherwise>&mdash;</c:otherwise>
	  				</c:choose>
	  			</td>
	  			<c:choose>
	  				<c:when test="${aef:isIteration(backlog) || ((!empty backlog.backlogItems) && (empty backlog.iterations))}">
		   			<td rowspan="5">
						<div class="smallBurndown">
						  <c:choose>
						  	<c:when test="${aef:isIteration(backlog)}"><a href="editIteration.action?iterationId=${backlog.id}#bigChart"><img src="drawSmallChart.action?iterationId=${backlog.id}"/></c:when>
						  	<c:otherwise><a href="editProject.action?projectId=${backlog.id}#bigChart"><img src="drawSmallProjectChart.action?projectId=${backlog.id}"/></c:otherwise>
						  </c:choose>      
	                   </a></div>
	                      					
	                 	<table>
		            	<tr>
			            	 <th>Velocity</th>
			                 <td><c:out value="${backlog.metrics.dailyVelocity}" /> / day</td>
		               </tr>
		               <c:if test="${backlog.metrics.backlogOngoing}">
		                  <tr>
			                 <th>Schedule variance</th>
			                 <td>
			                 	<c:choose>
				                    <c:when test="${backlog.metrics.scheduleVariance != null}">
					                   <c:choose>
	                                       <c:when test="${backlog.metrics.scheduleVariance > 0}">
	                                            <span class="red">+
	                                       </c:when>
	                                        <c:otherwise>
	                                            <span>
	                                        </c:otherwise>
	                                   </c:choose>
					                   <c:out value="${backlog.metrics.scheduleVariance}" /> days
					                   </span>
		                            </c:when>
				                    <c:otherwise> unknown </c:otherwise>
			                 </c:choose>
			                </td>
		                  </tr>
		                  <tr>
			                 <th>Scoping needed</th>
			                 <td><c:choose>
				                    <c:when test="${backlog.metrics.scopingNeeded != null}">
					                   <c:out value="${backlog.metrics.scopingNeeded}" />
				                    </c:when>
				                    <c:otherwise>unknown</c:otherwise>
			                 </c:choose></td>
		                  </tr>
		                  </c:if>
	                 </table> 			
	  				</td>
	  				</c:when>
	  				<c:otherwise><td></td></c:otherwise>
	  				</c:choose>
	  			</td>
	  		
	  		</tr>
	 
	  		<tr>
	  			<th>Timeframe</th>
	  			<td>
	  				<ww:date name="backlog.startDate" format="%{getText('webwork.date.format')}" />
				- <ww:date name="backlog.endDate" format="%{getText('webwork.date.format')}" />
	  			</td>
	  			<td></td>
	  		</tr>
	  		<tr>	
	  			<th>Effort left</th>
	  			<td>${backlog.metrics.effortLeft}</td>   
	  			<td></td>			
	  		</tr>
	  		<tr>	
	  			<th>Original estimate</th>
	  			<td>${backlog.metrics.originalEstimate}</td> 
	  			<td></td>  			
	  		</tr>
	  		<tr>	
	  			<th>Completed</th>
	  			<td>${backlog.metrics.percentDone}% (${backlog.metrics.completedItems} / ${backlog.metrics.totalItems})</td>
	  			<td></td>
	
	  		</tr>
	  		<c:if test="${aef:isProject(backlog)}">
	  		<tr>
	  			<th>Assignees</th>
	  			<td colspan="2">
	  				<c:forEach items="${backlog.responsibles}" var="responsible"><c:out value="${responsible.initials}" />&nbsp;</c:forEach>
	  			</td>
	  			<td></td>
	  		</tr>
	  		</c:if>
	  		<tr>
	  			<th>Themes</th>
	  			<td colspan="2">
	  			<c:choose>
	  				<c:when test="${aef:isProject(backlog)}">
	  					<c:forEach items="${themeCache}" var="bt"><c:out value="${bt.name}" /> (${themeEffort[bt]})&nbsp;</c:forEach>
	  				</c:when>
	  				<c:otherwise>
			  			<c:forEach items="${backlog.businessThemeBindings}" var="bindings">	
			  				<c:out value="${bindings.businessTheme.name}" />
			  				${bindings.boundEffort} <c:if test="${bindings.relativeBinding}"> ${bindings.percentage} </c:if>
			  			</c:forEach>	  				
	  				</c:otherwise>
	  			</c:choose>

	  			</td>
	  			<td></td>
	  		</tr>
	  </table>
</div>	  	