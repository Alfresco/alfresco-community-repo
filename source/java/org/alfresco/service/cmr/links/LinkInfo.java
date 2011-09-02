/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.service.cmr.links;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents a Link in a site 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface LinkInfo extends Serializable, PermissionCheckValue 
{
   /**
    * @return the NodeRef of the underlying link
    */
   NodeRef getNodeRef();
   
   /**
    * @return the NodeRef of the site container this belongs to
    */
   NodeRef getContainerNodeRef();
   
   /**
    * @return the System generated name for the link
    */
   String getSystemName();
   
   /**
    * @return the Title of the link
    */
   String getTitle();
   
   /**
    * Sets the Title of the link
    */
   void setTitle(String title);
   
   /**
    * @return the Description of the link
    */
   String getDescription();
   
   /**
    * Sets the Description of the link
    */
   void setDescription(String description);
   
   /**
    * @return the URL of the link
    */
   String getURL();

   /**
    * Sets the URL of the link
    */
   void setURL(String url);
   
   /**
    * @return the creator of the link
    */
   String getCreator();
   
   /**
    * @return the creation date and time
    */
   Date getCreatedAt();
   
   /**
    * @return the modification date and time
    */
   Date getModifiedAt();
   
   /**
    * Is this a internal link?
    */
   boolean isInternal();
   
   /**
    * Sets if this is an internal link or not
    */
   void setInternal(boolean internal);
   
   /**
    * @return the Tags associated with the link 
    */
   List<String> getTags();
}
