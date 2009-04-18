/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for all Handler implementations.
 *
 * @author Gavin Cornwell
 */
public abstract class AbstractHandler implements Handler
{
    private static final Log logger = LogFactory.getLog(AbstractHandler.class);
    
    protected HandlerRegistry handlerRegistry;
    protected boolean active = true;

    /**
     * Sets the handler registry
     * 
     * @param handlerRegistry The FormProcessorHandlerRegistry instance
     */
    public void setHandlerRegistry(HandlerRegistry handlerRegistry)
    {
        this.handlerRegistry = handlerRegistry;
    }
    
    /**
     * Sets whether this processor is active
     * 
     * @param active true if the processor should be active
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }
    
    /**
     * Registers this handler with the handler registry
     */
    public void register()
    {
        if (handlerRegistry == null)
        {
            if (logger.isWarnEnabled())
                logger.warn("Property 'handlerRegistry' has not been set.  Ignoring auto-registration of handler: " + this);
            
            return;
        }

        // register this instance
        handlerRegistry.addHandler(this);
    }

    /*
     * @see org.alfresco.repo.forms.processor.Handler#isActive()
     */
    public boolean isActive()
    {
        return this.active;
    }
    
    /*
     * @see org.alfresco.repo.forms.processor.Handler#isApplicable(java.lang.String)
     */
    public boolean isApplicable(Object item)
    {
        // by default all handlers are applicable
        return true;
    }
    
    /*
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        StringBuilder buffer = new StringBuilder(super.toString());
        buffer.append(" (");
        buffer.append("active=").append(this.isActive());
        buffer.append(")");
        return buffer.toString();
    }
}
