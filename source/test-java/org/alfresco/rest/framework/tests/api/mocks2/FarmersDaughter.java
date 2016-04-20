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
