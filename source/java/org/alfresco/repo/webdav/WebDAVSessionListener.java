
package org.alfresco.repo.webdav;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>WebDAVSessionListener is used to forcibly unlock documents that were 
 * persistently locked during user's session and were not unlocked because of some extraordinary
 * situations such as network connection lost. Was introduced in ALF-11777 jira issue.
 * </p>
 * 
 * @author Pavel.Yurkevich
 * 
 */
public class WebDAVSessionListener implements HttpSessionListener, ServletContextListener
{
    private static Log logger = LogFactory.getLog(WebDAVSessionListener.class);
    private WebDAVLockService webDAVLockService;

    /**
     * @param webDAVLockService
     *            the webDAVLockService to set
     */
    public void setWebDAVLockService(WebDAVLockService webDAVLockService)
    {
        this.webDAVLockService = webDAVLockService;
    }

    @Override
    public void sessionCreated(HttpSessionEvent hse)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Session created " + hse.getSession().getId());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void sessionDestroyed(HttpSessionEvent hse)
    {
        webDAVLockService.setCurrentSession(hse.getSession());
        webDAVLockService.sessionDestroyed();

        if (logger.isDebugEnabled())
        {
            logger.debug("Session destroyed " + hse.getSession().getId());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        this.webDAVLockService = (WebDAVLockService)context.getBean(WebDAVLockService.BEAN_NAME);
    }
}
