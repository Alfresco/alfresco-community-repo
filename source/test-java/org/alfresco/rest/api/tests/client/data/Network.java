package org.alfresco.rest.api.tests.client.data;

import java.util.List;

public interface Network
{
	String getId();
	Boolean getIsEnabled();
	String getCreatedAt();
	List<Quota> getQuotas();
	String getSubscriptionLevel();
    Boolean isPaidNetwork();
}
