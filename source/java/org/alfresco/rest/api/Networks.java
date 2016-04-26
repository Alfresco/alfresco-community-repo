package org.alfresco.rest.api;

import org.alfresco.rest.api.model.Network;
import org.alfresco.rest.api.model.PersonNetwork;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;

public interface Networks
{
	public Network validateNetwork(String networkId);
    public Network getNetwork(String networkId);
    public PersonNetwork getNetwork(String personId, String networkId);
    public CollectionWithPagingInfo<PersonNetwork> getNetworks(String personId, Paging paging);
}
