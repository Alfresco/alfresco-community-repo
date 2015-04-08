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
package org.alfresco.service.cmr.wiki;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents a Wiki Paeg in a site 
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface WikiPageInfo extends Serializable, PermissionCheckValue 
{
   /**
    * @return the NodeRef of the underlying wiki page
    */
   NodeRef getNodeRef();
   
   /**
    * @return the NodeRef of the site container this belongs to
    */
   NodeRef getContainerNodeRef();
   
   /**
    * @return the name of the wiki page
    */
   String getSystemName();
   
   /**
    * @return the Title of the wiki page
    */
   String getTitle();
   
   /**
    * Sets the Title of the wiki page
    */
   void setTitle(String title);
   
   /**
    * @return the HTML Content of the wiki page
    */
   String getContents();
   
   /**
    * Sets the (HTML) Content of the wiki page
    */
   void setContents(String contentHTML);
   
   /**
    * @return the creator of the wiki page
    */
   String getCreator();
   
   /**
    * @return the modifier of the wiki page
    */
   String getModifier();
   
   /**
    * @return the creation date and time
    */
   Date getCreatedAt();
   
   /**
    * @return the modification date and time
    */
   Date getModifiedAt();
   
   /**
    * @return the Tags associated with the wiki page 
    */
   List<String> getTags();
}
