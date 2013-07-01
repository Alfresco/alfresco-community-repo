/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.web.app.servlet;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import net.sf.acegisecurity.context.ContextHolder;

/**
 * This JSF phase listener clears security context after finishing rendering of each request to back-end bean. This action is required due to conflict in HTTP request processing
 * threads in application server thread pool. Not cleaned up security context becomes shared between Alfresco Explorer and CMIS.<br />
 * <br />
 * See "<a href="https://issues.alfresco.com/jira/browse/MNT-467 ">cmisatom URL (opencmis backed by Apache Chemistry OpenCMIS) does not support External authentication</a>" and
 * "<a href="https://issues.alfresco.com/jira/browse/MNT-8725">Security context for Alfresco Explorer is not being cleaned up after processing a request</a>" for more details
 * 
 * @since 4.1.4
 * @author Dmitry Velichkevich
 */
public class SecurityContextCleanupPhaseListener implements PhaseListener
{
    private static final long serialVersionUID = 1L;

    public SecurityContextCleanupPhaseListener()
    {
    }

    @Override
    public void afterPhase(PhaseEvent phaseevent)
    {
        ContextHolder.setContext(null);
    }

    @Override
    public void beforePhase(PhaseEvent phaseevent)
    {
    }

    @Override
    public PhaseId getPhaseId()
    {
        return PhaseId.RENDER_RESPONSE;
    }
}
