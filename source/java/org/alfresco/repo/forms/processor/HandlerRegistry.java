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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds a list of handlers for a type of form processor, the handlers are called 
 * in sequence to check their applicability, if the handler applies to the item
 * being processed it's generate or persist method is called.
 *
 * @see org.alfresco.repo.forms.processor.Handler
 * @author Gavin Cornwell
 */
public class HandlerRegistry
{
    private static final Log logger = LogFactory.getLog(HandlerRegistry.class);
    
    protected List<Handler> handlers;
    
    /**
     * Constructs the registry
     */
    public HandlerRegistry()
    {
        this.handlers = new ArrayList<Handler>(4);
    }
    
    /**
     * Registers a handler
     * 
     * @param handler The Handler to regsiter
     */
    public void addHandler(Handler handler)
    {
        if (handler.isActive())
        {
            this.handlers.add(handler);
            
            if (logger.isDebugEnabled())
                logger.debug("Registered handler: " + handler + " in register: " + this);
        }
        else if (logger.isWarnEnabled())
        {
            logger.warn("Ignored registration of handler " + handler + "as it was marked as inactive");
        }
    }
    
    /**
     * Returns a list of handlers applicable for the given item
     * 
     * @param item The item the form is being processed for
     * @return List of applicable Handler objects
     */
    public List<Handler> getApplicableHandlers(Object item)
    {
        List<Handler> applicableHandlers = new ArrayList<Handler>(4);
        
        // iterate round the handlers and add each active applicable
        // handler to the list
        for (Handler handler : this.handlers)
        {
            if (handler.isActive() && handler.isApplicable(item))
            {
                applicableHandlers.add(handler);
            }
        }
        
        if (logger.isDebugEnabled())
            logger.debug("Returning applicable handlers: " + applicableHandlers);
        
        return applicableHandlers;
    }
}
