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

import org.alfresco.repo.action.executer.ScriptActionExecuter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.wizard.IWizardBean;

/**
 * Action handler for the "script" action.
 * 
 * @author gavinc
 */
public class ScriptHandler extends BaseActionHandler
{
   protected static final String PROP_SCRIPT = "script";
   
   public String getJSPPath()
   {
      return getJSPPath(ScriptActionExecuter.NAME);
   }

   public void prepareForSave(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      // add the selected script noderef to the action properties
      String id = (String)actionProps.get(PROP_SCRIPT);
      NodeRef scriptRef = new NodeRef(Repository.getStoreRef(), id);
      repoProps.put(ScriptActionExecuter.PARAM_SCRIPTREF, scriptRef);
   }

   public void prepareForEdit(Map<String, Serializable> actionProps,
         Map<String, Serializable> repoProps)
   {
      NodeRef scriptRef = (NodeRef)repoProps.get(ScriptActionExecuter.PARAM_SCRIPTREF);
      actionProps.put(PROP_SCRIPT, scriptRef.getId());
   }

   public String generateSummary(FacesContext context, IWizardBean wizard,
         Map<String, Serializable> actionProps)
   {
      String id = (String)actionProps.get(PROP_SCRIPT);
      NodeRef scriptRef = new NodeRef(Repository.getStoreRef(), id);
      String scriptName = Repository.getNameForNode(
            Repository.getServiceRegistry(context).getNodeService(), scriptRef);
      
      return MessageFormat.format(Application.getMessage(context, "action_script"),
            new Object[] {scriptName});
   }
}
