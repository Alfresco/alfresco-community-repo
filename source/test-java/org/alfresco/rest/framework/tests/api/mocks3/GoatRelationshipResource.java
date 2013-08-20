package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.resource.actions.interfaces.RelationshipResourceAction;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;

/**
 * The goat has a herd
 * 
 * @author Gethin James
 */
@RelationshipResource(name = "herd",entityResource=GoatEntityResourceForV3.class, title = "Goat Herd")
public class GoatRelationshipResource implements RelationshipResourceAction.Read<Herd> 
{
    @Override
    public CollectionWithPagingInfo<Herd> readAll(String entityResourceId, Parameters params)
    {
        return CollectionWithPagingInfo.asPagedCollection(new Herd("bigun"));
    }

}
