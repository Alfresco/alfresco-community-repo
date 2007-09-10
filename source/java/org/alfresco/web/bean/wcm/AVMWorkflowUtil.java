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
package org.alfresco.web.bean.wcm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.faces.context.FacesContext;

import org.alfresco.config.ConfigElement;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.wf.AVMSubmittedAspect;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.PropertyValue;
import org.alfresco.repo.security.authority.AuthorityDAO;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
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

   // cached configured lists
   private static List<WorkflowDefinition> configuredWorkflowDefs = null;
   
   public static NodeRef createWorkflowPackage(final List<String> srcPaths,
                                               final SandboxInfo sandboxInfo,
                                               final WorkflowPath path,
                                               final AVMSubmittedAspect avmSubmittedAspect,
                                               final AVMSyncService avmSyncService,
                                               final AVMService avmService,
                                               final WorkflowService workflowService,
                                               final NodeService nodeService)
   {

      // create package paths (layered to user sandbox area as target)
      final String workflowMainStoreName = sandboxInfo.getMainStoreName();
      final String packagesPath = AVMUtil.buildStoreRootPath(workflowMainStoreName);

      final String stagingStoreName = AVMUtil.getStoreId(workflowMainStoreName);
      final HashSet<String> directoriesAdded = new HashSet<String>();
      final List<AVMDifference> diffs = new ArrayList<AVMDifference>(srcPaths.size());
      for (final String srcPath : srcPaths)
      {
         // add all newly created directories
         String parentPath = AVMNodeConverter.SplitBase(srcPath)[0];
         while (!directoriesAdded.contains(parentPath) &&
                avmService.lookup(-1, AVMUtil.getCorrespondingPath(parentPath, stagingStoreName)) == null)
         {
            diffs.add(new AVMDifference(-1, parentPath,
                                        -1, AVMUtil.getCorrespondingPath(parentPath, workflowMainStoreName),
                                        AVMDifference.NEWER));
            avmSubmittedAspect.markSubmitted(-1, parentPath, path.instance.id);
            directoriesAdded.add(parentPath);
            parentPath = AVMNodeConverter.SplitBase(parentPath)[0];
         }

         diffs.add(new AVMDifference(-1, srcPath, 
                                     -1, AVMUtil.getCorrespondingPath(srcPath, workflowMainStoreName),
                                     AVMDifference.NEWER));
         avmSubmittedAspect.markSubmitted(-1, srcPath, path.instance.id);
      }
                  
      // write changes to layer so files are marked as modified
      avmSyncService.update(diffs, null, true, true, false, false, null, null);
                    
      // convert package to workflow package
      final AVMNodeDescriptor packageDesc = avmService.lookup(-1, packagesPath);
      final NodeRef packageNodeRef = workflowService.createPackage(AVMNodeConverter.ToNodeRef(-1, packageDesc.getPath()));
      avmService.setNodeProperty(packagesPath, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, new PropertyValue(null, true));
      // nodeService.setProperty(packageNodeRef, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, true);

      // apply global permission to workflow package
      // TODO: Determine appropriate permissions
      final ServiceRegistry services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      final PermissionService permissionService = services.getPermissionService();
      permissionService.setPermission(packageNodeRef, PermissionService.ALL_AUTHORITIES, PermissionService.ALL_PERMISSIONS, true);
      return packageNodeRef;
   }
   
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
         if (config != null)
         {
            ConfigElement workflowConfig = config.getChild("workflows");
            if (workflowConfig != null)
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
            else
            {
               logger.warn("WARNING: Unable to find WCM 'workflows' config element definition.");
            }
         }
         else
         {
            logger.warn("WARNING: Unable to find 'wcm' config element definition.");
         }
         configuredWorkflowDefs = defs;
      }
      return configuredWorkflowDefs;
   }
}
