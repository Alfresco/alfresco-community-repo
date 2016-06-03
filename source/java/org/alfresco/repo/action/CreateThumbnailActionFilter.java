package org.alfresco.repo.action;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This filter class is used to identify equivalent create-thumbnail actions.
 * Two create-thumbnail actions are considered equivalent if they are on the same NodeRef and
 * if they have the same thumbnail-name parameter.
 * 
 * @author Neil McErlean
 */
public class CreateThumbnailActionFilter extends AbstractAsynchronousActionFilter
{
	private static final String PARAM_THUMBNAIL_NAME = "thumbnail-name";

	public int compare(OngoingAsyncAction nodeAction1, OngoingAsyncAction nodeAction2)
	{
		NodeRef n1 = nodeAction1.getNodeRef();
		NodeRef n2 = nodeAction2.getNodeRef();
		
		// Handle n1 potentially being null without triggering a NPE
		if (n1 == null && n2 == null)
		{
		    return 0;
		}
		else if(n1 == null && n2 != null)
		{
		    return 1;
		}
		
		// We can now do a regular compare without worrying about null nodes
		if (n1.equals(n2) == false)
		{
			return -1;
		}
		else
		{
			String thName1 = (String)nodeAction1.getAction().getParameterValue(PARAM_THUMBNAIL_NAME);
			String thName2 = (String)nodeAction2.getAction().getParameterValue(PARAM_THUMBNAIL_NAME);

			// Need to allow for the possibility of null values for thumbnail-name. (ALF-4946)
			if (thName1 == null && thName2 == null)
			{
				// Two null-valued thumbnail-names are considered equal under the compareTo contract.
				return 0;
			}
			else if (thName1 == null && thName2 != null)
			{
				return -1;
			}
			else if (thName1 != null && thName2 == null)
			{
				return 1;
			}
			else
			{
				return thName1.compareTo(thName2);
			}
		}
	}
}
