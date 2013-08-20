/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.workflow.api.model;

import org.activiti.engine.task.IdentityLink;
import org.apache.commons.lang.StringUtils;

public class TaskCandidate
{
    String candidateId;
    String candidateType;
    
    public TaskCandidate()
    {
    }

    public TaskCandidate(IdentityLink identityLink)
    {
        if (StringUtils.isNotEmpty(identityLink.getUserId())) {
            candidateId = identityLink.getUserId();
            candidateType = "user";
        } else {
            candidateId = identityLink.getGroupId();
            candidateType = "group";
        }
    }

    public String getCandidateId()
    {
        return candidateId;
    }

    public void setCandidateId(String candidateId)
    {
        this.candidateId = candidateId;
    }

    public String getCandidateType()
    {
        return candidateType;
    }

    public void setCandidateType(String candidateType)
    {
        this.candidateType = candidateType;
    }
}