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
package org.alfresco.web.ui.common.component.debug;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.faces.context.FacesContext;

import org.alfresco.repo.admin.patch.AppliedPatch;
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
      List<AppliedPatch> patches = patchService.getPatches(null, null);
      for (AppliedPatch patch : patches)
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
