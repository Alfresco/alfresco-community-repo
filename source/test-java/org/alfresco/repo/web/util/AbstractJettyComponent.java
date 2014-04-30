/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.web.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.BaseApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.WebApplicationContextLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

/**
 * Manages an embedded jetty server, hooking it up to the repository spring context.
 * 
 * @author steveglover
 *
 */
public abstract class AbstractJettyComponent implements JettyComponent
{
	protected static final Log logger = LogFactory.getLog(AbstractJettyComponent.class);

    public static final int JETTY_STOP_PORT = 8079;
    public static final String JETTY_LOCAL_IP = "127.0.0.1";

    protected int port = 8081;
    protected String contextPath = "/alfresco";
    protected String publicApiServletName = "api";
    protected String[] configLocations;
    protected String[] classLocations;
    protected static Server server;

    private WebAppContext webAppContext;

	public AbstractJettyComponent(int port, String contextPath, String[] configLocations, String[] classLocations)
	{
		this.configLocations = configLocations;
		this.classLocations = classLocations;
		this.port = port;
		this.contextPath = contextPath;
	    server = new Server(port);
	}
	
	public int getPort()
	{
		return port;
	}

	/*
	 * Creates a web application context wrapping a Spring application context (adapted from core spring code in
	 * org.springframework.web.context.ContextLoader)
	 */
	protected WebApplicationContext createWebApplicationContext(ServletContext sc, ApplicationContext parent)
	{
		GenericWebApplicationContext wac = (GenericWebApplicationContext) BeanUtils.instantiateClass(GenericWebApplicationContext.class);

		// Assign the best possible id value.
		wac.setId(ConfigurableWebApplicationContext.APPLICATION_CONTEXT_ID_PREFIX + contextPath);

		wac.setParent(parent);
		wac.setServletContext(sc);
		wac.refresh();

		return wac;
	}
	
	public ConfigurableApplicationContext getApplicationContext()
	{
		return (ConfigurableApplicationContext)webAppContext.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
	}
	
	protected abstract void configureWebAppContext(WebAppContext webAppContext);

	public void start()
	{
		if(logger.isDebugEnabled())
		{
			logger.debug("["+new Date()+"] startJetty: starting embedded Jetty server ...");
		}

	    try
	    {
			if(logger.isDebugEnabled())
			{
				logger.debug("["+new Date()+"] startJetty");
			}

		    this.webAppContext = new WebAppContext();
		    webAppContext.setContextPath(contextPath);

		    configure(webAppContext);

		    server.setHandler(webAppContext);

		    // for clean shutdown, add monitor thread 
		    
		    // from: http://ptrthomas.wordpress.com/2009/01/24/how-to-start-and-stop-jetty-revisited/
		    // adapted from: http://jetty.codehaus.org/jetty/jetty-6/xref/org/mortbay/start/Monitor.html
		    Thread monitor = new MonitorThread();
		    monitor.start();
		    
		    configureWebAppContext(webAppContext);

		    server.start();

			if(logger.isDebugEnabled())
			{
				logger.debug("["+new Date()+"] startJetty: ... embedded Jetty server started on port " + port);
			}
        }
        catch (Exception e)
        {
        	logger.error("["+new Date()+"] startJetty: ... failed to start embedded Jetty server on port " + port + ", " + e);
        }
	}
	
	protected void configure(WebAppContext webAppContext)
	{
        try
        {
        	ClassLoader classLoader = BaseApplicationContextHelper.buildClassLoader(classLocations);
            webAppContext.setClassLoader(classLoader);
        }
        catch (IOException e)
        {
            throw new ExceptionInInitializerError(e);
        }
        
	    webAppContext.addEventListener(new ServletContextListener()
		{
	    	public void contextInitialized(ServletContextEvent sce)
		    {
	    		// create a Spring web application context, wrapping and providing access to
	    		// the application context
	    		try
	    		{
		    		ServletContext servletContext = sce.getServletContext();

		    		// initialise web application context
		    		WebApplicationContextLoader.getApplicationContext(servletContext, configLocations, classLocations);

					if(logger.isDebugEnabled())
					{
						logger.debug("contextInitialized "+sce);
					}
	    		}
	    		catch(Throwable t)
	    		{
	    			logger.error("Failed to start Jetty server: " + t);
	    			throw new AlfrescoRuntimeException("Failed to start Jetty server", t);
	    		}
	    	}

		    public void contextDestroyed(ServletContextEvent sce)
		    {
				if(logger.isDebugEnabled())
				{
					logger.debug("contextDestroyed "+sce);
				}
			}    
		});
	    
	    // with a login-config in web.xml, jetty seems to require this in order to start successfully
	    webAppContext.getSecurityHandler().setLoginService(new HashLoginService());

	    // arbitrary temporary file location
	    File tmp = new File(TempFileProvider.getSystemTempDir(), String.valueOf(System.currentTimeMillis()));
	    webAppContext.setResourceBase(tmp.getAbsolutePath());		
	}

	public void shutdown()
	{
		try
		{
			server.stop();
		}
		catch(Exception e)
		{
			throw new AlfrescoRuntimeException("", e);
		}
	}
	
    private static class MonitorThread extends Thread 
    {
        private ServerSocket socket;
        
        public MonitorThread() 
        {
            setDaemon(true);
            setName("StopMonitor");
            try 
            {
                socket = new ServerSocket(JETTY_STOP_PORT, 1, InetAddress.getByName(JETTY_LOCAL_IP));
            } 
            catch(Exception e) 
            {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public void run() 
        {
            Socket accept;
            try 
            {
                accept = socket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                reader.readLine();
                server.stop();
                accept.close();
                socket.close();
            } 
            catch(Exception e) 
            {
                throw new RuntimeException(e);
            }
        }
    }
}
