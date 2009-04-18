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

/**
 * Interface definition for a handler which is used to process all or part
 * of a form, if it is applicable to the item being processed and it's active
 *
 * @author Gavin Cornwell
 */
public interface Handler
{
    /**
     * Determines whether the handler is applicable for the given item.
     * <p>
     * Handlers all relating to the same type of form can cast the Object
     * to a more appropriate object, for example all the Node based handlers
     * can expect a NodeRef object and therefore cast to that.
     * 
     * @param item An object representing the item to handle
     * @return true if the handler is applicable
     */
    public boolean isApplicable(Object item);
    
    /**
     * Determines whether the handler is active
     * 
     * @return true if the handler is active
     */
    public boolean isActive();
    
    /**
     * Handles the generation of a Form. 
     * <p>
     * Handlers all relating to the same type of form can cast the Object
     * to a more appropriate object, for example all the Node based handlers
     * can expect a NodeRef object and therefore cast to that.
     * 
     * @see org.alfresco.repo.forms.processor.FormProcessor#generate(org.alfresco.repo.forms.Item, java.util.List, java.util.List)
     * @param item The item to generate a Form for
     * @param fields Restricted list of fields to include
     * @param forcedFields List of fields to forcibly include
     * @param form The Form object
     * @return The modified Form object
     */
    public Form handleGenerate(Object item, List<String> fields, List<String> forcedFields, Form form);
    
    /**
     * Handles the persistence of form data for the given item.
     * <p>
     * Handlers all relating to the same type of form can cast the item Object
     * to a more appropriate object, for example all the Node based handlers
     * can expect a NodeRef object and therefore cast to that.
     * 
     * @param item The item to persist the form data for
     * @param data The form data
     */
    public void handlePersist(Object item, FormData data);
}
