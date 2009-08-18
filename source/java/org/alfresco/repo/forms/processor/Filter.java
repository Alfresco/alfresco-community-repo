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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.forms.processor;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;

/**
 * Interface definition for a filter which is called before and after
 * a form is generated and persisted.
 *
 * @author Gavin Cornwell
 */
public interface Filter
{
    /**
     * Determines whether the filter is active
     * 
     * @return true if the filter is active
     */
    public boolean isActive();
    
    /**
     * Callback used to indicate that a form is about to be generated for
     * the given items and fields.
     * 
     * <p>
     * NOTE: Filters all relating to the same type of form can cast the Object
     * to a more appropriate object, for example all the Node based handlers
     * can expect a NodeRef object and therefore cast to that.
     * 
     * @param item The item to generate a Form for
     * @param fields Restricted list of fields to include
     * @param forcedFields List of fields to forcibly include
     * @param form The Form object
     * @param @param context Map representing optional context that
     *                can be used during retrieval of the form
     */
    public void beforeGenerate(Object item, List<String> fields, List<String> forcedFields, 
                Form form, Map<String, Object> context);
    
    /**
     * Callback used to indicate that a form has just been generated for
     * the given items and fields.
     * 
     * <p>
     * NOTE: Filters all relating to the same type of form can cast the Object
     * to a more appropriate object, for example all the Node based handlers
     * can expect a NodeRef object and therefore cast to that.
     * 
     * @param item The item to generate a Form for
     * @param fields Restricted list of fields to include
     * @param forcedFields List of fields to forcibly include
     * @param form The Form object
     * @param context Map representing optional context that
     *                can be used during retrieval of the form
     */
    public void afterGenerate(Object item, List<String> fields, List<String> forcedFields, 
                Form form, Map<String, Object> context);
    
    /**
     * Callback used to indicate that the given form data is about to be 
     * persisted for the given item.
     * 
     * <p>
     * NOTE: Filters all relating to the same type of form can cast the item Object
     * to a more appropriate object, for example all the Node based handlers
     * can expect a NodeRef object and therefore cast to that.
     * 
     * @param item The item to persist the form data for
     * @param data The form data
     */
    public void beforePersist(Object item, FormData data);
    
    /**
     * Callback used to indicate that the given form data was just persisted
     * for the item and the given persistedObject was created or modified.
     * 
     * <p>
     * NOTE: Filters all relating to the same type of form can cast the item 
     * and persistedObject Objects to a more appropriate object, for example 
     * all the Node based handlers can expect a NodeRef object and therefore 
     * cast to that.
     * 
     * @param item The item to persist the form data for
     * @param data The form data
     * @param persistedObject The object created or modified as a result of 
     *        the form persistence
     */
    public void afterPersist(Object item, FormData data, Object persistedObject);
}
