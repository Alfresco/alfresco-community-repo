package org.alfresco.rest.framework.tests.api.mocks3;

import org.alfresco.rest.framework.WebApiDeleted;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.tests.api.mocks.GrassEntityResource;

/**
 * Marks this entity resource as deleted.
 * @author Gethin James
 */
@WebApiDeleted
@EntityResource(name = "grass",title="Marked this Grass entity resource as deleted.")
public class GrassEntityResourceNowDeleted extends GrassEntityResource 
{
    
}