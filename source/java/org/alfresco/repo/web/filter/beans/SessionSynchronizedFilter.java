package org.alfresco.repo.web.filter.beans;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * A filter that will use the HttpSession (if it exists) as the monitor for a
 * synchronized block so that only one request per session is processed at any
 * time.
 * 
 * Originally created to avoid having to make 200+ JSF session scoped beans thread
 * safe.
 * 
 * @author Alan Davis
 * @deprecated 5.0 not exposed in web-client web.xml
 */
public class SessionSynchronizedFilter implements Filter
{
    @Override
    public void init(FilterConfig arg0) throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException
    {
        HttpSession session = null;
        if (request instanceof HttpServletRequest)
        {
            session = ((HttpServletRequest)request).getSession(false);
        }
        if (session != null)
        {
            synchronized(session)
            {
                chain.doFilter(request, response);
            }
        }
        else
        {
            chain.doFilter(request, response);
        }
    }
}
