/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.search;

/**
 * Basic POJO to represent an individual statistic
 * 
 * @author Gethin James
 * @since 5.0
 */
public class StatsResultStat {
    
    private String name;
    private final Long sum;
    private final Long count;
    private final Long min;
    private final Long max;
    private final Long mean;
    
    public StatsResultStat(String name, Long sum, Long count, Long min, Long max, Long mean)
    {
        super();
        this.name = name;
        this.sum = sum;
        this.count = count;
        this.min = min;
        this.max = max;
        this.mean = mean;
    }

    public String getName()
    {
        return this.name;
    }

    public Long getSum()
    {
        return this.sum;
    }

    public Long getCount()
    {
        return this.count;
    }

    public Long getMin()
    {
        return this.min;
    }

    public Long getMax()
    {
        return this.max;
    }

    public Long getMean()
    {
        return this.mean;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Stat [name=").append(this.name).append(", sum=").append(this.sum)
                    .append(", count=").append(this.count).append(", min=").append(this.min)
                    .append(", max=").append(this.max).append(", mean=").append(this.mean)
                    .append("]");
        return builder.toString();
    }


}