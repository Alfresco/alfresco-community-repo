<%@taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@taglib prefix="a" uri="/WEB-INF/alfresco.tld" %>
<%@taglib prefix="c" uri="/WEB-INF/c.tld" %>

<script type="text/javascript">
<!--
	setTimeout("document.location.href=" + "\'" + document.getElementById("url") + "\'", 10000);			
//-->
</script>
<script type="text/javascript">
<!--
	function timer(second)
	{	
		document.getElementById("time").innerHTML = second;
		second--;
		if (second >=0)
		{
			window.setTimeout('timer('+second+')', 1000);
		}
	}	
//-->
</script>


<f:verbatim>
<body onload="timer(10)">

	&nbsp;&nbsp;&nbsp;</f:verbatim><h:outputText value="#{msg.offline_download_first}"/>
	
	<f:verbatim>&nbsp;<b id="time">10</b>&nbsp;</f:verbatim>
	
	<h:outputText value="#{msg.offline_download_second}"/><f:verbatim>&nbsp;</f:verbatim>
	<a:actionLink id="url" style="text-decoration: underline;" value="#{msg.click_here}" href="#{CCProperties.document.properties.url}"/>
	
	<f:verbatim><br/><br/>
	
	&nbsp;&nbsp;&nbsp;</f:verbatim><h:outputText value="#{msg.offline_download_third}"/><f:verbatim>&nbsp;</f:verbatim>
		
	<a:actionLink value="#{msg.click_here}" 
				  action="dialog:userConsole"
				  actionListener="#{UsersDialog.setupUserAction}"
				  style="text-decoration: underline;">
       <f:param name="id" value="#{NavigationBean.currentUser.person.id}" />
    </a:actionLink>
	
	<f:verbatim><br/><br/>&nbsp;&nbsp;&nbsp;</f:verbatim>
	
	<h:graphicImage url="/images/icons/Help_icon.gif" alt="#{msg.help}"/><f:verbatim>&nbsp;&nbsp;</f:verbatim>	
	<h:outputText value="#{msg.offline_help}"/><f:verbatim>&nbsp;</f:verbatim> 		
	<a:actionLink style="text-decoration: underline;" value="#{msg.click_here}" href="http://wiki.alfresco.com/wiki/Main_Page"/>
	
<f:verbatim>	
<br/>
</f:verbatim>






