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
package org.alfresco.repo.jscript;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.ParameterCheck;

/**
 * Scripted Presence service for determining online status of People.
 * 
 * @author Mike Hatfield
 */

public final class Presence extends BaseScopableProcessorExtension
{
   /** Repository Service Registry */
   private ServiceRegistry services;

   /**
    * Set the service registry
    * 
    * @param serviceRegistry  the service registry
    */
   public void setServiceRegistry(ServiceRegistry serviceRegistry)
   {
      this.services = serviceRegistry;
   }

   /**
    * Gets whether the Person has configured Presence parameters
    * 
    * @param person       the person to query
    * 
    * @return true if this person is configured for presence
    */
   public boolean hasPresence(ScriptNode person)
   {
       ParameterCheck.mandatory("Person", person);
       String presenceProvider = (String)person.getProperties().get(ContentModel.PROP_PRESENCEPROVIDER);
       String presenceUsername = (String)person.getProperties().get(ContentModel.PROP_PRESENCEUSERNAME);

       return ((presenceProvider != "") && (presenceUsername != ""));
   }

   /**
    * Query current online status of given person
    * 
    * @param person       the person to query
    * 
    * @return string indicating online presence status
    */
   public String getDetails(ScriptNode person)
   {
      ParameterCheck.mandatory("Person", person);
      String presenceProvider = (String)person.getProperties().get(ContentModel.PROP_PRESENCEPROVIDER);
      String presenceUsername = (String)person.getProperties().get(ContentModel.PROP_PRESENCEUSERNAME);
      String detail = presenceProvider + "|" + presenceUsername;

      return detail;
   }

}
