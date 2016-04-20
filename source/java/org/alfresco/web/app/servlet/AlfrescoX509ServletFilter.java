
package org.alfresco.web.app.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.alfresco.web.scripts.servlet.X509ServletFilterBase;

import javax.servlet.*;
import java.io.IOException;
import java.util.Properties;

/**
 * The AlfrescoX509ServletFilter implements the checkEnforce method of the X509ServletFilterBase.
 * This allows the configuration of X509 authentication to be toggled on/off through a
 * configuration outside of the web.xml.
 **/

public class AlfrescoX509ServletFilter extends X509ServletFilterBase
{
    private static final String BEAN_GLOBAL_PROPERTIES = "global-properties";
    private static final String SECURE_COMMS = "solr.secureComms";

    private static Log logger = LogFactory.getLog(AlfrescoX509ServletFilter.class);

    @Override
    protected boolean checkEnforce(ServletContext servletContext) throws IOException
    {
        /*
        * Get the secureComms setting from the global properties bean.
        */

        WebApplicationContext wc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        Properties globalProperties = (Properties) wc.getBean(BEAN_GLOBAL_PROPERTIES);
        String prop = globalProperties.getProperty(SECURE_COMMS);

        if(logger.isDebugEnabled())
        {
            logger.debug("secureComms:"+prop);
        }

        /*
         * Return true or false based on the property. This will switch on/off X509 enforcement in the X509ServletFilterBase.
         */

        if (prop == null || "none".equals(prop))
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}