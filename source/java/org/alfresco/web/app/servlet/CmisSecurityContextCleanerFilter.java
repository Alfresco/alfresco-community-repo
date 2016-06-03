package org.alfresco.web.app.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.sf.acegisecurity.context.ContextHolder;

/**
 * Clears security context. It should follow Authentication filters in the chain and should be mapped for CMIS requests only
 * 
 * @author Dmitry Velichkevich
 * @since 4.1.5
 */
public class CmisSecurityContextCleanerFilter implements Filter
{
    @Override
    public void destroy()
    {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException
    {
        ContextHolder.setContext(null);
        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig config) throws ServletException
    {
    }
}
