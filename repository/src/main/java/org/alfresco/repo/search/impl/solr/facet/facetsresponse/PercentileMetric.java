/*-
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.solr.facet.facetsresponse;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * A metric with one value
 */
public class PercentileMetric implements Metric
{
    private static Log logger = LogFactory.getLog(PercentileMetric.class);
    private final METRIC_TYPE type;
    private final Map<String, Object> value = new HashMap<>(1);

    public PercentileMetric(METRIC_TYPE type, Object val)
    {
        this.type = type;
        try
        {
            JSONArray jsonArray = (JSONArray) val;
            Map<String, Number> percentiles = new HashMap<>(jsonArray.length() / 2);

            for (int i = 0, length = jsonArray.length(); i < length; i++)
            {
                percentiles.put(jsonArray.getString(i++), jsonArray.getDouble(i));
            }
            value.put(type.toString(), percentiles);
        }
        catch (ClassCastException cce)
        {
            logger.debug("ClassCastException for " + val);
        }
        catch (JSONException e)
        {
            logger.debug("Failed to process percentile for " + val + " " + e.getMessage());
        }
    }

    @Override
    public METRIC_TYPE getType()
    {
        return type;
    }

    @Override
    public Map<String, Object> getValue()
    {
        return value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        PercentileMetric that = (PercentileMetric) o;

        if (type != that.type)
            return false;
        if (!value.equals(that.value))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
