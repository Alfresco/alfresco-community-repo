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
import org.alfresco.repo.forms.Item;

/**
 * Interface definition of a form processor which is responsible
 * for generating a Form representation of a data source, for example a
 * repository node, a task or an XML schema and for persisting the
 * form data back to the data source.
 *
 * @author Gavin Cornwell
 */
public interface FormProcessor
{
    /**
     * Determines whether this form processor is applicable for
     * the supplied item
     * 
     * @param item The item the form is being generated for
     * @return true if the processor is applicable
     */
    public boolean isApplicable(Item item);
    
    /**
     * Determines whether this form processor is active
     * 
     * @return true if the processor is active
     */
    public boolean isActive();
    
    /**
     * Returns a Form representation for an item
     * 
     * @param item The item to generate a Form object for
     * @param fields Restricted list of fields to include, null
     *               indicates all possible fields for the item 
     *               should be included
     * @param forcedFields List of field names from 'fields' list
     *                     that should be forcibly included, it is
     *                     up to the form processor implementation
     *                     to determine how to enforce this 
     * @return The Form representation
     */
    public Form generate(Item item, List<String> fields, List<String> forcedFields);
    
    /**
     * Persists the given object representing the form data
     * for an item
     *
     * @param item The item to generate a Form object for
     * @param data An object representing the data of the form
     */
    public void persist(Item item, FormData data);
}
