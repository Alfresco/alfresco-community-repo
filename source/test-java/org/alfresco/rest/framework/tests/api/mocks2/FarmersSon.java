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

import org.alfresco.rest.framework.resource.UniqueId;
import org.alfresco.rest.framework.tests.api.mocks.Farmer;

/**
 * This inherits all Farmer's properties and ANNOTATIONS and just adds
 * 1 field.
 * 
 * It overrides the farmer's getId method (which has a @UniqueId annotation)
 * specifying the annotation on this class is optional     
 *
 * @author Gethin James
 */
public class FarmersSon extends Farmer
{
    private boolean wearsGlasses;

    public FarmersSon(String id)
    {
        super(id);
        wearsGlasses = true;
    }

    /**
     * @return the wearsGlasses
     */
    public boolean isWearsGlasses()
    {
        return this.wearsGlasses;
    }

    /**
     * @param wearsGlasses the wearsGlasses to set
     */
    public void setWearsGlasses(boolean wearsGlasses)
    {
        this.wearsGlasses = wearsGlasses;
    }

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.Farmer#getId()
     */
    @Override
    @UniqueId
    public String getId()
    {
        return super.getId();
    }
}
