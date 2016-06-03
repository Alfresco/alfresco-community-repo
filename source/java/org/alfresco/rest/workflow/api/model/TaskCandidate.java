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