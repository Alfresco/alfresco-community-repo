package org.alfresco.rest.search;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import java.util.List;

/**
 * Represents a facet interval.
 */

public class FacetInterval extends TestModel implements IRestModel<FacetInterval>
{
    private String field;
    private String label;
    private List<RestRequestFacetSetModel> sets;

    public FacetInterval()
    {
    }

    public FacetInterval(String field, String label, List<RestRequestFacetSetModel> sets)
    {
        this.field = field;
        this.label = label;
        this.sets = sets;
    }

    public String getField()
    {
        return field;
    }

    public String getLabel()
    {
        return label;
    }

    public List<RestRequestFacetSetModel> getSets()
    {
        return sets;
    }

    public void setField(String field)
    {
        this.field = field;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public void setSets(List<RestRequestFacetSetModel> sets)
    {
        this.sets = sets;
    }

    @Override
    public FacetInterval onModel()
    {
        return null;
    }

    @Override
    public ModelAssertion<FacetInterval> and()
    {
        return assertThat();
    }

    @Override
    public ModelAssertion<FacetInterval> assertThat()
    {
       return new ModelAssertion<FacetInterval>(this);
    }
}
