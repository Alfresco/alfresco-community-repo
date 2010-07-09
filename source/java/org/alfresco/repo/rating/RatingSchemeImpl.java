/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

import org.alfresco.service.cmr.rating.RatingScheme;
import org.alfresco.service.cmr.rating.RatingServiceException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/*
 * @author Neil McErlean
 * @since 3.4
 */
public class RatingSchemeImpl implements RatingScheme, BeanNameAware, InitializingBean
{
    private final RatingSchemeRegistry ratingSchemeRegistry;
    
    private String name;
    private int minRating, maxRating;
    
    public RatingSchemeImpl(RatingSchemeRegistry registry)
    {
        this.ratingSchemeRegistry = registry;
    }
    
    public void init()
    {
        ratingSchemeRegistry.register(this.name, this);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String name)
    {
        this.name = name;
    }

    public void setMinRating(int minRating)
    {
        this.minRating = minRating;
    }

    public void setMaxRating(int maxRating)
    {
        this.maxRating = maxRating;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        if (this.minRating > this.maxRating)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Illegal rating limits for ").append(name)
               .append(". Min > Max. ")
               .append(minRating).append(" > ").append(maxRating);
            throw new RatingServiceException(msg.toString());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingScheme#getMaxRating()
     */
    public int getMaxRating()
    {
        return this.maxRating;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingScheme#getMinRating()
     */
    public int getMinRating()
    {
        return this.minRating;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.service.cmr.rating.RatingScheme#getName()
     */
    public String getName()
    {
        return this.name;
    }
    
    @Override
    public String toString()
    {
        StringBuilder msg = new StringBuilder();
        msg.append(this.getClass().getSimpleName())
           .append(" ").append(this.name)
           .append(" [").append(this.minRating)
           .append("..").append(this.maxRating)
           .append("]");
        return msg.toString();
    }
}
