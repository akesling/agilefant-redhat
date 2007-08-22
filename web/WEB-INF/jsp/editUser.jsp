<%@ include file="./inc/_taglibs.jsp" %>
<%@ include file="./inc/_header.jsp" %>

<aef:menu navi="users" pageHierarchy="${pageHierarchy}"/> 
	<ww:actionerror/>
	<ww:actionmessage/>

	<c:choose>
		<c:when test="${user.id == 0}">
			<h2>Create user</h2>
		</c:when>
		<c:otherwise>
			<h2>Edit user</h2>
		</c:otherwise>
	</c:choose>

	<ww:form method="POST" action="storeUser">
		<c:if test="${user.id > 0}">
			<p>
				To keep the old password, just leave password fields empty.
			</p>
		</c:if>
		<ww:hidden name="userId" value="${user.id}"/>

		<table class="formTable">
		<tr>
		<td></td>
		<td></td>
		<td></td>	
		</tr>
		<tr>
		<td>Full name</td>
		<td>*</td>
		<td><ww:textfield name="user.fullName"/></td>	
		</tr>
		<tr>
		<td>User/login id</td>
		<td>*</td>
		<td><ww:textfield name="user.loginName"/></td>	
		</tr>
		<tr>
		<td>Email</td>
		<td>*</td>
		<td><ww:textfield name="user.email" value="${user.email}"/></td>	
		</tr>
		<tr>
		<td>Password</td>
		<td>*</td>
		<td><ww:password name="password1"/></td>	
		</tr>
		<tr>
		<td>Confirm password</td>
		<td>*</td>
		<td><ww:password name="password2"/></td>	
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
<%@ include file="./inc/_footer.jsp" %>