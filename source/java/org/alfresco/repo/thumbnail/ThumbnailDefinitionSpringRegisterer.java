package org.alfresco.repo.thumbnail;

import org.alfresco.service.cmr.repository.TransformationOptions;
import org.springframework.beans.factory.InitializingBean;

/**
 * This class provides a way to register a new {@link ThumbnailDefinition}
 *  with the {@link ThumbnailRegistry} in spring, without needing to
 *  override the whole of the "thumbnailRegistry" bean.
 * This class is a stop-gap until Alfresco 4.0, when ThumbnailDefinitions
 *  will be able to be registered more cleanly with the registry in
 *  the same way as other Alfresco beans to their registrys.
 * 
 * @author Nick Burch
 */
public class ThumbnailDefinitionSpringRegisterer implements InitializingBean
{
   private ThumbnailRegistry thumbnailRegistry;
   private ThumbnailDefinition thumbnailDefinition;
   
   /**
    * Registers the {@link ThumbnailDefinition} with the
    *  registry.
    */
   @Override
   public void afterPropertiesSet() 
   {
      if(thumbnailDefinition == null || thumbnailRegistry == null)
      {
         throw new IllegalArgumentException("Must specify both a thumbnailRegistry AND a thumbnailDefinition");
      }
      thumbnailRegistry.addThumbnailDefinition(thumbnailDefinition);
   }

   public void setThumbnailRegistry(ThumbnailRegistry thumbnailRegistry) 
   {
      this.thumbnailRegistry = thumbnailRegistry;
   }

   public void setThumbnailDefinition(ThumbnailDefinition thumbnailDefinition) 
   {
      this.thumbnailDefinition = thumbnailDefinition;
   }
}
