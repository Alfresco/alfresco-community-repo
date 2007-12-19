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
   
   <h:form acceptcharset="UTF-8" id="about">
   
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
                           <td width="32">
                              <h:graphicImage id="logo" url="/images/logo/AlfrescoLogo32_bluebg.gif" alt="Alfresco logo" width="32" height="32" />
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
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_7.gif" alt="" width="4" height="9"></td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/statuspanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/statuspanel_9.gif" alt="" width="4" height="9"></td>
               </tr>
               
               <%-- Details --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td valign="top">
                     
                     <table cellspacing="0" cellpadding="0" border="0" width="100%">
                     <tr>
                     <td width="100%" align="center">
                     
                        <div style="padding:8px">
                           <img src="<%=request.getContextPath()%>/images/logo/alfresco3d.jpg" alt="Alfresco" width="520" height="168">
                        </div>
                        <div style="padding:4px">
                           <a href="http://www.alfresco.com/" class="title" target="new">http://www.alfresco.com</a>
                           <p>
                           Alfresco Software Inc. &copy; 2005-2008 All rights reserved. <a href="http://www.alfresco.com/legal/" target="new">Legal and License</a>
                        </div>
                        
                        <div style="padding:4px" class="mainSubTitle">
                           Alfresco Software utilises components or libraries from the following software vendors and companies
                        </div>
                        
                        <p>
                        
                        <div style="padding:4px">
                           <span style="text-align:center">
                              <a href="http://www.springframework.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/spring_logo.jpg" alt="Spring Framework" width="355" height="73" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://www.hibernate.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/hibernate_logo.gif" alt="Hibernate" width="249" height="78" border="0"></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span style="text-align:center">
                              <a href="http://www.apache.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/asf_logo_wide.gif" alt="The Apache Software Foundation" width="537" height="51" border="0"></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span style="text-align:center">
                              <a href="http://jakarta.apache.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/jakarta-logo.gif" alt="The Apache Jakarta Project" width="505" height="48" border="0"></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span style="text-align:center">
                              <a href="http://www.java.com/" target="new"><img src="<%=request.getContextPath()%>/images/logo/java.gif" alt="Java" width="52" height="108" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://www.jboss.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/jbosslogo.gif" alt="JBoss" width="249" height="78" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://myfaces.apache.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/myfaces.png" alt="My Faces" width="106" height="100" border="0"></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span style="text-align:center">
                              <a href="http://lucene.apache.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/lucene.gif" alt="Lucene" width="300" height="46" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://cglib.sourceforge.net/" target="new"><img src="<%=request.getContextPath()%>/images/logo/cglib.png" alt="Code Generation Library" width="228" height="35" border="0"></a>
                           </span>
                        </div>
                        <div style="padding:4px">
                           <span style="text-align:center">
                              <a href="http://www.pdfbox.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/pdfbox.gif" alt="PDFBox" width="200" height="38" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://tinymce.moxiecode.com/" target="new"><img src="<%=request.getContextPath()%>/images/logo/tinymce.png" alt="TinyMCE" width="88" height="32" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://www.openoffice.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/open-office.gif" alt="OpenOffice.org" width="122" height="40" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://jooreports.sourceforge.net/" target="new"><img src="<%=request.getContextPath()%>/images/logo/jooreports.png" alt="JooReports" width="125" height="24" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://freemarker.sourceforge.net/" target="new"><img src="<%=request.getContextPath()%>/images/logo/freemarker.png" alt="FreeMarker" width="165" height="26" border="0"></a>
                           </span>
                        </div>
                        
                        <p>
                        <p>
                        
                        <div style="padding:4px" class="mainSubTitle">
                           Alfresco Development tools:
                        </div>
                        
                        <div style="padding:4px">
                           <span style="text-align:center">
                              <a href="http://subversion.tigris.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/subversion_logo.png" alt="Subversion" width="200" height="27" border="0"></a>
                           </span>
                           <span style="text-align:center">
                              <a href="http://www.eclipse.org/" target="new"><img src="<%=request.getContextPath()%>/images/logo/eclipse_logo.jpg" alt="eclipse" width="143" height="49" border="0"></a>
                           </span>
                        </div>
                        
                        <p>
                        <p>
                        
                        <div style="padding:4px" class="mainSubTitle">
                           Alfresco Software contributors
                        </div>
                        
                        <table border="0" cellspacing="2" cellpadding="2">
                           <tr>
                              <td width="50%" align="right">
                                 Kofax Release Script:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width="50%">
                                 <img src="<%=request.getContextPath()%>/images/logo/ardenringcroft_logo.png" alt="Aarden Ringcroft" width="132" height="32" border="0">
                              </td>
                           </tr>
                           <tr>
                              <td width="50%" align="right">
                                 Meta Data Extraction Framework and PDF/Open Office Format meta data extraction:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width="50%">
                                 Jesper Steen M&oslash;ller
                              </td>
                           </tr>
                           <tr>
                              <td width="50%" align="right">
                                 Open Document Format meta data extraction:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width="50%">
                                 Antti Jokipii
                              </td>
                           </tr>
                           <tr>
                              <td width="50%" align="right">
                                 Multilingual Document Management:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width="50%">
                                 CEC
                              </td>
                           </tr>
                           <tr>
                              <td width="50%" align="right">
                                 Category Browsing:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width="50%">
                                 Atol Conseils et D&eacute;veloppements
                              </td>
                           </tr>
                           <tr>
                              <td width="50%" align="right">
                                 Fixes and improvements:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width="50%">
                                 Ray Gauss II,
                                 Dave Gillen,
                                 Michael Kriske,
                                 Carina Lansing,
                                 DMC.de,
                                 Optaros
                              </td>
                           </tr>
                           <tr>
                              <td width="50%" align="right">
                                 Language and translation packs:
                              </td>
                              <td>&nbsp;&#8226;&nbsp;</td>
                              <td width="50%">
                                 Camille B&eacute;gnis,
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
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_7.gif" alt="" width="4" height="4"></td>
                  <td width="100%" align="center" style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_8.gif)"></td>
                  <td><img src="<%=request.getContextPath()%>/images/parts/whitepanel_9.gif" alt="" width="4" height="4"></td>
               </tr>
               
            </table>
          </td>
       </tr>
    </table>
    
    </h:form>
    
</f:view>

</r:page>