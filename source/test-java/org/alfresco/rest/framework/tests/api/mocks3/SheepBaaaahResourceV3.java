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

import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.tests.api.mocks.Sheep;
import org.alfresco.rest.framework.tests.api.mocks.SheepNoActionEntityResource;

/**
 * A mock version 3 relationship resource.  This is attached to the SheepNoActionEntityResource action (version 1).
 * In version 1, SheepNoActionEntityResource did no action.
 * In version 2, SheepNoActionEntityResource was overridden by SheepWasNoActionEntityResource and given an action
 * In this version (3) - a relationship resource is being added to the ORIGNAL entity (which has been overridden).
 * 
 * It is expected that this is correct behavior.  This relationship resource could have been attached to either
 * the SheepNoActionEntityResource or the SheepWasNoActionEntityResource with the same result.  This is because
 * they both use the same entity key (name="sheepnoaction")
 * 
 * @author Gethin James
 */
@RelationshipResource(name = "v3isaresource", entityResource=SheepNoActionEntityResource.class, title = "Sheep Version 3 resource")
public class SheepBaaaahResourceV3 implements RelationshipResourceAction.ReadById<Sheep>
{

    @Override
    public Sheep readById(String entityResourceId, String id, Parameters parameters)
    {
        return new Sheep("Z2");
    }


}
