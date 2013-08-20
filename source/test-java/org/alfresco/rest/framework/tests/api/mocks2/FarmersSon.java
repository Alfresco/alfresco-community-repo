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
