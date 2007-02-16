/*
 * Copyright (C) 2005 Alfresco, Inc.
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
