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
package org.alfresco.repo.forms;

import java.util.List;
import java.util.Map;


/**
 * Form service API.
 * <p>
 * This service API is designed to support the public facing Form APIs
 * 
 * @author Gavin Cornwell
 */
public interface FormService
{
    /**
     * Returns a form representation of the given item,
     * all known fields for the item are included.
     * 
     * @param item The item to get a form for
     * @return The Form representation
     */
    public Form getForm(Item item);
    
    /**
     * Returns a form representation of the given item,
     * all known fields for the item are included.
     * 
     * @param item The item to get a form for
     * @param context Map representing optional context that
     *                can be used during retrieval of the form
     * @return The Form representation
     */
    public Form getForm(Item item, Map<String, Object> context);
    
    /**
     * Returns a form representation of the given item consisting 
     * only of the given fields.
     * 
     * @param item The item to get a form for
     * @param fields Restricted list of fields to include, null
     *               indicates all possible fields for the item 
     *               should be included
     * @return The Form representation
     */
    public Form getForm(Item item, List<String> fields);
    
    /**
     * Returns a form representation of the given item consisting 
     * only of the given fields.
     * 
     * @param item The item to get a form for
     * @param fields Restricted list of fields to include, null
     *               indicates all possible fields for the item 
     *               should be included
     * @param context Map representing optional context that
     *                can be used during retrieval of the form
     * @return The Form representation
     */
    public Form getForm(Item item, List<String> fields, Map<String, Object> context);
    
    /**
     * Returns a form representation of the given item consisting 
     * only of the given fields.
     * 
     * @param item The item to get a form for
     * @param fields Restricted list of fields to include, null
     *               indicates all possible fields for the item 
     *               should be included
     * @param forcedFields List of field names from 'fields' list
     *                     that should be forcibly included, it is
     *                     up to the form processor implementation
     *                     to determine how to enforce this
     * @return The Form representation
     */
    public Form getForm(Item item, List<String> fields, List<String> forcedFields);
    
    /**
     * Returns a form representation of the given item consisting 
     * only of the given fields.
     * 
     * @param item The item to get a form for
     * @param fields Restricted list of fields to include, null
     *               indicates all possible fields for the item 
     *               should be included
     * @param forcedFields List of field names from 'fields' list
     *                     that should be forcibly included, it is
     *                     up to the form processor implementation
     *                     to determine how to enforce this
     * @param context Map representing optional context that
     *                can be used during retrieval of the form
     * @return The Form representation
     */
    public Form getForm(Item item, List<String> fields, List<String> forcedFields, Map<String, Object> context);
    
    /**
     * Persists the given form representation for the given item.
     * 
     * @param item The item to persist the form for
     * @param data An object representing the form data to persist
     * @return The object persisted
     */
    public Object saveForm(Item item, FormData data);
}
