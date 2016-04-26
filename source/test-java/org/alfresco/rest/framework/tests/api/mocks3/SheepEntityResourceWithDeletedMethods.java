/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.WebApiDeleted;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource;

/**
 * This no longer does all the actions.
 * 
 * The original SheepEntityResource had 4 actions: update, readById, readAll, delete
 * update and delete methods are marked as deleted
 * readAll is untouched - (only present in SheepEntityResource);
 * readById is overridden.
 *
 * @author Gethin James
 */
public class SheepEntityResourceWithDeletedMethods extends SheepEntityResource 
{

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource#update(java.lang.String, org.alfresco.rest.framework.tests.api.mocks.Sheep)
     */
    @Override
    @WebApiDeleted
    public Sheep update(String id, Sheep entity, Parameters parameters)
    {
        return null; //No implementation because its deleted
    }

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource#readById(java.lang.String)
     */
    @Override
    public Sheep readById(String id, Parameters parameters)
    {
        return new Sheep("v3_"+id);
    }

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource#delete(java.lang.String)
     */
    @Override
    @WebApiDeleted
    public void delete(String id, Parameters parameters)
    {
        //No implementation because its deleted
    }

}
