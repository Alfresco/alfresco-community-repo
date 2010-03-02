/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.action.evaluator;

import org.alfresco.service.cmr.avmsync.AVMDifference;
import org.alfresco.web.bean.repository.Node;

public class WCMConflictEvaluator extends WCMWorkflowDeletedEvaluator
{

    /* (non-Javadoc)
     * @see org.alfresco.web.action.evaluator.WCMWorkflowDeletedEvaluator#evaluate(org.alfresco.web.bean.repository.Node)
     */
	public boolean evaluate(final Node node)
	{
		if (!super.evaluate(node))
		{
			return false;
		}
		Integer diff = (Integer)node.getProperties().get("avmDiff");
		if (diff == null)
		{
			return true;
		}
		if (diff == AVMDifference.CONFLICT)
		{
			return false;
		}
		return true;
	}
}
