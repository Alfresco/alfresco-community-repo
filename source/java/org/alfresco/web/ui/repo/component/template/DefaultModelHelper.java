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
package org.alfresco.web.ui.repo.component.template;

import java.util.Map;

import org.alfresco.repo.template.FreeMarkerProcessor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;

/**
 * Helper class to generate the default template model.
 * <p>
 * See http://www.alfresco.org/mediawiki/index.php/Template_Guide for details
 * 
 * @author Kevin Roast
 */
public class DefaultModelHelper
{
   /**
    * Private Constructor
    */
   private DefaultModelHelper()
   {
   }

   /**
    * Construct the default FreeMarker template model.
    * <p>
    * Other root level objects such as the current Space or Document are generally
    * added by the appropriate bean responsible for provided access to those nodes. 
    * <p>
    * Uses the default TemplateImageResolver instance to resolve icons - assumes that the client
    * has a valid FacesContext.
    * <p>
    * See http://www.alfresco.org/mediawiki/index.php/Template_Guide for details
    * 
    * @return Map containing the default model.
    */
   public static Map<String, Object> buildDefaultModel(
         ServiceRegistry services, User user, NodeRef template)
   {
      return buildDefaultModel(services, user, template, imageResolver);
   }
   
   /**
    * Construct the default FreeMarker template model.
    * <p>
    * Other root level objects such as the current Space or Document are generally
    * added by the appropriate bean responsible for provided access to those nodes. 
    * <p>
    * See http://www.alfresco.org/mediawiki/index.php/Template_Guide for details
    * 
    * @return Map containing the default model.
    */
   public static Map<String, Object> buildDefaultModel(
         ServiceRegistry services, User user, NodeRef template, TemplateImageResolver resolver)
   {
      if (services == null)
      {
         throw new IllegalArgumentException("ServiceRegistry is mandatory.");
      }
      if (user == null)
      {
         throw new IllegalArgumentException("Current User is mandatory.");
      }
      
      NodeRef companyRootRef = new NodeRef(Repository.getStoreRef(), Application.getCompanyRootId());
      NodeRef userRootRef = new NodeRef(Repository.getStoreRef(), user.getHomeSpaceId());
      
      return FreeMarkerProcessor.buildDefaultModel(
              services, user.getPerson(), companyRootRef, userRootRef, template, resolver);
   }
   
   /** Template Image resolver helper */
   public static final TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
       public String resolveImagePathForName(String filename, boolean small)
       {
           return Utils.getFileTypeImage(filename, small);
       }
   };
}
