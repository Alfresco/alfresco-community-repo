/*
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
 */
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wcm.AVMBrowseBean;
import org.alfresco.web.bean.wcm.AVMNode;
import org.alfresco.web.bean.wcm.AVMUtil;
import org.alfresco.web.bean.wcm.WebProject;

/**
 * UI Action Evaluator - return true if the node is not part of an in-progress WCM workflow.
 * 
 * @author Kevin Roast
 */
public class WCMWorkflowEvaluator extends WCMLockEvaluator
{
   private static final long serialVersionUID = -5847066921917855781L;
   
   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(final Node node)
   {
      boolean proceed = false;
      if (super.evaluate(node))
      {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final AVMBrowseBean avmBrowseBean = (AVMBrowseBean)FacesHelper.getManagedBean(fc, AVMBrowseBean.BEAN_NAME);
         
         WebProject webProject = avmBrowseBean.getWebProject();
         if (webProject == null || webProject.hasWorkflow())
         {
            String sandbox = AVMUtil.getStoreName(node.getPath());
            
            proceed = ((AVMUtil.isWorkflowStore(sandbox) ||
                    !((AVMNode)node).isInActiveWorkflow(sandbox)) &&
                    !((AVMNode)node).isDeleted());
         }
         else
         {
            // if the WebProject has no workflow then we can proceed without checking further
            proceed = true;
         }
      }
      return proceed;
   }
}