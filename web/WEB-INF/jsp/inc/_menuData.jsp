<%@ include file="./_taglibs.jsp"%>


<c:set var="count" value="0" />
[
<c:forEach items="${menuData.menuItems}" var="item">
<c:set var="count" value="${count + 1}" />


    {
        <%-- Create the link url --%>
        <ww:url id="editLink" action="contextView" includeParams="none">
            <ww:param name="contextObjectId" value="${item.id}" />
            <ww:param name="resetContextView" value="true" />
        </ww:url>
    
        <%-- Create the text --%>
        "text": '<a href="${editLink}&amp;contextName=${menuData.objectTypes[item]}"><c:out value="${item.name}" /></a>',
        
        <%-- Check for children --%>
        <c:choose>
            <c:when test="${aef:listContains(openBacklogs, item)}">
                <c:set var="subMenuData" value="${openDatas[item]}" />
                "expanded": true,
                "children": 
                [
                    <c:set var="subCount" value="0" />
	                <c:forEach items="${subMenuData.menuItems}" var="subItem">
	                   <c:set var="subCount" value="${subCount + 1}" />
	                   { 
                            <ww:url id="subEditLink" action="contextView" includeParams="none">
                                <ww:param name="contextObjectId" value="${subItem.id}" />
                                <ww:param name="resetContextView" value="true" />
                            </ww:url>

                            "text": '<a href="${subEditLink}&amp;contextName=${subMenuData.objectTypes[subItem]}"><c:out value="${subItem.name}" /></a>',
                            
                            <c:choose>
                            <c:when test="${aef:listContains(openBacklogs, subItem)}">
                            <c:set var="subsubMenuData" value="${openDatas[subItem]}" />
                            "expanded": true,
                            "children":
                            [
                                <%-- And once again, iterate the children  --%>
                                <c:set var="subsubCount" value="0" />
                                <c:forEach items="${subsubMenuData.menuItems}" var="subsubItem">
                                    <c:set var="subsubCount" value="${subsubCount + 1}"/>
                                    {
                                        <ww:url id="subsubEditLink" action="contextView" includeParams="none">
                                            <ww:param name="contextObjectId" value="${subsubItem.id}" />
                                            <ww:param name="resetContextView" value="true" />
                                        </ww:url>            
                                    
                                        "text": '<a href="${subsubEditLink}&amp;contextName=${subsubMenuData.objectTypes[subsubItem]}"><c:out value="${subsubItem.name}" /></a>',
                                        
                                        <c:choose>
                                        <c:when test="${subsubItem.id == currentIterationId}">
                                            "classes": "path selected",
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${aef:isBeforeThisDay(subsubItem.endDate)}">
                                                "classes": "past",
                                            </c:if>
                                            <c:if test="${!aef:isBeforeThisDay(subsubItem.startDate)}">
                                                "classes": "upcoming",
                                            </c:if>
                                        </c:otherwise>
                                        </c:choose>
                                    },
                                </c:forEach>
                            ],
                            </c:when>
                            <c:otherwise>
                                <c:if test="${subMenuData.hasChildren[subItem]}">
                                    "hasChildren": true,
                                </c:if>
                            </c:otherwise>
                            </c:choose>
                            
                            <%-- Classes --%>
                            <c:choose>
                            <c:when test="${aef:isProject(subItem)}">
                            <c:choose>
                            <c:when test="${subItem.id == currentProjectId}">
                                <c:choose>
                                <c:when test="${!empty currentIterationId}">
                                    "classes": "path",
                                </c:when>
                                <c:otherwise>
                                    "classes": "selected path",
                                </c:otherwise>
                                </c:choose>
                            </c:when>
                            <%-- Check, if is past or upcoming --%>
                            <c:otherwise>
                                <c:if test="${aef:isBeforeThisDay(subItem.endDate)}">
                                    "classes": "past",
                                </c:if>
                                <c:if test="${!aef:isBeforeThisDay(subItem.startDate)}">
                                    "classes": "upcoming",
                                </c:if>
                            </c:otherwise>
                            </c:choose>
                            </c:when>
                            <c:when test="${aef:isIteration(subItem)}">
                                <c:choose>
                                <c:when test="${subItem.id == currentIterationId}">
                                    "classes": "selected path",
                                </c:when>
                                <c:otherwise>
                                    <c:if test="${aef:isBeforeThisDay(subItem.endDate)}">
                                        "classes": "past",
                                    </c:if>
                                    <c:if test="${!aef:isBeforeThisDay(subItem.startDate)}">
                                        "classes": "upcoming",
                                    </c:if>
                                </c:otherwise>
                                </c:choose>
                            </c:when>
                            </c:choose>
                        
                        <%--
				            <c:choose>  
					        <c:when test="${aef:isProject(subItem)}">
                                <c:if test="${subItem.id == currentProjectId}">
							    <c:choose>
								    <c:when test="${!empty currentIterationId}">
                                        "classes": "path",
                                    </c:when>
                                    <c:otherwise>
                                        "classes": "selected path",
                                    </c:otherwise>
                                </c:choose>
                                </c:if>
                            </c:when>
                            <c:when test="${aef:isIteration(subItem)}">
                                <c:if test="${subItem.id == currentIterationId}">
                                    "classes": "selected path",
                                </c:if>
                            </c:when>
                            </c:choose>--%>
                            
                            "id": "menubacklog_<c:out value="${subItem.id}" />"
                            
	                   },
	                 </c:forEach>
                ],
            </c:when>
            <c:otherwise>
                <c:if test="${menuData.hasChildren[item]}">
                "hasChildren": true,
                </c:if>
            </c:otherwise>
        </c:choose>

        <%-- Classes --%>
        <c:choose>
        <c:when test="${aef:isProduct(item)}">
            <c:if test="${item.id == currentProductId}">
            <c:choose>
                <c:when test="${!empty currentProjectId}">
                    "classes": "path",
                </c:when>
                <c:otherwise>
                    "classes": "selected path",
                </c:otherwise>
            </c:choose>
            </c:if>
        </c:when>
        <c:when test="${aef:isProject(item)}">
            <c:choose>
            <c:when test="${item.id == currentProjectId}">
            <c:choose>
                <c:when test="${!empty currentIterationId}">
                    "classes": "path",
                </c:when>
                <c:otherwise>
                    "classes": "selected path",
                </c:otherwise>
            </c:choose>
            </c:when>
            <%-- Check, if is past or upcoming --%>
            <c:otherwise>
                <c:if test="${aef:isBeforeThisDay(item.endDate)}">
                    "classes": "past",
                </c:if>
                <c:if test="${!aef:isBeforeThisDay(item.startDate)}">
                    "classes": "upcoming",
                </c:if>
            </c:otherwise>
            </c:choose>
        </c:when>
        <c:when test="${aef:isIteration(item)}">
            <c:choose>
            <c:when test="${item.id == currentIterationId}">
                "classes": "selected path",
            </c:when>
            <c:otherwise>
                <c:if test="${aef:isBeforeThisDay(item.endDate)}">
                    "classes": "past",
                </c:if>
                <c:if test="${!aef:isBeforeThisDay(item.startDate)}">
                    "classes": "upcoming",
                </c:if>
            </c:otherwise>
            </c:choose>
        </c:when>
        </c:choose>

        <%-- The id --%>
        "id": "menubacklog_<c:out value="${item.id}" />"
    },
</c:forEach>
]