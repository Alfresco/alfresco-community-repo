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
package org.alfresco.web.bean.content;

import java.io.File;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.model.ContentModel;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.forms.Form;
import org.alfresco.web.forms.FormNotFoundException;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean implementation for the "Edit Content Wizard" dialog
 */
public class EditContentWizard extends CreateContentWizard
{
   private NodeRef nodeRef;
   private Form form;

   // ------------------------------------------------------------------------------
   // Wizard implementation
   
   @Override
   public void init(final Map<String, String> parameters)
   {
      // TODO - currently assumes this is form content
      super.init(parameters);
      Node node = this.navigator.getDispatchContextNode();
      if (node == null)
      {
         throw new IllegalArgumentException("Edit Form wizard requires action node context.");
      }
      this.nodeRef = node.getNodeRef();
      try
      {
         formName = (String)getNodeService().getProperty(nodeRef, WCMAppModel.PROP_PARENT_FORM_NAME); // getFormName() ...
         form = formsService.getForm(this.formName);
      }
      catch (FormNotFoundException fnfe)
      {
         Utils.addErrorMessage(fnfe.getMessage(), fnfe);
         throw new IllegalArgumentException(fnfe);
      }

      this.content = this.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT).getContentString();
      
      this.fileName = (String)getNodeService().getProperty(nodeRef, ContentModel.PROP_NAME); // getName() ...
      this.mimeType = MimetypeMap.MIMETYPE_XML;
   }

   @Override
   public String back()
   {
      return super.back();
   }
   
   @Override
   protected void saveContent(File fileContent, String strContent) throws Exception
   {
      ContentWriter writer = getContentService().getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
      writer.putContent(strContent);
   }
   
   @Override
   protected String doPostCommitProcessing(FacesContext context, String outcome)
   {
      return outcome;
   }

   @Override 
   public Form getForm()
   {
      return this.form;
   }
}
