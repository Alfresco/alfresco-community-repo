package org.alfresco.rest.framework.tests.api.mocks;

import org.alfresco.rest.framework.resource.UniqueId;

/**
 * Simple mock pojo for serialization testing.
 * 
 * This is an invalid class. It has a uniqueId getter method but no setter.
 *
 * @author Gethin James
 */
public class UniqueIdMethodButNoSetter
{

    private String name;

    @UniqueId
    public String getName()
    {
        return this.name;
    }
}
