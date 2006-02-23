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
package org.alfresco.web.ui.common.component.debug;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;

import org.alfresco.repo.admin.patch.PatchInfo;
import org.alfresco.repo.admin.patch.PatchService;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.descriptor.Descriptor;
import org.alfresco.service.descriptor.DescriptorService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * Component which displays the Alfresco Repository properties
 * 
 * @author kevinr
 */
public class UIRepositoryProperties extends BaseDebugComponent
{
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.debug.RepositoryProperties";
   }

   /**
    * @see org.alfresco.web.ui.common.component.debug.BaseDebugComponent#getDebugData()
    */
   @SuppressWarnings("unchecked")
   public Map getDebugData()
   {
      // note: sort properties
      Map properties = new TreeMap();
      
      FacesContext fc = FacesContext.getCurrentInstance();
      ServiceRegistry services = Repository.getServiceRegistry(fc);
      DescriptorService descriptorService = services.getDescriptorService();
      
      Descriptor installedRepoDescriptor = descriptorService.getInstalledRepositoryDescriptor();
      properties.put("Installed Version", installedRepoDescriptor.getVersion());
      properties.put("Installed Schema", installedRepoDescriptor.getSchema());
      
      Descriptor systemDescriptor = descriptorService.getServerDescriptor();
      properties.put("Server Version", systemDescriptor.getVersion());
      properties.put("Server Schema", systemDescriptor.getSchema());
      
      WebApplicationContext cx = FacesContextUtils.getRequiredWebApplicationContext(fc);
      PatchService patchService = (PatchService)cx.getBean("PatchService");
      List<PatchInfo> patches = patchService.getPatches(null, null);
      for (PatchInfo patch : patches)
      {
         StringBuilder data = new StringBuilder(256);
         data.append(patch.getAppliedOnDate())
             .append(" - ")
             .append(patch.getDescription())
             .append(" - ")
             .append(patch.getSucceeded() == true ?
                     Application.getMessage(fc, "repository_patch_succeeded") :
                     Application.getMessage(fc, "repository_patch_failed"));
         properties.put(patch.getId(), data);
      }
      
      return properties; 
   }
}
