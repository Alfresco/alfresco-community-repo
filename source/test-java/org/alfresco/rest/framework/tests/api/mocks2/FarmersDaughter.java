package org.alfresco.rest.framework.tests.api.mocks2;

import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;
import org.alfresco.rest.framework.tests.api.mocks.GrassEntityResource;

/**
 * This inherits all Farmer's properties and ANNOTATIONS
 * It adds a new embedded entity which is fine, but it adds another @UniqueId annotation
 * which is invalid because its already on Farmer
 *
 * @author Gethin James
 */
public class FarmersDaughter extends Farmer
{
    private boolean likesFlowers;
    String grassId;

    public FarmersDaughter(String id)
    {
        super(id);
        likesFlowers = true;
    }
    
    @EmbeddedEntityResource(propertyName = "specialgrass", entityResource=GrassEntityResource.class)
    public String getGrassId()
    {
        return this.grassId;
    }

    /**
     * @return the likesFlowers
     */
    @UniqueId
    public boolean getLikesFlowers()
    {
        return this.likesFlowers;
    }

    /**
     * @param likesFlowers the likesFlowers to set
     */
    public void setLikesFlowers(boolean likesFlowers)
    {
        this.likesFlowers = likesFlowers;
    }

    /**
     * @param grassId the grassId to set
     */
    public void setGrassId(String grassId)
    {
        this.grassId = grassId;
    }

}
