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
<%@ page import="org.alfresco.web.bean.*,
                 org.alfresco.service.cmr.repository.*,
		 org.alfresco.web.bean.content.*,
		 org.alfresco.web.templating.*" %>
<%@ page import="java.io.*" %>
<%@ page import="org.alfresco.web.app.Application" %>
<%@ page import="org.alfresco.web.templating.*" %>
<%@ page import="org.w3c.dom.Document" %>
<%
final CheckinCheckoutBean ccb = (CheckinCheckoutBean)
      session.getAttribute("CheckinCheckoutBean");
NodeRef nr = ccb.getDocument().getNodeRef();
String ttName = (String)ccb.getNodeService().getProperty(nr, TemplatingService.TT_QNAME);
final TemplatingService ts = TemplatingService.getInstance();
final TemplateType tt  = ts.getTemplateType(ttName);
TemplateInputMethod tim = tt.getInputMethods().get(0);
final InstanceData instanceData = new InstanceData() {

    public Document getContent()
    { 
        try
	{
            return ccb.getEditorOutput() != null ? ts.parseXML(ccb.getEditorOutput()) : null;
        }
	catch (Exception e)
	{
	    e.printStackTrace();
	    return null;
	}
    }
    
    public void setContent(final Document d)
    {
        ccb.setEditorOutput(ts.writeXMLToString(d));
    }
};
%>

<r:page titleId="title_edit_text_inline">

<f:view>
   
   <%-- load a bundle of properties with I18N strings --%>
   <f:loadBundle basename="alfresco.messages.webclient" var="msg"/>
   
   <h:form acceptCharset="UTF-8" id="edit-file">
   
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
         <td width="100%" height="100%">
            <table cellspacing="0" cellpadding="0" width="100%" height="100%">
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
                              <h:graphicImage id="wizard-logo" url="/images/icons/edit_large.gif" />
                           </td>
                           <td>
                              <div class="mainTitle">'<h:outputText value="#{CheckinCheckoutBean.document.name}" />'</div>
                              <div class="mainSubText"><h:outputText value="#{msg.editfileinline_description}" /></div>
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
                  <td height="100%">
                     <table cellspacing="0" cellpadding="3" border="0" width="100%" height="100%">
                        <tr>
                           <td width="100%" valign="top">
                              <%-- Hide the checkout info if this document is already checked out --%>
                              <a:panel id="checkout-panel" rendered="#{CheckinCheckoutBean.document.properties.workingCopy == false}">
                                 <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "yellowInner", "#ffffcc"); %>
                                 <table cellpadding="0" cellspacing="0" border="0" width="100%">
                                    <tr>
                                       <td valign=top style="padding-top:2px" width=20><h:graphicImage url="/images/icons/info_icon.gif" width="16" height="16"/></td>
                                       <td><td class="mainSubText">
                                             <h:outputText value="#{msg.you_may_want}" />
                                             <a:actionLink value="#{msg.checkout_document}" actionListener="#{CheckinCheckoutBean.setupContentAction}" action="checkoutFile">
                                                <f:param name="id" value="#{CheckinCheckoutBean.document.id}" />
                                             </a:actionLink>
                                             <h:outputText value="#{msg.checkout_want_to}" />
                                             <br>
                                             <h:outputText value="#{msg.checkout_warn}" />
                                          </td>
                                       </td>
                                    </tr>
                                 </table>
                                 <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "yellowInner"); %>
                              </a:panel>
                           </td>
                           
                           <td valign="top" rowspan=2>
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "lbgrey", "white"); %>
                              <table cellpadding="1" cellspacing="1" border="0">
                                 <tr>
                                    <td align="center">
                                       <h:commandButton id="save-button" value="#{msg.save}" action="#{CheckinCheckoutBean.editInlineOK}" styleClass="dialogControls" />
                                    </td>
                                 </tr>
                                 <tr><td class="dialogButtonSpacing"></td></tr>
                                 <tr>
                                    <td align="center">
                                       <h:commandButton value="#{msg.cancel}" action="browse" styleClass="dialogControls" />
                                    </td>
                                 </tr>
                              </table>
                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "lbgrey"); %>
                           </td>
                        </tr>
                        
                        <%-- Inline editor --%>
                        <tr>
                           <td width="100%" valign="top" height="100%">
                              <% PanelGenerator.generatePanelStart(out, request.getContextPath(), "white", "white"); %>
<% tim.generate(instanceData, tt, out); %>

                              <% PanelGenerator.generatePanelEnd(out, request.getContextPath(), "white"); %>
                           </td>
                        </tr>
                     </table>
                  </td>
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_6.gif)" width="4"></td>
               </tr>
               
               <%-- Error Messages --%>
               <tr valign="top">
                  <td style="background-image: url(<%=request.getContextPath()%>/images/parts/whitepanel_4.gif)" width="4"></td>
                  <td>
                     <%-- messages tag to show messages not handled by other specific message tags --%>
                     <h:messages globalOnly="true" styleClass="errorMessage" layout="table" />
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
<script type="text/javascript">
dojo.addOnLoad(function()
{
  addSubmitHandlerToButton(document.getElementById("edit-file:save-button"));
});
	
</script>

</r:page>
