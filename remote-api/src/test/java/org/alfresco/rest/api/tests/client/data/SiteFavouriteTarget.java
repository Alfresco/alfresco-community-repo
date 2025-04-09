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
package org.alfresco.rest.api.tests.client.data;

import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;

public class SiteFavouriteTarget implements FavouritesTarget
{
    private Site site;

    public SiteFavouriteTarget(Site site)
    {
        super();
        this.site = site;
    }

    public Site getSite()
    {
        return site;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JSONObject toJSON()
    {
        JSONObject favouriteJson = new JSONObject();
        favouriteJson.put("site", getSite().toJSON());
        return favouriteJson;
    }

    @Override
    public String toString()
    {
        return "SiteFavouritesTarget [site=" + site + "]";
    }

    @Override
    public void expected(Object o)
    {
        assertTrue(o instanceof SiteFavouriteTarget);

        SiteFavouriteTarget other = (SiteFavouriteTarget) o;
        site.expected(other.getSite());
    }

    public String getTargetGuid()
    {
        return site.getGuid();
    }
}
