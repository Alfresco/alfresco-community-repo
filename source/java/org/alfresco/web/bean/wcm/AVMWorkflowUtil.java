/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.wcm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.avm.wf.AVMSubmittedAspect;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.WorkflowUtil;

/**
 * AVM Specific workflow related helper methods.
 * 
 * @author Ariel Backenroth
 * @author Kevin Roast
 */
public class AVMWorkflowUtil extends WorkflowUtil
{
   // Common workflow definitions
   private static final String WCM_WORKFLOW_MODEL_1_0_URI = "http://www.alfresco.org/model/wcmworkflow/1.0";
   public static final QName PROP_FROM_PATH = QName.createQName(WCM_WORKFLOW_MODEL_1_0_URI, "fromPath");
   public static final QName PROP_LABEL = QName.createQName(WCM_WORKFLOW_MODEL_1_0_URI, "label");

   public static NodeRef createWorkflowPackage(final List<String> srcPaths,
                                               final String storeId,
                                               final WorkflowPath path,
                                               final AVMSubmittedAspect avmSubmittedAspect,
                                               final AVMSyncService avmSyncService,
                                               final AVMService avmService,
                                               final WorkflowService workflowService,
                                               final NodeService nodeService)
   {
      // create package paths (layered to user sandbox area as target)
      final String packageName = SandboxFactory.createWorkflowSandbox(storeId);
      final String workflowMainStoreName =
         AVMConstants.buildWorkflowMainStoreName(storeId, packageName);
      final String packagesPath = AVMConstants.buildStoreRootPath(workflowMainStoreName);
                    
      final List<AVMDifference> diffs = new ArrayList<AVMDifference>(srcPaths.size());
      for (final String srcPath : srcPaths)
      {
         diffs.add(new AVMDifference(-1, srcPath, 
                                     -1, AVMConstants.getCorrespondingPath(srcPath, workflowMainStoreName),
                                     AVMDifference.NEWER));
         avmSubmittedAspect.markSubmitted(-1, srcPath, path.instance.id);
      }
                  
      // write changes to layer so files are marked as modified
      avmSyncService.update(diffs, null, true, true, false, false, null, null);
                    
      // convert package to workflow package
      final AVMNodeDescriptor packageDesc = avmService.lookup(-1, packagesPath);
      final NodeRef packageNodeRef = workflowService.createPackage(AVMNodeConverter.ToNodeRef(-1, packageDesc.getPath()));
      nodeService.setProperty(packageNodeRef, WorkflowModel.PROP_IS_SYSTEM_PACKAGE, true);
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
         ContentWriter writer = cs.getWriter(workflowRef, WCMAppModel.PROP_WORKFLOWDEFAULTS, true);
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
         ContentReader reader = cs.getReader(workflowRef, WCMAppModel.PROP_WORKFLOWDEFAULTS);
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
}
