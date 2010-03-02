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
package org.alfresco.web.bean.wcm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;

import org.springframework.extensions.config.ConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.wcm.util.WCMUtil;
import org.alfresco.wcm.util.WCMWorkflowUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.WorkflowUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * AVM Specific workflow related helper methods.
 * 
 * @author Ariel Backenroth
 * @author Kevin Roast
 */
public class AVMWorkflowUtil extends WorkflowUtil
{
   private static final Log logger = LogFactory.getLog(AVMWorkflowUtil.class);
   
   private static final String PATH_CACHE = "_alf_sandbox_path_cache";

   // cached configured lists
   private static List<WorkflowDefinition> configuredWorkflowDefs = null;

   /**
    * Serialize the workflow params to a content stream
    * 
    * @param params             Serializable workflow params
    * @param workflowRef        The noderef to write the property too
    */
   public static void serializeWorkflowParams(Serializable params, NodeRef workflowRef)
   {
      try
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(params);
         oos.close();
         // write the serialized Map as a binary content stream - like database blob!
         ContentService cs = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentService();
         ContentWriter writer = cs.getWriter(workflowRef, 
                                             WCMAppModel.PROP_WORKFLOW_DEFAULT_PROPERTIES, 
                                             true);
         writer.setMimetype(MimetypeMap.MIMETYPE_BINARY);
         writer.putContent(new ByteArrayInputStream(baos.toByteArray()));
      }
      catch (IOException ioerr)
      {
         throw new AlfrescoRuntimeException("Unable to serialize workflow default parameters: " + ioerr.getMessage());
      }
   }
   
   /**
    * Deserialize the default workflow params from a content stream
    * 
    * @param workflowRef        The noderef to write the property too
    * 
    * @return Serializable workflow params
    */
   public static Serializable deserializeWorkflowParams(NodeRef workflowRef)
   {
      try
      {
         // restore the serialized Map from a binary content stream - like database blob!
         Serializable params = null;
         ContentService cs = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getContentService();
         ContentReader reader = cs.getReader(workflowRef, WCMAppModel.PROP_WORKFLOW_DEFAULT_PROPERTIES);
         if (reader != null)
         {
            ObjectInputStream ois = new ObjectInputStream(reader.getContentInputStream());
            params = (Serializable)ois.readObject();
            ois.close();
         }
         return params;
      }
      catch (IOException ioErr)
      {
         throw new AlfrescoRuntimeException("Unable to deserialize workflow default parameters: " + ioErr.getMessage());
      }
      catch (ClassNotFoundException classErr)
      {
         throw new AlfrescoRuntimeException("Unable to deserialize workflow default parameters: " + classErr.getMessage());
      }
   }
   
   /**
    * @return the list of WorkflowDefinition objects as configured in the wcm/workflows client config.
    */
   public static List<WorkflowDefinition> getConfiguredWorkflows()
   {
      if ((configuredWorkflowDefs == null) || (Application.isDynamicConfig(FacesContext.getCurrentInstance())))
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         List<WorkflowDefinition> defs = Collections.<WorkflowDefinition>emptyList();
         ConfigElement config = Application.getConfigService(fc).getGlobalConfig().getConfigElement("wcm");
         if (config == null)
         {
            logger.warn("WARNING: Unable to find 'wcm' config element definition.");
         }
         else
         {
            ConfigElement workflowConfig = config.getChild("workflows");
            if (workflowConfig == null)
            {
               logger.warn("WARNING: Unable to find WCM 'workflows' config element definition.");
            }
            else
            {
               WorkflowService service = Repository.getServiceRegistry(fc).getWorkflowService();
               StringTokenizer t = new StringTokenizer(workflowConfig.getValue().trim(), ", ");
               defs = new ArrayList<WorkflowDefinition>(t.countTokens());
               while (t.hasMoreTokens())
               {
                  String wfName = t.nextToken();
                  WorkflowDefinition def = service.getDefinitionByName("jbpm$" + wfName);
                  if (def != null)
                  {
                     defs.add(def);
                  }
                  else
                  {
                     logger.warn("WARNING: Cannot find WCM workflow def for configured definition name: " + wfName); 
                  }
               }
            }
         }
         configuredWorkflowDefs = defs;
      }
      return configuredWorkflowDefs;
   }
   
   /**
    * @deprecated since 3.2
    */
   public static List<WorkflowTask> getAssociatedTasksForSandbox(final String storeName)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      WorkflowService workflowService = Repository.getServiceRegistry(fc).getWorkflowService();
      return WCMWorkflowUtil.getAssociatedTasksForSandbox(workflowService, storeName);
   }
   
   /**
    * @deprecated since 3.2
    */
   public static List<WorkflowTask> getAssociatedTasksForNode(AVMNodeDescriptor node, List<WorkflowTask> tasks)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      AVMService avmService = Repository.getServiceRegistry(fc).getAVMService();
      return WCMWorkflowUtil.getAssociatedTasksForNode(avmService, node, tasks);
   }
   
   public static boolean isInActiveWorkflow(String sandbox, AVMNodeDescriptor node)
   {
       return isInActiveWorkflow(sandbox, WCMUtil.getStoreRelativePath(node.getPath()));
   }
   
   public static boolean isInActiveWorkflow(String sandbox, String relativePath)
   {
       List<String> cachedPaths = AVMWorkflowUtil.getAssociatedPathsForSandbox(sandbox);
       return (cachedPaths.contains(relativePath));
   }
   
   private static List<String> getAssociatedPathsForSandbox(String sandbox)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      AVMSyncService avmSyncService = Repository.getServiceRegistry(fc).getAVMSyncService();
      WorkflowService workflowService = Repository.getServiceRegistry(fc).getWorkflowService();
       
      Map<String, List<String>> cachedSandboxPaths = (Map<String, List<String>>)fc.getExternalContext().getRequestMap().get(PATH_CACHE);
      if (cachedSandboxPaths == null)
      {
         cachedSandboxPaths = new HashMap<String, List<String>>(64, 1.0f);
         fc.getExternalContext().getRequestMap().put(PATH_CACHE, cachedSandboxPaths);
      }
       
      List<String> cachedPaths = cachedSandboxPaths.get(sandbox);
      if (cachedPaths == null)
      {
         cachedPaths = WCMWorkflowUtil.getAssociatedPathsForSandbox(avmSyncService, workflowService, sandbox);
         cachedSandboxPaths.put(sandbox, cachedPaths);
      }
      
      return cachedPaths;
    }
}
