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
package org.alfresco.repo.invitation;


import java.util.Map;

import org.alfresco.repo.invitation.activiti.RejectModeratedInviteDelegate;
import org.alfresco.repo.invitation.site.AbstractInvitationAction;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * JBPM Action fired when a moderated invitation is rejected.
 * Note - uses a classpath template, rather than a data dictionary template,
 *  so behaves slightly differently to many other mail actions, and can't
 *  currently be localised easily.
 *  
 * <b>Same behaviour as {@link RejectModeratedInviteDelegate}</b>
 */
public class ModeratedActionReject extends AbstractInvitationAction
{
    private static final long serialVersionUID = 4377660284993206875L;
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public void execute(final ExecutionContext executionContext) throws Exception
    {
        Map<String, Object> vars = executionContext.getContextInstance().getVariables();
        inviteHelper.rejectModeratedInvitation(vars);
    }
}