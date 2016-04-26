package org.alfresco.web.ui.repo.component.template;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.alfresco.repo.web.scripts.FileTypeImageUtils;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.FileTypeImageSize;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.TemplateImageResolver;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.bean.repository.User;

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
      
      return services.getTemplateService().buildDefaultModel(
              user.getPerson(), companyRootRef, userRootRef, template, resolver);
   }
   
   /** Template Image resolver helper */
   public static final TemplateImageResolver imageResolver = new TemplateImageResolver()
   {
      public String resolveImagePathForName(String filename, FileTypeImageSize size)
      {
         return FileTypeImageUtils.getFileTypeImage(FacesContext.getCurrentInstance(), filename, size);
      }
   };
}
