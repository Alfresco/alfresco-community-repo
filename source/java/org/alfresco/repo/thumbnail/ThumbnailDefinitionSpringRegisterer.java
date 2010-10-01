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
