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
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wcm.AVMNode;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.LinkValidationState;
import org.alfresco.web.config.ClientConfigElement;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.repo.component.UIActions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JSF component that shows the broken file information for a link
 * validation report.
 * 
 * @author gavinc
 */
public class UILinkValidationBrokenFiles extends AbstractLinkValidationReportComponent
{
   private boolean oddRow = true;
   
   private static Log logger = LogFactory.getLog(UILinkValidationBrokenFiles.class);
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.LinkValidationBrokenFiles";
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
      ResponseWriter out = context.getResponseWriter();
      LinkValidationState linkState = getValue();
      
      if (logger.isDebugEnabled())
         logger.debug("Rendering broken files from state object: " + linkState);
      
      // render the list of broken files and their contained links
      out.write("<div class='linkValidationBrokenFilesPanel'><div class='linkValidationReportTitle'>");
      out.write(Application.getMessage(context, "files_with_broken_links"));
      out.write("</div><div class='linkValidationList'><table width='100%' cellpadding='0' cellspacing='0'>");

      List<String> brokenFiles = linkState.getStaticFilesWithBrokenLinks();
      if (brokenFiles == null || brokenFiles.size() == 0)
      {
         renderNoItems(out, context);
      }
      else
      {
         UIActions actions = aquireUIActions("broken_file_actions", getValue().getStore());
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
                     rootPathIndex, wcmDomain, wcmPort, dns);
         }
      }
      
      out.write("</table></div></div>");
   }
   
   // ------------------------------------------------------------------------------
   // Helpers
   
   private void renderBrokenFile(FacesContext context, ResponseWriter out,
            String file, LinkValidationState linkState, UIActions actions,
            AVMService avmService, int rootPathIndex, String wcmDomain, 
            String wcmPort, String dns) 
            throws IOException
   {
      // gather the data to show for the file
      String[] nameAndPath = this.getFileNameAndPath(file);
      String fileName = nameAndPath[0];
      String filePath = nameAndPath[1];

      // build the list of broken links for the file
      String brokenLinks = getBrokenLinks(file, linkState);
      
      // render the row with the appropriate background style
      out.write("<tr class='");
      
      if (this.oddRow)
      {
         out.write("linkValidationListOddRow");
      }
      else
      {
         out.write("linkValidationListEvenRow");
      }
      
      // toggle the type of row
      this.oddRow = !this.oddRow;
      
      // render the data
      out.write("'><td>");
      renderFile(out, context, fileName, filePath, brokenLinks);
      out.write("</td><td align='right' valign='top'><div style='white-space: nowrap; padding-top: 10px; padding-right: 20px;'>");
      
      // setup the context for the actions
      AVMNodeDescriptor desc = avmService.lookup(-1, file);
      AVMNode node = new AVMNode(desc);
      
      String assetPath = file.substring(rootPathIndex);
      String previewUrl = AVMUtil.buildAssetUrl(assetPath, wcmDomain, wcmPort, dns);   
      node.getProperties().put("previewUrl", previewUrl);
      actions.setContext(node);
      
      // render the actions
      Utils.encodeRecursive(context, actions);
      
      out.write("</div></td></tr>");
   }
}




