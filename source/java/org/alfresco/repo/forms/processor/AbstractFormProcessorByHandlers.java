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

import java.util.List;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormException;
import org.alfresco.repo.forms.Item;

/**
 * Abstract base class for all FormProcessor implementations that wish to use the 
 * handler mechanism.
 *
 * @author Gavin Cornwell
 */
public abstract class AbstractFormProcessorByHandlers extends AbstractFormProcessor
{
    protected HandlerRegistry handlerRegistry;
    
    /**
     * Sets the form processor handler registry 
     * 
     * @param handlerRegistry The FormProcessorHandlerRegistry instance
     */
    public void setHandlerRegistry(HandlerRegistry handlerRegistry)
    {
        this.handlerRegistry = handlerRegistry;
    }
        
    /**
     * Generates a Form for the given item, constructed by calling each
     * applicable registered handler
     * 
     * @see org.alfresco.repo.forms.processor.FormProcessor#generate(org.alfresco.repo.forms.Item, java.util.List, java.util.List)
     * @param item The item to generate a form for
     * @param fields Restricted list of fields to include
     * @param forcedFields List of fields to forcibly include
     * @return The generated Form
     */
    public Form generate(Item item, List<String> fields, List<String> forcedFields)
    {
        if (this.handlerRegistry == null)
        {
            throw new FormException("Property 'handlerRegistry' has not been set.");
        }
        
        // get the typed object representing the item
        Object typedItem = getTypedItem(item);

        // create an empty Form
        Form form = new Form(item);
        
        // execute each applicable handler
        for (Handler handler: this.handlerRegistry.getApplicableHandlers(typedItem))
        {
            form = handler.handleGenerate(typedItem, fields, forcedFields, form);
        }
        
        return form;
    }

    /**
     * Persists the given form data for the given item, completed by calling 
     * each applicable registered handler
     * 
     * @see org.alfresco.repo.forms.processor.FormProcessor#persist(org.alfresco.repo.forms.Item, org.alfresco.repo.forms.FormData)
     * @param item The item to save the form for
     * @param data The object representing the form data
     */
    public void persist(Item item, FormData data)
    {
        if (this.handlerRegistry == null)
        {
            throw new FormException("Property 'handlerRegistry' has not been set.");
        }
        
        // get the typed object representing the item
        Object typedItem = getTypedItem(item);
        
        // execute each applicable handler
        for (Handler handler: this.handlerRegistry.getApplicableHandlers(typedItem))
        {
            handler.handlePersist(typedItem, data);
        }
    }
    
    /**
     * Returns a typed Object representing the given item. 
     * <p>
     * Subclasses that represent a form type will return a typed object
     * that is then passed to each of it's handlers, the handlers can 
     * therefore safely cast the Object to the type they expect.
     * 
     * @param item The item to get a typed object for
     * @return The typed object
     */
    protected abstract Object getTypedItem(Item item);
}
