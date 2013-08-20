package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.WebApiDeleted;
import org.alfresco.rest.framework.resource.RelationshipResource;
import org.alfresco.rest.framework.tests.api.mocks.SheepEntityResource;

/**
 * This relationship has been deleted
 * 
 * @author Gethin James
 */
@WebApiDeleted
@RelationshipResource(name = "blacksheep",entityResource=SheepEntityResource.class, title = "blacksheep NOW DELETED relationship")
public class SheepBlackSheepResourceIsNoMore {

}
