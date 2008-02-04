<%--
 * Copyright (C) 2005-2007 Alfresco Software Limited.

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
--%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a"%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r"%>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8"%>
<%@ page isELIgnored="false"%>

<f:verbatim>
   <table cellspacing="0" cellpadding="0" border="0" width="100%">
      <tr>
         <td> </f:verbatim>
         <a:booleanEvaluator value="#{DialogManager.bean.versionable}"> <f:verbatim>
            <table cellpadding="2" cellspacing="2" border="0" width="100%">
               <tr>
                  <td class="wizardSectionHeading"> </f:verbatim>
                     <h:outputText value="#{msg.version_info}" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:outputText value="#{msg.new_version_has}" escape="false" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:selectOneRadio value="#{CCProperties.minorChange}">
                        <f:selectItem itemValue="true" itemLabel="#{msg.minor_changes} (#{DialogManager.bean.minorNewVersionLabel})" />
                        <f:selectItem itemValue="false" itemLabel="#{msg.major_changes} (#{DialogManager.bean.majorNewVersionLabel})" />
                     </h:selectOneRadio> <f:verbatim>
                     </span> <br/>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:outputText value="#{msg.version_notes}" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                  <h:inputTextarea value="#{CCProperties.versionNotes}" rows="4" cols="50" /> <f:verbatim>
                  </span></td>
               </tr>
               <tr>
                  <td class="paddingRow"></td>
               </tr>
            </table> </f:verbatim>
         </a:booleanEvaluator>
         <a:booleanEvaluator value="#{!DialogManager.bean.versionable}"> <f:verbatim>
            <table cellpadding="2" cellspacing="2" border="0" width="100%">
               <tr>
                  <td class="wizardSectionHeading"> </f:verbatim>
                     <h:outputText value="#{msg.not_versionable}" /> <f:verbatim>
                  </td>
               </tr>
               <tr>
                  <td> </f:verbatim>
                     <h:outputText value="#{msg.check_in}?" escape="false" /> <f:verbatim>
                  </td>
               </tr>
            </table> </f:verbatim>
         </a:booleanEvaluator> <f:verbatim>
         </td>
      </tr>
   </table>
</f:verbatim>
