package org.alfresco.rest.model.body;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.rest.core.assertion.ModelAssertion;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestNodeLockBodyModel extends TestModel implements IRestModel<RestNodeLockBodyModel>
{
    
    @JsonProperty(value = "entry")
    RestNodeLockBodyModel model;

    @Override
    public ModelAssertion<RestNodeLockBodyModel> and()
    {
        return new ModelAssertion<RestNodeLockBodyModel>(this);
    }

    @Override
    public ModelAssertion<RestNodeLockBodyModel> assertThat()
    {
        return assertThat();
    }

    @Override
    public RestNodeLockBodyModel onModel()
    {
        return model;
    }

    @JsonProperty
    private int timeToExpire;

    @JsonProperty
    private String type;
    
    @JsonProperty
    private String lifetime;
    
    public String getLifetime()
    {
        return lifetime;
    }

    public void setLifetime(String lifetime)
    {
        this.lifetime = lifetime;
    }

    public int getTimeToExpire()
    {
        return timeToExpire;
    }

    /*
     * Set in seconds lock time
     * if lock time = 0 or not set, the lock never expires
     */
    public void setTimeToExpire(int timeToExpire)
    {
        this.timeToExpire = timeToExpire;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }



}
