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
package org.alfresco.web.ui.repo.component.template;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.template.DateCompareMethod;
import org.alfresco.repo.template.FreeMarkerProcessor;
import org.alfresco.repo.template.HasAspectMethod;
import org.alfresco.repo.template.I18NMessageMethod;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.service.cmr.repository.TemplateNode;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.ui.common.Utils;

/**
 * Helper class to generate the default template model.
 * <p>
 * See {@link http://www.alfresco.org/mediawiki/index.php/Template_Guide}
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
    * See {@link http://www.alfresco.org/mediawiki/index.php/Template_Guide}
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
    * See {@link http://www.alfresco.org/mediawiki/index.php/Template_Guide}
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
