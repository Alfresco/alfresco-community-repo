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

package org.alfresco.repo.workflow.jbpm;

import java.util.Date;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.exe.Token;
import org.jbpm.graph.node.TaskNode;
import org.jbpm.job.ExecuteActionJob;
import org.jbpm.job.ExecuteNodeJob;

/**
 * @since 3.4
 * @author Nick Smith
 * 
 */
public class AlfrescoTaskNode extends TaskNode
{
    private static final long serialVersionUID = -5582345187516764993L;

    public AlfrescoTaskNode()
    {
        super();
    }

    public AlfrescoTaskNode(String name)
    {
        super(name);
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    protected ExecuteNodeJob createAsyncContinuationJob(Token token)
//    {
//        AlfrescoExecuteNodeJob job = new AlfrescoExecuteNodeJob(token);
//        job.setNode(this);
//        job.setDueDate(new Date());
//        job.setExclusive(isAsyncExclusive);
//        return job;
//    }

}
