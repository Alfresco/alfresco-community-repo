package org.alfresco.rest.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.TenantWebScriptServlet;
import org.alfresco.rest.framework.tools.ApiAssistant;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.webscripts.RuntimeContainer;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRuntime;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class PublicApiWebScriptServlet extends TenantWebScriptServlet
{
	private static final long serialVersionUID = 726730674397482039L;
	private ApiAssistant apiAssistant;
    @Override
    public void init() throws ServletException
    {
    	super.init();
    	
        ApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        container = (RuntimeContainer)context.getBean("publicapi.container");
        apiAssistant = (ApiAssistant) context.getBean("apiAssistant");
    }
    
    /* (non-Javadoc) 
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
		// make the request input stream a BufferedInputStream so that the first x bytes can be reused.
    	PublicApiHttpServletRequest wrapped = new PublicApiHttpServletRequest(req);
        super.service(wrapped, res);
    }
    
    protected WebScriptServletRuntime getRuntime(HttpServletRequest req, HttpServletResponse res)
    {
        WebScriptServletRuntime runtime = new PublicApiTenantWebScriptServletRuntime(container, authenticatorFactory, req, res, serverProperties, apiAssistant);
        return runtime;
    }
}
