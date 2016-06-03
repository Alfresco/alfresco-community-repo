package org.alfresco.web.app.servlet.command;

import java.util.Map;

import org.alfresco.service.ServiceRegistry;

/**
 * @author Kevin Roast
 */
public abstract class BaseUIActionCommand implements Command
{
   public static final String PROP_SERVLETCONTEXT = "ServletContext";
   public static final String PROP_REQUEST = "Request";
   public static final String PROP_RESPONSE = "Response";
}
