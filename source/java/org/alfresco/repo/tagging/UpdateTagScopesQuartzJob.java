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
package org.alfresco.repo.tagging;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Finds Tag Scope updates that haven't been applied, and triggers
 *  then to be run.
 * Works with the {@link UpdateTagScopesActionExecuter}, and is typically
 *  run after server restart.
 * Also refer to scheduled-jobs-context.xml for more information
 */
public class UpdateTagScopesQuartzJob implements Job {
   private static Log logger = LogFactory.getLog(UpdateTagScopesQuartzJob.class);
   
   public UpdateTagScopesQuartzJob() {}

   /**
    * Finds the tag scopes to be updated, and has them worked on
    */
   public void execute(JobExecutionContext context) throws JobExecutionException
   {
       JobDataMap jobData = context.getJobDetail().getJobDataMap();
       
       // Extract out our beans
       Object actionServiceO = jobData.get("actionService");
       if(actionServiceO == null || !(actionServiceO instanceof ActionService))
       {
          throw new AlfrescoRuntimeException(
                "UpdateTagScopesQuartzJob data must contain a valid 'actionService' reference");
       }
       
       Object updateTagsActionO = jobData.get("updateTagsAction");
       if(updateTagsActionO == null || !(updateTagsActionO instanceof UpdateTagScopesActionExecuter))
       {
          throw new AlfrescoRuntimeException(
                "UpdateTagScopesQuartzJob data must contain a valid 'updateTagsAction' reference");
       }
       
       ActionService actionService = (ActionService)actionServiceO;
       UpdateTagScopesActionExecuter updateTagsAction = (UpdateTagScopesActionExecuter)updateTagsActionO;
       
       // Do the work
       execute(actionService, updateTagsAction);
   }

   protected void execute(final ActionService actionService, final UpdateTagScopesActionExecuter updateTagsAction)
   {
       // Process
       final ArrayList<NodeRef> tagNodes = new ArrayList<NodeRef>();
       final HashSet<NodeRef> handledTagNodes = new HashSet<NodeRef>();
       
       while(true)
       {
          // Fetch a batch of the pending changes
          AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Void>() 
             {
                public Void doWork() throws Exception
                {
                   tagNodes.clear();
                   tagNodes.addAll(
                         updateTagsAction.searchForTagScopesPendingUpdates()
                   );
                   return null;
                }
             }, AuthenticationUtil.getSystemUserName()
          );
          
          // If we're on our 2nd loop round for any of them, then skip them from now on
          // (This can happen if another thread is already processing one of them)
          Iterator<NodeRef> it = tagNodes.iterator();
          while(it.hasNext())
          {
             NodeRef nodeRef = it.next();
             if(handledTagNodes.contains(nodeRef))
             {
                it.remove();
                if(logger.isDebugEnabled())
                   logger.debug("Tag scope " + nodeRef + " must be being processed by another Thread, not updating it");
             }
          }
          
          // Log what we found to process
          if(logger.isDebugEnabled())
          {
             logger.debug("Checked for tag scopes with pending tag updates, found " + tagNodes);
          }
          
          // If we're out of tag scopes, stop!
          if(tagNodes.size() == 0)
             break;
          
          // Have the action run for these tag scope nodes
          // Needs to run synchronously
          Action action = actionService.createAction(UpdateTagScopesActionExecuter.NAME);
          action.setParameterValue(UpdateTagScopesActionExecuter.PARAM_TAG_SCOPES, (Serializable)tagNodes); 
          actionService.executeAction(action, null, false, false);
          
          // Record the scopes we've just done
          handledTagNodes.addAll(tagNodes);
       }
   }
}
