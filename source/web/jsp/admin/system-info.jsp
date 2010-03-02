<%--
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<f:verbatim>
<table cellpadding="2" cellspacing="2" border="0" width="100%">
<tr>
<td colspan="2">
</f:verbatim><a:panel label="#{msg.http_app_state}" id="http-application-state" border="white" bgcolor="white"
titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
expanded="false">
<a:httpApplicationState id="has" />
</a:panel>
<f:verbatim>
<br/>
</f:verbatim>
<a:panel label="#{msg.http_session_state}" id="http-session-state" border="white" bgcolor="white"
titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
expanded="false">
<a:httpSessionState id="hss" />
</a:panel>
<f:verbatim>
<br/>
</f:verbatim>
<a:panel label="#{msg.http_request_state}" id="http-request-state" border="white" bgcolor="white"
titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
expanded="false">
<a:httpRequestState id="hrs" />
</a:panel>
<f:verbatim>
<br/>
</f:verbatim>
<a:panel label="#{msg.http_request_params}" id="http-request-params" border="white" bgcolor="white"
titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
expanded="false">
<a:httpRequestParams id="hrp" />
</a:panel>
<f:verbatim>
<br/>
</f:verbatim>
<a:panel label="#{msg.http_request_headers}" id="http-request-headers" border="white" bgcolor="white"
titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
expanded="false">
<a:httpRequestHeaders id="hrh" />
</a:panel>
<f:verbatim>
<br/>
</f:verbatim>
<a:panel label="#{msg.repository_props}" id="repo-props" border="white" bgcolor="white"
titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
expanded="false">
<a:repositoryProperties id="rp" />
</a:panel>
<f:verbatim>
<br/>
</f:verbatim>
<a:panel label="#{msg.system_props}" id="system-props" border="white" bgcolor="white"
titleBorder="lbgrey" expandedTitleBorder="dotted" titleBgcolor="white" progressive="true" styleClass="mainSubTitle"
expanded="false">
<a:systemProperties id="sp" />
</a:panel>
<f:verbatim>
</td>
</tr>
</table>
</f:verbatim>