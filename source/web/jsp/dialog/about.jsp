<%--
  Copyright (C) 2005 Alfresco, Inc.
 
  Licensed under the Mozilla Public License version 1.1 
  with a permitted attribution clause. You may obtain a
  copy of the License at
 
    http://www.alfresco.org/legal/license.txt
 
  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  either express or implied. See the License for the specific
  language governing permissions and limitations under the
  License.
--%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/alfresco.tld" prefix="a" %>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<r:page titleId="title_about">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   <f:loadBundle basename="alfresco.version" var="version"/>
   
   <h:form acceptCharset="UTF-8" id="about">
   
   <%-- Main outer table --%>
   <table cellspacing="0" cellpadding="2">
      
      <%-- Title bar --%>
      <tr>
         <td colspan="2">
            <%@ include file="../parts/titlebar.jsp" %>
         </td>
      </tr>
      
      <%-- Main area --%>
      <tr valign="top">
         <%-- Shelf --%>
         <td>
            <%@ include file="../parts/shelf.jsp" %>
         </td>
         
         <%-- Work Area --%>
         <td width="100%">
            <table cellspacing="0" cellpadding="0" width="100%">
               <%-- Breadcrumb --%>
               <%@ include file="../parts/breadcrumb.jsp" %>
               
               <%-- Status and Actions --%>
               <tr>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_4.gif)" width="4"></td>
                  <td bgcolor="#dfe6ed">
                  
                     <%-- Status and Actions inner contents table --%>
                     <%-- Generally this consists of an icon, textual summary and actions for the current object --%>
                     <table cellspacing="4" cellpadding="0" width="100%">
                        <tr>
                           <td width=32>
                              <h:graphicImage id="logo" url="/images/logo/AlfrescoLogo32.gif" width="32" height="32" />
                           </td>
                           <td>
                              <div class="mainTitle"><h:outputText value="#{msg.title_about}" /></div>
                              <div class="mainSubText"><h:outputText value="#{msg.version}" />:&nbsp;<h:outputText value="#{AboutBean.edition}" />&nbsp;-&nbsp;v<h:outputText value="#{AboutBean.version}" /></div>
                           </td>
                        </tr>
                     </table>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- separator row with gradient shadow --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" width="4" height="9"></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" width="4" height="9"></td>
               </tr>
               
               <%-- Details --%>
               <tr valign=top>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td valign=top>
                     
                     <table cellspacing=0 cellpadding=0 border=0 width=100%>
                     <tr>
                     <td width=100% align=center>
                     
                        <div style="padding:8px">
                           <img src="<%=request.getContextPath()%>/images/logo/alfresco3d.jpg" width=520 height=168>
                        </div>
                        <div style="padding:4px">
                           <a href="http://www.alfresco.com/" class="title" target="new">http://www.alfresco.com</a>
                           <p>
                           Alfresco Software Inc. &#169; 2005-2006 All rights reserved. <a href="http://www.alfresco.com/legal/" target="new">Legal and License</a>
                        </div>
                        
                        <div style="padding:4px" class="mainSubTitle">
                           Alfresco Software utilises components or libraries from the following software vendors and companies
                        </div>
                        
                        <p>
                        
                        <div style="padding:4px">
                           <span align=center>
                              <a href="http://www.springframework.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/spring_logo.jpg" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://www.hibernate.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/hibernate_logo.gif" border=0></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span align=center>
                              <a href="http://www.apache.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/asf_logo_wide.gif" border=0></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span align=center>
                              <a href="http://jakarta.apache.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/jakarta-logo.gif" border=0></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span align=center>
                              <a href="http://www.java.com/" target="new"><img src="<%=request.getContextPath()%>/images/logo/java.gif" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://www.jboss.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/jbosslogo.gif" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://myfaces.apache.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/myfaces.png" border=0></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span align=center>
                              <a href="http://lucene.apache.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/lucene.gif" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://cglib.sourceforge.net/" target="new"><img src="<%=request.getContextPath()%>/images/logo/cglib.png" border=0></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span align=center>
                              <a href="http://www.pdfbox.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/pdfbox.gif" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://tinymce.moxiecode.com/" target="new"><img src="<%=request.getContextPath()%>/images/logo/tinymce.png" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://www.openoffice.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/open-office.gif" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://jooreports.sourceforge.net/" target="new"><img src="<%=request.getContextPath()%>/images/logo/jooreports.png" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://freemarker.sourceforge.net/" target="new"><img src="<%=request.getContextPath()%>/images/logo/freemarker.png" border=0></a>
                           </span>
                        </div>
                        
                        <p>
                        <p>
                        
                        <div style="padding:4px" class="mainSubTitle">
                           Alfresco Development tools:
                        </div>
                        
                        <div style="padding:4px">
                           <span align=center>
                              <a href="http://subversion.tigris.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/subversion_logo.png" border=0></a>
                           </span>
                           <span align=center>
                              <a href="http://www.eclipse.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/eclipse_logo.jpg" border=0></a>
                           </span>
                        </div>
                        
                        <p>
                        <p>
                        
                        <div style="padding:4px" class="mainSubTitle">
                           Alfresco Software contributors
                        </div>
                        
                        <table border=0 cellspacing=2 cellpadding=2>
                           <tr>
                              <td width=50% align=right>
                                 Kofax Release Script:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width=50%>
                                 <img src="<%=request.getContextPath()%>/images/logo/ardenringcroft_logo.png" border=0 alt="Arden Ringcroft">
                              </td>
                           </tr>
                           <tr>
                              <td width=50% align=right>
                                 Meta Data Extraction Framework and PDF/Open Office Format meta data extraction:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width=50%>
                                 Jesper Steen M&#248;ller
                              </td>
                           </tr>
                           <tr>
                              <td width=50% align=right>
                                 Open Document Format meta data extraction:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width=50%>
                                 Antti Jokipii
                              </td>
                           </tr>
                           <tr>
                              <td align=right width=50%>
                                 Language and translation packs:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width=50%>
                                 Camille B&#233;gnis,
                                 Andrejus Chaliapinas,
                                 Laurent Genier,
                                 Antti Jokipii,
                                 Henning Kristensen,
                                 Betty Mai,
                                 Fabian Mandelbaum,
                                 Theodoros Papageorgiou,
                                 Helio Silvio Piccinatto,
                                 Gian Luca Farina Perseu,
                                 Alex Revesz,
                                 Christian Roy,
                                 Philippe Seillier,
                                 Frank Shipley,
                                 Michiel Steltman,
                                 Gert Thiel,
                                 cnalfresco
                              </td>
                           </tr>
                        </table>
                        
                     </td>
                     <td valign="top">
                        <div style="padding-top:16px">
                        <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "greyround", "#F5F5F5"); %>
                        <table cellpadding="1" cellspacing="1" border="0">
                           <tr>
                              <td align="center">
                                 <h:commandButton value="#{msg.close}" action="browse" styleClass="wizardButton" />
                              </td>
                           </tr>
                        </table>
                        <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "greyround"); %>
                        </div>
                     </td>
                     
                     </tr>
                     </table>
                     
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- separator row with bottom panel graphics --%>
               <tr>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" width="4" height="4"></td>
                  <td width="100%" align="center" style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" width="4" height="4"></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    </h:form>
    
</f:view>

</r:page>