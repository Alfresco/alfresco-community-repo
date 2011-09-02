/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.invitation.site;

import org.alfresco.repo.invitation.NominatedInvitationProcess;
import org.alfresco.service.cmr.invitation.Invitation;

/**
 * The Site Private Invitation Process implements the PrivateInvitatonProcess for 
 * Web Sites. 
 */
public class SiteNominatedInvitationProcess implements NominatedInvitationProcess
{
    /**
     * inviter starts the invitation process
     */
    public Invitation invite(Invitation request, String reason)
    {
        return null;
    }
    
    /**
     * invitee accepts this request
     * @param request
     */
    public void accept(Invitation request)
    {
    }
    
    /**
     * invitee rejects this request
     * @param request
     */
    public void reject(Invitation request)
    {
    }
    
    /**
     * cancel this request
     */
    public void cancel (Invitation request)
    {
    }
}
