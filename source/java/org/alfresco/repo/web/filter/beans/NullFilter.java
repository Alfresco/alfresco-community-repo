/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
