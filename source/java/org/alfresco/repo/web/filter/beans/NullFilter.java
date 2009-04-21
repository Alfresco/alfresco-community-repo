/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
