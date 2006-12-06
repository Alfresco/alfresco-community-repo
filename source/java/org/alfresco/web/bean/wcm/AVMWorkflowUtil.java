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

import javax.faces.context.FacesContext;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.WCMAppModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.GUID;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.workflow.WorkflowUtil;

/**
 * AVM Specific workflow related helper methods.
 * 
 * @author Kevin Roast
 */
public class AVMWorkflowUtil extends WorkflowUtil
{
   private static final String STORE_WORKFLOW_SYSTEM = "workflow-system";
   private static final String FOLDER_PACKAGES = "packages";
   
   /**
    * Return the AVM workflow package root folder path - creating the default
    * store and root folder if required.
    * 
    * @param avmService       AVMService to use
    * 
    * @return AVM Root package path
    */
   public static String getAVMPackageRoot(AVMService avmService)
   {
      String packagesRoot = STORE_WORKFLOW_SYSTEM + ":/" + FOLDER_PACKAGES;
      AVMNodeDescriptor packagesDesc = avmService.lookup(-1, packagesRoot);
      if (packagesDesc == null)
      {
         avmService.createAVMStore(STORE_WORKFLOW_SYSTEM);
         avmService.createDirectory(STORE_WORKFLOW_SYSTEM + ":/", FOLDER_PACKAGES);
      }
      return packagesRoot;
   }
   
   /**
    * Create an AVM layered workflow package against the specified sandbox path. 
    * 
    * @param avmService       AVMService to use
    * @param sandboxPath      The sandbox path to layer the package over
    * 
    * @return Path to the layered package.
    */
   public static String createAVMLayeredPackage(AVMService avmService, String sandboxPath)
   {
      String packagesRoot = getAVMPackageRoot(avmService);
      String packageName = GUID.generate();
      avmService.createLayeredDirectory(sandboxPath, packagesRoot, packageName);
      
      return packagesRoot + "/" + packageName;
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
