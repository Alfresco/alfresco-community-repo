/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.rest.framework.tests.api.mocks4;

import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * Simple mock for RelationshipResource for relation TypeA1 to TypeB2
 *
 * @author janv
 */
@RelationshipResource(name = "relation-a1-b2", entityResource=TypeA1EntityResource.class, title = "Relation Type A1 to Type B2")
public class TypeA1toB2RelationshipResource implements RelationshipResourceAction.ReadById<TypeB2>
{
    // GET /type-a1/a1/relation-a1-b2/b2
    @Override
    public TypeB2 readById(String typeA1EntityResourceId, String typeB2EntityResourceId, Parameters parameters)
    {
        return new TypeB2("b2");
    }
}
