package org.alfresco.repo.search.impl.solr.facet.facetsresponse;

import java.util.HashMap;
import java.util.Map;

/**
 * A metric with one value
 */
public class SimpleMetric implements Metric
{
    private final METRIC_TYPE type;
    private final Map<String, Object> value = new HashMap<>(1);

    public SimpleMetric(METRIC_TYPE type, String val)
    {
        this.type = type;
        value.put(type.toString(), val);
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
}
