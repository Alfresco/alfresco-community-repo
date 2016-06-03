package org.alfresco.repo.jscript;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.springframework.extensions.surf.util.ParameterCheck;

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

       return (!"".equals((presenceProvider)) && (!"".equals(presenceUsername)));
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
