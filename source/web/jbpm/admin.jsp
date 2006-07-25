<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

<f:view>
<jsp:include page="header1.jsp" />
Administration
<jsp:include page="header2.jsp" />

<b><h:messages /></b>

TODO

<%--
(this page is not yet implemented)

<h4>Deploy process</h4>
<h:form id="deploy">
  <h:inputText value="#{adminBean.deployUrl}" /> &nbsp;&nbsp; 
  <h:commandButton action="#{adminBean.deployProcess}" value="Deploy" />
</h:form>

<h4>Database Schema</h4>
<h:form id="createschema">
  <h:commandButton action="#{adminBean.createSchema}" value="Create Schema" />
</h:form>
<h:form id="dropschema">
  <h:commandButton action="#{adminBean.dropSchema}" value="Drop Schema" />
</h:form>

<h4>Scheduler</h4>
<h:dataTable value="#{adminBean.schedulerHistoryLogs}" var="schedulerLog">
  <h:column >
    <h:outputText value="#{schedulerLog.date}">
      <f:convertDateTime type="date" dateStyle="full"/>
    </h:outputText>
  </h:column>
  <h:column >
    <h:outputText value="#{schedulerLog.timer}" />
  </h:column>
  <h:column >
    <h:outputText value="#{schedulerLog.exception}" />
  </h:column>
</h:dataTable> 

--%>
<jsp:include page="footer.jsp" />
</f:view>
