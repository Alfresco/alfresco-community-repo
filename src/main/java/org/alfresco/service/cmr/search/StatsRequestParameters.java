/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.service.cmr.search;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO class representing Stats request
 */
public class StatsRequestParameters
{
    private final String field;
    private final String label;
    private final List<Float> percentiles;
    private final Boolean min;
    private final Boolean max;
    private final Boolean sum;
    private final Boolean countValues;
    private final Boolean missing;
    private final Boolean sumOfSquares;
    private final Boolean mean;
    private final Boolean stddev;
    private final Boolean distinctValues;
    private final Boolean countDistinct;
    private final Boolean cardinality;
    private final Float cardinalityAccuracy;
    private final List<String> excludeFilters;

    @JsonCreator
    public StatsRequestParameters(
                 @JsonProperty("field") String field,
                 @JsonProperty("label") String label,
                 @JsonProperty("percentiles") List<Float> percentiles,
                 @JsonProperty("min") Boolean min,
                 @JsonProperty("max") Boolean max,
                 @JsonProperty("sum") Boolean sum,
                 @JsonProperty("countValues") Boolean countValues,
                 @JsonProperty("missing") Boolean missing,
                 @JsonProperty("sumOfSquares") Boolean sumOfSquares,
                 @JsonProperty("mean") Boolean mean,
                 @JsonProperty("stddev") Boolean stddev,
                 @JsonProperty("distinctValues") Boolean distinctValues,
                 @JsonProperty("countDistinct") Boolean countDistinct,
                 @JsonProperty("cardinality") Boolean cardinality,
                 @JsonProperty("cardinalityAccuracy") Float cardinalityAccuracy,
                 @JsonProperty("excludeFilters") List<String> excludeFilters)
    {
        this.field = field;
        this.label = label;
        this.percentiles = percentiles == null? Collections.emptyList():percentiles;

        this.min = min == null?true:min;
        this.max = max == null?true:max;
        this.sum = sum == null?true:sum;
        this.countValues = countValues == null?true:countValues;
        this.missing = missing == null?true:missing;
        this.sumOfSquares = sumOfSquares == null?true:sumOfSquares;
        this.mean = mean == null?true:mean;
        this.stddev = stddev == null?true:stddev;

        this.distinctValues = distinctValues == null?false:distinctValues;
        this.countDistinct = countDistinct == null?false:countDistinct;
        this.cardinality = cardinality == null?false:cardinality;
        this.cardinalityAccuracy = cardinalityAccuracy == null?0.3f:cardinalityAccuracy;
        this.excludeFilters = excludeFilters == null? Collections.emptyList():excludeFilters;
    }

    public String getField()
    {
        return field;
    }

    public String getLabel()
    {
        return label;
    }

    public List<Float> getPercentiles()
    {
        return percentiles;
    }

    public Boolean getDistinctValues()
    {
        return distinctValues;
    }

    public Boolean getCountDistinct()
    {
        return countDistinct;
    }

    public Boolean getCardinality()
    {
        return cardinality;
    }

    public Float getCardinalityAccuracy()
    {
        return cardinalityAccuracy;
    }

    public List<String> getExcludeFilters()
    {
        return excludeFilters;
    }

    public Boolean getMin()
    {
        return min;
    }

    public Boolean getMax()
    {
        return max;
    }

    public Boolean getSum()
    {
        return sum;
    }

    public Boolean getCountValues()
    {
        return countValues;
    }

    public Boolean getMissing()
    {
        return missing;
    }

    public Boolean getSumOfSquares()
    {
        return sumOfSquares;
    }

    public Boolean getMean()
    {
        return mean;
    }

    public Boolean getStddev()
    {
        return stddev;
    }

    @Override
    public String toString()
    {
        return "StatsRequestParameters{" + "field='" + field + '\'' + ", label='" + label + '\'' + ", percentiles=" + percentiles + ", min=" + min
                    + ", max=" + max + ", sum=" + sum + ", countValues=" + countValues + ", missing=" + missing + ", sumOfSquares=" + sumOfSquares + ", mean="
                    + mean + ", stddev=" + stddev + ", distinctValues=" + distinctValues + ", countDistinct=" + countDistinct + ", cardinality="
                    + cardinality + ", cardinalityAccuracy=" + cardinalityAccuracy + ", excludeFilters=" + excludeFilters + '}';
    }
}
