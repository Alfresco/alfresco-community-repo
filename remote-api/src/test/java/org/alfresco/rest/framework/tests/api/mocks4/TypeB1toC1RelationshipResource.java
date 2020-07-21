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
 * Simple mock for RelationshipResource for relation TypeB1 to TypeC1
 *
 * note: with entityResource = TypeA1toB1RelationshipResource (rather than just TypeBEntityResource)
 *
 * @author janv
 */
@RelationshipResource(name = "relation-b1-c1", entityResource= TypeA1toB1RelationshipResource.class, title = "Relation Type B1 to Type C1")
public class TypeB1toC1RelationshipResource implements RelationshipResourceAction.ReadById<TypeC1>
{
    // GET /type-a1/a1/type-b1/b1/relation-b1-c1/c1
    @Override
    public TypeC1 readById(String typeBEntityResourceId, String typeCEntityResourceId, Parameters parameters)
    {
        return new TypeC1("c1");
    }
}
