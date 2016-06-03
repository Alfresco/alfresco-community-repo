package org.alfresco.rest.api.impl.activities;

public class SubscriptionsActivitySummaryProcessor extends BaseActivitySummaryProcessor
{
	//{"lastName":"Glover","userFirstName":"David","followerLastName":"Glover","userUserName":"david.bowie@alfresco.com","followerFirstName":"Steve",
	// "userLastName":"Bowie","followerUserName":"steven.glover@alfresco.com","firstName":"Steve","tenantDomain":"alfresco.com"}
	
	@Override
	protected Change processEntry(String key, Object value)
	{
		Change change = super.processEntry(key, value);

		if(key.equals("userUserName"))
		{
			change = new ChangeKey(key, "userPersonId");
		}
		
		if(key.equals("followerUserName"))
		{
			change = new ChangeKey(key, "followerPersonId");
		}

		return change;
	}

}
