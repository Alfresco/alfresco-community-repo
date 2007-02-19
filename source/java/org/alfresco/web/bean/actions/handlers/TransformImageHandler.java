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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.bean.actions.handlers;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ImageTransformActionExecuter;
import org.alfresco.repo.action.executer.TransformActionExecuter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.actions.BaseActionWizard;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler for the "transform-image" action.
 * 
 * @author gavinc
 */
public class TransformImageHandler extends BaseActionHandler
{
   protected static final String PROP_IMAGE_TRANSFORMER = "imageTransformer";
   protected static final String PROP_TRANSFORM_OPTIONS = "transformOptions";
   
   public String getJSPPath()
   {
      return getJSPPath(ImageTransformActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // add the transformer to use
      repoProps.put(ImageTransformActionExecuter.PARAM_MIME_TYPE,
            actionProps.get(PROP_IMAGE_TRANSFORMER));
      
      // add the options
      repoProps.put(ImageTransformActionExecuter.PARAM_CONVERT_COMMAND, 
            actionProps.get(PROP_TRANSFORM_OPTIONS));
      
      // add the destination space id to the action properties
      NodeRef destNodeRef = (NodeRef)actionProps.get(PROP_DESTINATION);
      repoProps.put(ImageTransformActionExecuter.PARAM_DESTINATION_FOLDER, destNodeRef);
      
      // add the type and name of the association to create when the copy
      // is performed
      repoProps.put(TransformActionExecuter.PARAM_ASSOC_TYPE_QNAME, 
            ContentModel.ASSOC_CONTAINS);
      repoProps.put(TransformActionExecuter.PARAM_ASSOC_QNAME, 
            QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "copy"));
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      String transformer = (String)repoProps.get(TransformActionExecuter.PARAM_MIME_TYPE);
      actionProps.put(PROP_IMAGE_TRANSFORMER, transformer);
      
      String options = (String)repoProps.get(ImageTransformActionExecuter.PARAM_CONVERT_COMMAND);
      actionProps.put(PROP_TRANSFORM_OPTIONS, options != null ? options : "");
      
      NodeRef destNodeRef = (NodeRef)repoProps.get(ImageTransformActionExecuter.PARAM_DESTINATION_FOLDER);
      actionProps.put(PROP_DESTINATION, destNodeRef);
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      String label = null;
      NodeRef space = (NodeRef)actionProps.get(PROP_DESTINATION);
      String name = Repository.getNameForNode(
            Repository.getServiceRegistry(context).getNodeService(), space);
      String transformer = (String)actionProps.get(PROP_IMAGE_TRANSFORMER);
      String option = (String)actionProps.get(PROP_TRANSFORM_OPTIONS);
      
      // find the label used by looking through the SelectItem list
      for (SelectItem item : ((BaseActionWizard)wizard).getImageTransformers())
      {
         if (item.getValue().equals(transformer))
         {
            label = item.getLabel();
            break;
         }
      }
      
      return MessageFormat.format(Application.getMessage(context, "action_transform_image"),
            new Object[] {name, label, option});
   }
}
