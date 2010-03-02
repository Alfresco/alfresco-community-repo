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
 * No check for deleted items is made in this evaluator. @see WCMWorkflowEvaluator
 * 
 * @author Kevin Roast
 */
public class WCMWorkflowDeletedEvaluator extends WCMLockEvaluator
{
   private static final long serialVersionUID = -4341942166433855200L;
   
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
            
            // evaluate to true if we are within a workflow store (i.e. list of resources in the task
            // dialog) or not part of an already in-progress workflow
            proceed = (AVMUtil.isWorkflowStore(sandbox) ||
                       !((AVMNode)node).isInActiveWorkflow(sandbox));
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