/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.ui.wcm.component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import org.alfresco.config.JNDIConstants;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMNode;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.LinkValidationState;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.ComponentConstants;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.repo.component.UIActions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSF component that renders the results of a link validation report.
 * 
 * @author gavinc
 */
public class UILinkValidationReport extends AbstractLinkValidationReportComponent
{
   public static final String DEFAULT_INTIAL_TAB = "staticTab";
   
   private String initialTab;
   private Boolean itemsExpanded;
   private boolean oddRow = true;
   
   private static Log logger = LogFactory.getLog(UILinkValidationReport.class);
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.LinkValidationReport";
   }
   
   // ------------------------------------------------------------------------------
   // Component implementation

   @SuppressWarnings("unchecked")
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.itemsExpanded = (Boolean)values[1];
      this.initialTab = (String)values[2];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[3];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.itemsExpanded;
      values[2] = this.initialTab;
      return values;
   }
   
   @SuppressWarnings("unchecked")
   @Override
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      // get the link validation state object to get the data from
      ResourceBundle bundle = Application.getBundle(context);
      ResponseWriter out = context.getResponseWriter();
      LinkValidationState linkState = getValue();
      
      if (logger.isDebugEnabled())
         logger.debug("Rendering report from state object: " + linkState);
      
      if (linkState.getError() == null && linkState.getNumberBrokenLinks() > 0)
      {
         // determine whether the generated files and broken links sections
         // should be expanded
         boolean sectionsExpanded = this.getItemsExpanded();

         // render the required JavaScript
         String selectedTab = this.getInitialTab(); 
         out.write("<script type='text/javascript'>");
         out.write("var _alfCurrentTab = '");
         out.write(selectedTab);
         out.write("';</script>\n");
         
         out.write("<script type='text/javascript' src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/scripts/ajax/yahoo/dom/dom-min.js'></script>\n");
         
         out.write("<script type='text/javascript' src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/scripts/ajax/link-validation-report.js'></script>\n");
         
         // gather count data for tab titles
         int numStaticFiles = linkState.getStaticFilesWithBrokenLinks().size();
         int numForms = linkState.getFormsWithBrokenLinks().size();
         int numBrokenFileLinks = linkState.getNoBrokenLinksInStaticFiles();
         int numBrokenFormLinks = linkState.getNoBrokenLinksInForms();
         int numFixedItems = linkState.getNumberFixedItems();
         
         String pattern = bundle.getString("static_tab");
         String staticTabTitle = MessageFormat.format(pattern, 
                  new Object[] {numStaticFiles});
         
         pattern = bundle.getString("generated_tab");
         String generatedTabTitle = MessageFormat.format(pattern, 
                  new Object[] {numForms});
         
         // render the tabs
         out.write("<div class='tabs'><ul><li><span class='tabLabel'>");
         out.write(bundle.getString("broken"));
         out.write(":</span></li><li id='staticTab'");
         if (selectedTab.equals("staticTab"))
         {
            out.write(" class='selectedTab'");
         }
         out.write("><a href=\"");
         out.write("javascript:Alfresco.tabSelected('static');\"><span>");
         out.write(staticTabTitle);
         out.write("&nbsp;(<img class='tabTitleBrokenLinkIcon' src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/images/icons/broken_link.gif' />");
         out.write(Integer.toString(numBrokenFileLinks));
         out.write(")</span></a></li><li id='generatedTab'");
         if (selectedTab.equals("generatedTab"))
         {
            out.write(" class='selectedTab'");
         }
         out.write("><a href=\"");
         out.write("javascript:Alfresco.tabSelected('generated');\"><span>");
         out.write(generatedTabTitle);
         out.write("&nbsp;(<img class='tabTitleBrokenLinkIcon' src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/images/icons/broken_link.gif' />");
         out.write(Integer.toString(numBrokenFormLinks));
         out.write(")</span></a></li><li><span class='tabLabel'>");
         out.write(bundle.getString("fixed"));
         out.write(":</span></li><li id='fixedTab'>");
         if (selectedTab.equals("fixedTab"))
         {
            out.write(" class='selectedTab'");
         }
         out.write("<a href=\"");
         out.write("javascript:Alfresco.tabSelected('fixed');\"><span>");
         out.write(bundle.getString("all_items_tab"));
         out.write("&nbsp;(<img class='tabTitleBrokenLinkIcon' src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/images/icons/green_tick.gif' />");
         out.write(Integer.toString(numFixedItems));
         out.write(")</span></a></li>");
         out.write("<li><span class='tabButton'>");
         
         // render the update status button
         UICommand updateStatusAction = aquireUpdateStatusAction(context, 
                  "update_status_" + linkState.getStore());
         Utils.encodeRecursive(context, updateStatusAction);
         
         out.write("</span></li></ul></div>");
         
         // reset the oddRow flag
         this.oddRow = true;
         
         // render the list of broken files and their contained links
         out.write("<div id='staticTabContent'");
         if (selectedTab.equals("staticTab") == false)
         {
            out.write(" style='display: none;'");
         }
         out.write(">");
         renderTabHeader(out, context, "staticTab", true);
         out.write("<div id='staticTabBody' class='linkValTabContentBody'>");
   
         List<String> brokenFiles = linkState.getStaticFilesWithBrokenLinks();
         if (brokenFiles == null || brokenFiles.size() == 0)
         {
            renderNoItems(out, context);
         }
         else
         {
            UIActions actions = aquireFileActions("broken_file_actions", getValue().getStore());
            AVMService avmService = Repository.getServiceRegistry(context).getAVMService();
            int rootPathIndex = AVMUtil.buildSandboxRootPath(linkState.getStore()).length();
            String dns = AVMUtil.lookupStoreDNS(linkState.getStore());
            ClientConfigElement config = Application.getClientConfig(context);
            String wcmDomain = config.getWCMDomain();
            String wcmPort = config.getWCMPort();
         
            // render each broken file
            for (String file : brokenFiles)
            {
               renderBrokenFile(context, out, file, linkState, actions, avmService,
                        rootPathIndex, wcmDomain, wcmPort, dns, sectionsExpanded);
            }
         }
         
         out.write("</div></div>");
         
         // reset the oddRow flag
         this.oddRow = true;
         
         // render the list of broken forms, the files it generated and their contained links
         out.write("<div id='generatedTabContent'");
         if (selectedTab.equals("generatedTab") == false)
         {
            out.write(" style='display: none;'");
         }
         out.write(">");
         renderTabHeader(out, context, "generatedTab", true);
         out.write("<div id='generatedTabBody' class='linkValTabContentBody'>");
         
         List<String> brokenForms = linkState.getFormsWithBrokenLinks();
         if (brokenForms == null || brokenForms.size() == 0)
         {
            renderNoItems(out, context);
         }
         else
         {
            UIActions actions = aquireFileActions("broken_form_actions", getValue().getStore());
            AVMService avmService = Repository.getServiceRegistry(context).getAVMService();
            
            for (String form : brokenForms)
            {
               renderBrokenForm(context, out, form, linkState, actions, 
                        avmService, sectionsExpanded);
            }
         }
         
         out.write("</div></div>");
         
         // reset the oddRow flag
         this.oddRow = true;
         
         // render the list of fixed items
         out.write("<div id='fixedTabContent'");
         if (selectedTab.equals("fixedTab") == false)
         {
            out.write(" style='display: none;'");
         }
         out.write(">");
         renderTabHeader(out, context, "fixedTab", false);
         out.write("<div id='fixedTabBody' class='linkValTabContentBody'>");
         
         int fixedItems = 0;
         List<String> fixedFiles = linkState.getFixedFiles();
         List<String> fixedForms = linkState.getFixedForms();
         if (fixedFiles != null)
         {
            fixedItems = fixedFiles.size();
         }
         if (fixedForms != null)
         {
            fixedItems += fixedForms.size();
         }
         
         if (fixedItems == 0)
         {
            renderNoItems(out, context);
         }
         else
         {
            for (String file : fixedFiles)
            {
               renderFixedItem(context, out, file, linkState);
            }
            
            for (String file : fixedForms)
            {
               renderFixedItem(context, out, file, linkState);
            }
         }
         
         out.write("</div></div>");
      }
      else
      {
         out.write("<div>&nbsp;</div>");
      }
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return true if broken links and generated files should be expanded
    */
   public boolean getItemsExpanded()
   {
      ValueBinding vb = getValueBinding("itemsExpanded");
      if (vb != null)
      {
         this.itemsExpanded = (Boolean)vb.getValue(getFacesContext());
      }
      
      if (this.itemsExpanded == null)
      {
         this.itemsExpanded = Boolean.FALSE;
      }
      
      return this.itemsExpanded.booleanValue();
   }
   
   /**
    * @param value true if broken links and generated files should be expanded
    */
   public void setItemsExpanded(boolean value)
   {
      this.itemsExpanded = value;
   }
   
   /**
    * @return The tab that will be initially selected
    */
   public String getInitialTab()
   {
      ValueBinding vb = getValueBinding("initialTab");
      if (vb != null)
      {
         this.initialTab = (String)vb.getValue(getFacesContext());
      }
      
      if (this.initialTab == null)
      {
         this.initialTab = DEFAULT_INTIAL_TAB;
      }
      
      return this.initialTab;
   }
   
   /**
    * @param tab The initial tab to be selected
    */
   public void setItemsExpanded(String tab)
   {
      this.initialTab = tab;
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   protected void renderBrokenFile(FacesContext context, ResponseWriter out,
            String file, LinkValidationState linkState, UIActions actions,
            AVMService avmService, int rootPathIndex, String wcmDomain, 
            String wcmPort, String dns, boolean brokenLinksExpanded) 
            throws IOException
   {
      // gather the data to show for the file
      String[] nameAndPath = this.getFileNameAndPath(file);
      String fileName = nameAndPath[0];
      String filePath = nameAndPath[1];
      
      // render the row with the appropriate background style
      out.write("<div class='linkValRow ");
      
      if (this.oddRow)
      {
         out.write("linkValRowOdd");
      }
      else
      {
         out.write("linkValRowEven");
      }
      
      // toggle the type of row
      this.oddRow = !this.oddRow;
      
      // render the icon
      out.write("'><div class='linkValIcon'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write(getIcon(fileName));
      out.write("' /></div><div class='linkValActions'>");
      
      // setup the context for the actions
      AVMNodeDescriptor desc = avmService.lookup(-1, file);
      AVMNode node = new AVMNode(desc);
      
      String assetPath = file.substring(rootPathIndex);
      String previewUrl = AVMUtil.buildAssetUrl(assetPath, wcmDomain, wcmPort, dns);   
      node.getProperties().put("previewUrl", previewUrl);
      actions.setContext(node);
      
      // render the actions
      Utils.encodeRecursive(context, actions);
      
      out.write("</div>");
      
      // render the file details
      String brokenLinks = getBrokenLinks(context, file, linkState);
      int numBrokenLinks = linkState.getBrokenLinksForFile(file).size();
      renderFileDetails(out, context, fileName, filePath, brokenLinks, 
               numBrokenLinks, brokenLinksExpanded);
      
      out.write("</div>");
   }
   
   protected void renderBrokenForm(FacesContext context, ResponseWriter out,
            String file, LinkValidationState linkState, UIActions actions,
            AVMService avmService, boolean generatedFilesExpanded) throws IOException
   {  
      // get the web form name and path
      String[] formNamePath = this.getFileNameAndPath(file);
      String formName = formNamePath[0];
      String formPath = formNamePath[1];
      
      // setup the context for the actions
      AVMNodeDescriptor desc = avmService.lookup(-1, file);
      AVMNode node = new AVMNode(desc);
      actions.setContext(node);

      // generate a unique id for this form
      String formId = this.getId() + "_" + desc.getId();
      
      // render the row with the appropriate background style
      out.write("<div class='linkValRow ");
      
      if (this.oddRow)
      {
         out.write("linkValRowOdd");
      }
      else
      {
         out.write("linkValRowEven");
      }
      
      // toggle the type of row
      this.oddRow = !this.oddRow;
      
      // render the icon
      out.write("'><div class='linkValIcon'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/webform_large.gif' /></div><div class='linkValActions'>");
      
      // render the actions
      Utils.encodeRecursive(context, actions);
      
      out.write("</div>");
      
      // render the generated files
      List<String> brokenFiles = linkState.getBrokenFilesByForm(file);
      
      out.write("<div class='linkValItemDetails'><div class='linkValFormName'>");
      out.write(formName);
      out.write("</div><div class='linkValFormPath'>");
      out.write(formPath);
      out.write("</div><div class='linkValToggle'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      if (generatedFilesExpanded)
      {
         out.write("/images/icons/arrow_open.gif' class='linkValToggleExpanded' ");
      }
      else
      {
         out.write("/images/icons/arrow_closed.gif' class='linkValToggleCollapsed' ");
      }
      out.write("onclick='Alfresco.toggleGeneratedFiles(this, \"");
      out.write(formId);
      out.write("\");return false;' />");
      out.write(Application.getMessage(context, "generated_files"));
      out.write("&nbsp;(");
      out.write(Integer.toString(brokenFiles.size()));
      out.write(")</div><div id='");
      out.write(formId);
      out.write("'");
      if (generatedFilesExpanded == false)
      {
         out.write(" style='display: none;'");
      }
      out.write(">");
      
      for (String brokenFile : brokenFiles)
      {
         String[] nameAndPath = this.getFileNameAndPath(brokenFile);
         String fileName = nameAndPath[0];
         String filePath = nameAndPath[1];
         
         out.write("<div><div class='linkValIcon'><img src='");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write(getIcon(fileName));
         out.write("' /></div>");
   
         // build the list of broken links for the file
         String brokenLinks = getBrokenLinks(context, brokenFile, linkState);
         int numBrokenLinks = linkState.getBrokenLinksForFile(brokenFile).size();
         renderFileDetails(out, context, fileName, filePath, brokenLinks,
                  numBrokenLinks, generatedFilesExpanded);
         
         out.write("</div>");
      }
      
      out.write("</div></div></div>");
   }
   
   protected void renderFixedItem(FacesContext context, ResponseWriter out,
            String file, LinkValidationState linkState) throws IOException
   {
      // gather the data to show for the file
      String[] nameAndPath = this.getFileNameAndPath(file);
      String fileName = nameAndPath[0];
      String filePath = nameAndPath[1];
      
      // render the row with the appropriate background style
      out.write("<div class='linkValRow ");
      
      if (this.oddRow)
      {
         out.write("linkValRowOdd");
      }
      else
      {
         out.write("linkValRowEven");
      }
      
      // toggle the type of row
      this.oddRow = !this.oddRow;
      
      out.write("'><div class='linkValIcon'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write(getIcon(fileName));
      out.write("' /></div>");
      
      // render the file details
      renderFileDetails(out, context, fileName, filePath, null, 0, false);
      out.write("</div>");
   }
   
   /**
    * Returns the name and path for the given avm path
    * 
    * @param avmPath The path to split
    * @return A String array with the name in the first position and the path in the
    *         second position.
    */
   protected String[] getFileNameAndPath(String avmPath)
   {
      String fileName = avmPath;
      String filePath = avmPath;
      
      int idx = avmPath.lastIndexOf("/");
      if (idx != -1)
      {
         fileName = avmPath.substring(idx+1);
         
         int appbaseIdx = avmPath.indexOf(JNDIConstants.DIR_DEFAULT_APPBASE);
         if (appbaseIdx != -1)
         {
            filePath = avmPath.substring(appbaseIdx+JNDIConstants.DIR_DEFAULT_APPBASE.length(), idx);
         }
         else
         {
            filePath = avmPath.substring(0, idx);
         }
      }
      
      return new String[] {fileName, filePath};
   }
   
   /**
    * Constructs a comma separated list of broken links for the given avm path
    * 
    * @param avmPath The avm path to get the broken links for
    * @param linkState The current link valiation state
    * @return Comma separated list of broken links
    */
   protected String getBrokenLinks(FacesContext context, String avmPath, LinkValidationState linkState)
   {
      List<String> brokenLinks = linkState.getBrokenLinksForFile(avmPath);
      StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (String link : brokenLinks)
      {
         if (first == false)
         {
            builder.append("<br/>");
         }
         else
         {
            first = false;
         }
         
         builder.append("<img src='");
         builder.append(context.getExternalContext().getRequestContextPath());
         builder.append("/images/icons/broken_link.gif' style='vertical-align: -4px;' />");
         builder.append(parseBrokenLink(link));
      }
      
      return builder.toString();
   }
   
   /**
    * Removes the virtaulisation server host name from the link if appropriate
    * 
    * @param linkUrl The URL that is broken
    * @return Parsed URL
    */
   protected String parseBrokenLink(String linkUrl)
   {
      String link = linkUrl;
      
      if (linkUrl.startsWith("http://") && linkUrl.indexOf("www--sandbox") != -1)
      {
         // remove the virtualisation server host name
         int idx = linkUrl.indexOf("/", 7);
         if (idx != -1)
         {
            link = linkUrl.substring(idx);
         }
      }
      
      // truncate the link if it is longer than 60 chars
      String title = link;
      if (link.length() > 65)
      {
         link = link.substring(0, 30) + "&nbsp;...&nbsp;" + 
                link.substring(link.length()-30);
      }
      
      return "<span title='" + title + "'>&nbsp;" + link + "</span>";
   }
   
   protected void renderTabHeader(ResponseWriter out, FacesContext context,
            String tabId, boolean showExpandCollapseControls) throws IOException
   {
      out.write("<div class='linkValTabContentHeader'><div>");
      out.write("<div class='expandCollapseControls'>");

      if (showExpandCollapseControls)
      {
         if (this.getItemsExpanded())
         {
            UICommand collapseAllAction = aquireCollapseAllAction(context, tabId);
            Utils.encodeRecursive(context, collapseAllAction);
         }
         else
         {
            UICommand expandAllAction = aquireExpandAllAction(context, tabId);
            Utils.encodeRecursive(context, expandAllAction);
         }
      }
      else
      {
         out.write("&nbsp;");
      }
      
      out.write("</div><div class='incDecControls'><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/dec_tab_size.gif' onclick='Alfresco.decreaseTabSize(\"");
      out.write(tabId + "Body");
      out.write("\");' title='");
      out.write(Application.getMessage(context, "dec_tab_size"));
      out.write("'/>&nbsp;<img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/inc_tab_size.gif' onclick='Alfresco.increaseTabSize(\"");
      out.write(tabId + "Body");
      out.write("\");' title='");
      out.write(Application.getMessage(context, "inc_tab_size"));
      out.write("'/></div></div></div>");
   }
   
   protected void renderFileDetails(ResponseWriter out, FacesContext context,
            String fileName, String filePath, String brokenLinks,
            int numBrokenLinks, boolean brokenLinksExpanded) throws IOException
   {
      // generate a unique id for the file
      String fileId = "file" + Integer.toString((filePath + fileName).hashCode());
      
      out.write("<div class='linkValItemDetails'><div class='linkValFileName'>");
      out.write(fileName);
      out.write("</div><div class='linkValFilePath'>");
      out.write(filePath);
      out.write("</div>");
      
      if (brokenLinks != null && brokenLinks.length() > 0)
      {
         out.write("<div class='linkValToggle'><img src='");
         out.write(context.getExternalContext().getRequestContextPath());
         if (brokenLinksExpanded)
         {
            out.write("/images/icons/arrow_open.gif' class='linkValToggleExpanded' ");
         }
         else
         {
            out.write("/images/icons/arrow_closed.gif' class='linkValToggleCollapsed' ");
         }
         out.write("onclick='Alfresco.toggleBrokenLinks(this, \"");
         out.write(fileId);
         out.write("\");return false;' />");
         out.write(Application.getMessage(context, "broken_links"));
         out.write("&nbsp;(");
         out.write(Integer.toString(numBrokenLinks));
         out.write(")</div>");
         
         out.write("<div id='");
         out.write(fileId);
         out.write("' class='linkValBrokenLinks'");
         if (brokenLinksExpanded == false)
         {
            out.write(" style='display: none;'");
         }
         out.write(">");
         out.write(brokenLinks);
         out.write("</div>");
      }
      
      out.write("</div>");
   }
   
   /**
    * Renders the "No items to display" message
    * 
    * @param out ResponseWriter instance to write to
    * @param context FacesContext
    * @throws IOException
    */
   protected void renderNoItems(ResponseWriter out, FacesContext context)
      throws IOException
   {
      out.write("<div class='linkValNoItems'>");
      out.write(Application.getMessage(context, "no_items"));
      out.write("</div>");
   }
   
   /**
    * Returns the icon to use given a file name
    * 
    * @param fileName File name to find an icon for
    * @return The path to the icon to use
    */
   protected String getIcon(String fileName)
   {
      // work out what icon to use
      String icon = "/images/filetypes32/_default.gif";
      String ext = "";
      int idx = fileName.indexOf(".");
      if (idx != -1)
      {
         ext = fileName.substring(idx);
      }
      
      if (ext.equals(".html") || ext.equals(".htm"))
      {
         icon = "/images/filetypes32/html.gif";
      }
      else if (ext.equals(".xml"))
      {
         icon = "/images/icons/webform_large.gif";
      }
      
      return icon;
   }
   
   /**
    * Aquire the UIActions component for the specified action group ID.
    * Search for the component in the child list or create as needed. 
    * 
    * @param id      ActionGroup id of the UIActions component
    * 
    * @return UIActions component
    */
   @SuppressWarnings("unchecked")
   protected UIActions aquireFileActions(String id, String store)
   {
      UIActions uiActions = null;
      String componentId = id + '_' + store;
      
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (componentId.equals(component.getId()))
         {
            uiActions = (UIActions)component;
            break;
         }
      }
      
      if (uiActions == null)
      {
         javax.faces.application.Application facesApp = FacesContext.getCurrentInstance().getApplication();
         uiActions = (UIActions)facesApp.createComponent("org.alfresco.faces.Actions");
         uiActions.setShowLink(false);
         uiActions.getAttributes().put("styleClass", "inlineAction");
         uiActions.setId(componentId);
         uiActions.setParent(this);
         uiActions.setValue(id);
         
         this.getChildren().add(uiActions);
      }
      
      return uiActions;
   }
   
   @SuppressWarnings("unchecked")
   protected UICommand aquireUpdateStatusAction(FacesContext context, String actionId)
   {
      UICommand action = null;
      
      // try find the action as a child of this component
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UICommand)component;
            break;
         }
      }
      
      if (action == null)
      {
         // create the action and add as a child component
         javax.faces.application.Application facesApp = context.getApplication();
         action = (UICommand)facesApp.createComponent(UICommand.COMPONENT_TYPE);
         action.setId(actionId);
         action.setValue(Application.getMessage(context, "update_status"));
         MethodBinding binding = facesApp.createMethodBinding("#{DialogManager.bean.updateStatus}", 
                  new Class[] {});
         action.setAction(binding);
         this.getChildren().add(action);
      }
      
      return action;
   }
   
   @SuppressWarnings("unchecked")
   protected UIActionLink aquireExpandAllAction(FacesContext context, String tabId)
   {
      UIActionLink action = null;
      String actionId = "expand_" + tabId;
      
      // try find the action as a child of this component
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            break;
         }
      }
      
      if (action == null)
      {
         // create the action and add as a child component
         javax.faces.application.Application facesApp = context.getApplication();
         action = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
         action.setId(actionId);
         action.setValue(Application.getMessage(context, "expand_all"));
         MethodBinding binding = facesApp.createMethodBinding("#{DialogManager.bean.toggleSections}", 
                  new Class[] {javax.faces.event.ActionEvent.class});
         action.setActionListener(binding);
         
         // add a parameter to indicate what tab is being expanded
         UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
         param.setId(actionId + "_param");
         param.setName("tab");
         param.setValue(tabId);
         action.getChildren().add(param);
         
         this.getChildren().add(action);
      }
      
      return action;
   }
   
   @SuppressWarnings("unchecked")
   protected UIActionLink aquireCollapseAllAction(FacesContext context, String tabId)
   {
      UIActionLink action = null;
      String actionId = "collapse_" + tabId;
      
      // try find the action as a child of this component
      for (UIComponent component : (List<UIComponent>)getChildren())
      {
         if (actionId.equals(component.getId()))
         {
            action = (UIActionLink)component;
            break;
         }
      }
      
      if (action == null)
      {
         // create the action and add as a child component
         javax.faces.application.Application facesApp = context.getApplication();
         action = (UIActionLink)facesApp.createComponent(UIActions.COMPONENT_ACTIONLINK);
         action.setId(actionId);
         action.setValue(Application.getMessage(context, "collapse_all"));
         MethodBinding binding = facesApp.createMethodBinding("#{DialogManager.bean.toggleSections}", 
                  new Class[] {javax.faces.event.ActionEvent.class});
         action.setActionListener(binding);
         
         // add a parameter to indicate what tab is being expanded
         UIParameter param = (UIParameter)facesApp.createComponent(ComponentConstants.JAVAX_FACES_PARAMETER);
         param.setId(actionId + "_param");
         param.setName("tab");
         param.setValue(tabId);
         action.getChildren().add(param);
         
         this.getChildren().add(action);
      }
      
      return action;
   }
}




