package org.alfresco.repo.web.filter.beans;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.alfresco.repo.management.subsystems.ActivateableBean;

/**
 * A Benign filter that does nothing more than invoke the filter chain. Allows strategic points of the filter chain to
 * be configured in and out according to the authentication subsystem in use.
 * 
 * @author dward
 */
public class NullFilter implements DependencyInjectedFilter, ActivateableBean
{
    private boolean isActive = true;

    /**
     * Activates or deactivates the bean
     * 
     * @param active
     *            <code>true</code> if the bean is active and initialization should complete
     */
    public void setActive(boolean active)
    {
        this.isActive = active;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.management.subsystems.ActivateableBean#isActive()
     */
    public boolean isActive()
    {
        return this.isActive;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.web.filter.beans.DependencyInjectedFilter#doFilter(javax.servlet.ServletContext,
     * javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletContext context, ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException
    {
        chain.doFilter(request, response);
    }
    
    
}
