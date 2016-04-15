package org.alfresco.web.bean;

import java.io.Serializable;

/**
 * A request-scoped bean that is used to propagate the URL to redirect to after successful login. This is now done with
 * request parameters rather than session-scoped beans so that tools such as MS Office, which do not propagate cookies
 * to hyperlinks, can be used to open hyperlinks to protected documents (ALF-206).
 * 
 * @author dward
 */
public class LoginOutcomeBean implements Serializable
{
   /** The name of the request parameter that provides the initial value of the {@link #redirectURL} property. */
   public static final String PARAM_REDIRECT_URL = "_alfRedirect";

   private static final long serialVersionUID = -2575348340143674698L;

   /** The URL to redirect to after successful login. */
   private String redirectURL;

   /**
    * Gets the URL to redirect to after successful login.
    * 
    * @return the URL to redirect to after successful login
    */
   public String getRedirectURL()
   {
      return redirectURL;
   }

   /**
    * Sets the URL to redirect to after successful login.
    * 
    * @param redirectURL
    *           the URL to redirect to after successful login
    */
   public void setRedirectURL(String redirectURL)
   {
      this.redirectURL = redirectURL;
   }

}
