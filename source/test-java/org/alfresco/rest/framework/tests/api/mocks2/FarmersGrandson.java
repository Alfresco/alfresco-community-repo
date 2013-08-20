package org.alfresco.rest.framework.tests.api.mocks2;

import org.alfresco.rest.framework.resource.EmbeddedEntityResource;
import org.alfresco.rest.framework.tests.api.mocks.GoatEntityResource;


/**
 * This inherits all Farmer's son's properties and ANNOTATIONS
 *
 * getSheepId is overidden but no annotation specified (OK)
 * getGoatId is overidden, a new annotation key is specified (THIS IS NOT RECOMMENDED)
 * getId is overidden but no annotation specified (OK)
 * @author Gethin James
 */
public class FarmersGrandson extends FarmersSon
{
    public FarmersGrandson(String id)
    {
        super(id);
    }

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.Farmer#getSheepId()
     */
    @Override
    public String getSheepId()
    {
        return super.getSheepId();
    }

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks.Farmer#getGoatId()
     */
    @Override
    @EmbeddedEntityResource(propertyName = "grandgoat", entityResource=GoatEntityResource.class)
    public String getGoatId()
    {
        return super.getGoatId();
    }

    /*
     * @see org.alfresco.rest.framework.tests.api.mocks2.FarmersSon#getId()
     */
    @Override
    public String getId()
    {
        return super.getId();
    }

}
