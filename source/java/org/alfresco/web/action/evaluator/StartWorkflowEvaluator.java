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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Node;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * UI Action Evaluator - Start workflow on a node.
 * 
 * @author gavinc
 */
public class StartWorkflowEvaluator extends BaseActionEvaluator
{
   private static final String BPM_ENGINE_BEAN_NAME = "bpm_engineRegistry";
   private static final long serialVersionUID = 3110333488835027710L;

   /**
    * @see org.alfresco.web.action.ActionEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
    */
   public boolean evaluate(Node node)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      NavigationBean nav =
         (NavigationBean)FacesHelper.getManagedBean(facesContext, NavigationBean.BEAN_NAME);
      
      // determine whether the workflow services are active
      boolean workflowPresent = false;
      WebApplicationContext springContext = FacesContextUtils.getRequiredWebApplicationContext(facesContext);
      BPMEngineRegistry bpmReg = (BPMEngineRegistry)springContext.getBean(BPM_ENGINE_BEAN_NAME);
      if (bpmReg != null)
      {
         String[] components = bpmReg.getWorkflowComponents();
         workflowPresent = (components != null && components.length > 0);
      }
      
      return (workflowPresent && nav.getIsGuest() == false && 
              node.hasAspect(ContentModel.ASPECT_MULTILINGUAL_EMPTY_TRANSLATION) == false);
   }
}
