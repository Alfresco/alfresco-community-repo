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

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>

<a:booleanEvaluator value="#{DialogManager.bean.sourceFound}">
<f:verbatim>
   <table cellspacing="0" cellpadding="0" border="0" width="100%">
      <tr>
         <td>
            <table cellpadding="2" cellspacing="2" border="0" width="100%">
               <tr>
                  <td class="wizardSectionHeading"> </f:verbatim>
                     <h:outputText value="#{msg.version_info}" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:outputText value="#{msg.new_version_has}" escape="false" rendered="#{DialogManager.bean.sourceVersionable}" />
                     <h:outputText value="#{msg.initial_version}" escape="false" rendered="#{!DialogManager.bean.sourceVersionable}" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:selectOneRadio value="#{CCProperties.minorChange}" layout="pageDirection" rendered="#{DialogManager.bean.sourceVersionable}">
                        <f:selectItem itemValue="#{true}" itemLabel="#{msg.minor_changes} (#{DialogManager.bean.minorNewVersionLabel})" />
                        <f:selectItem itemValue="#{false}" itemLabel="#{msg.major_changes} (#{DialogManager.bean.majorNewVersionLabel})" />
                     </h:selectOneRadio> <f:verbatim>
                     </span> <br/>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:outputText value="#{msg.version_notes}"/> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:inputTextarea value="#{CCProperties.versionNotes}" rows="4" cols="50" /> <f:verbatim>
                     </span>
                  </td>
               </tr>
               <tr>
                  <td class="paddingRow"> </td>
               </tr>
            </table>
         </td>
      </tr>
   </table>
</f:verbatim>
</a:booleanEvaluator>
