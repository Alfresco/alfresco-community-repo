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

<%@ page buffer="32kb" contentType="text/html;charset=UTF-8" %>
<%@ page isELIgnored="false" %>
<%@ page import="org.alfresco.web.ui.common.PanelGenerator" %>

<f:verbatim>
                     <table cellspacing="0" cellpadding="0" border="0" width="100%">
                     <tr>
                     <td width="100%" align="center">
                     
                        <div style="padding:8px">
                           <img src="<%=request.getContextPath()%>/images/logo/alfresco3d.jpg" alt="Alfresco" width="520" height="168">
                        </div>
                        <div style="padding:4px">
                           <a href="http://www.alfresco.com/" class="title" target="new">http://www.alfresco.com</a>
                           <p>
                           Alfresco Software Inc. &copy; 2005-2011 All rights reserved. <a href="http://www.alfresco.com/legal/" target="new">Legal and License</a>
                        </div>
                        
                        <div style="padding:4px" class="mainSubTitle">
                           Alfresco Software utilises components or libraries from the following software vendors and companies
                        </div>
                        
                        <p>&nbsp;</p>
                        
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
                        
                        <p>&nbsp;</p>
                        <p>&nbsp;</p>
                        
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
                        
                        <p>&nbsp;</p>
                        <p>&nbsp;</p>
                        
                        <div style="padding:4px" class="mainSubTitle">
                           Alfresco Software contributors
                        </div>
                        
                        <table border="0" cellspacing="2" cellpadding="2">
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
                     </tr>
                    </table>
</f:verbatim>                     
               