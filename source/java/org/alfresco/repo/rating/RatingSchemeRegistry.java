/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */

package org.alfresco.repo.rating;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.rating.RatingScheme;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class maintains a registry of all known {@link RatingScheme rating schemes} in the system.
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingSchemeRegistry
{
    private static final Log log = LogFactory.getLog(RatingSchemeRegistry.class);
    
    Map<String, RatingScheme> ratingSchemes = new HashMap<String, RatingScheme>();

    public void register(String name, RatingScheme ratingScheme)
    {
        ratingSchemes.put(name, ratingScheme);
        if (log.isDebugEnabled())
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Registering ")
               .append(ratingScheme);

            log.debug(msg.toString());
        }
    }

    /**
     * This method returns an unmodifiable map of the registered rating schemes.
     * @return
     */
    public Map<String, RatingScheme> getRatingSchemes()
    {
        return Collections.unmodifiableMap(ratingSchemes);
    }
}
