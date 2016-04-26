package org.alfresco.rest.api.impl.activities;

public class SiteActivitySummaryProcessor extends BaseActivitySummaryProcessor
{
	@Override
	protected Change processEntry(String key, Object value)
	{
		Change change = super.processEntry(key, value);

		if(key.equals("memberUserName"))
		{
			change = new ChangeKey(key, "memberPersonId");
		}

		return change;
	}

}
