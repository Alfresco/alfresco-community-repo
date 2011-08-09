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
package org.alfresco.service.cmr.discussion;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class represents a Topic in a forum
 *
 * TODO Decide about the primary post on a topic
 * 
 * @author Nick Burch
 * @since 4.0
 */
public interface ForumTopicInfo extends Serializable, PermissionCheckValue {
   /**
    * @return the NodeRef of the underlying topic
    */
   NodeRef getNodeRef();
   
   /**
    * @return the NodeRef of the container this belongs to (Site or Otherwise)
    */
   NodeRef getContainerNodeRef();
   
   /**
    * @return the System generated name for the topic
    */
   String getSystemName();
   
   /**
    * @return the creator of the topic
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
    * @return the Tags associated with the topic 
    */
   List<String> getTags();
}
