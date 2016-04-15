/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.forms.processor;

import java.util.List;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;

/**
 * Interface definition for a filter which is called before and after
 * a form is generated and persisted.
 *
 * @author Gavin Cornwell
 */
@AlfrescoPublicApi
public interface Filter<ItemType, PersistType>
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
     * @param context Map representing optional context that
     *                can be used during retrieval of the form
     */
    public void beforeGenerate(ItemType item, List<String> fields, List<String> forcedFields, 
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
    public void afterGenerate(ItemType item, List<String> fields, List<String> forcedFields, 
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
    public void beforePersist(ItemType item, FormData data);
    
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
    public void afterPersist(ItemType item, FormData data, PersistType persistedObject);
}
