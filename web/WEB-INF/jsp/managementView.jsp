<%@ include file="./inc/_taglibs.jsp" %>
<%@ include file="./inc/_header.jsp" %>
<aef:menu navi="${contextName}" /> 

<p>T�h�n [alkuaika] ja [loppuaika], defaulttina [nykyhetki-viikko] ja [nykyhetki+3kk]</p>
<p>Gantt-kaavio n�ytt�� asiat vain t�lt� aikav�lilt�</p>

	<ww:form action="#">
		<ww:hidden name="deliverableId" value="${deliverable.id}"/>
		<ww:hidden name="productId"/>

Startdate: <ww:textfield size="4" name="startdate"/>  - Enddate: <ww:textfield  size="4" name="enddate"/>
			<ww:submit value="Select timescale"/> TODO!
		</ww:form>


<p>
        <img src="drawGantChart.action"/>
</p>

<%@ include file="./inc/_footer.jsp" %>
