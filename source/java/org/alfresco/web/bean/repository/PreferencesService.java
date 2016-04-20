package org.alfresco.web.bean.repository;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.alfresco.web.app.Application;

/**
 * Simple client service to retrieve the Preferences object for the current User.
 * 
 * @author Kevin Roast
 */
public final class PreferencesService
{
   /**
    * Private constructor
    */
   private PreferencesService()
   {
   }
   
   /**
    * @return The Preferences for the current User instance.
    */
   public static Preferences getPreferences()
   {
      return getPreferences(FacesContext.getCurrentInstance());
   }
   
   /**
    * @param fc   FacesContext
    * @return The Preferences for the current User instance.
    */
   public static Preferences getPreferences(FacesContext fc)
   {
      User user = Application.getCurrentUser(fc);
      return user != null ? user.getPreferences(fc) : null;
   }
   
   /**
    * @param session Http session
    * @return The Preferences for the current User instance.
    */
   public static Preferences getPreferences(HttpSession session)
   {
      User user = Application.getCurrentUser(session);
      return user != null ? user.getPreferences(session.getServletContext()) : null;
   }
}
