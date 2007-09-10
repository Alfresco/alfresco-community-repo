/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.avm.wf;

import org.alfresco.repo.workflow.jbpm.JBPMSpringActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.BeanFactory;


/**
 * No-op stub for the no longer used AVMSubmittedApsect. 
 */
public class AVMClearSubmittedHandler extends JBPMSpringActionHandler 
{
    private static final long serialVersionUID = 4113360751217684995L;

    /**
     * Initialize service references.
     * @param factory The BeanFactory to get references from.
     */
    @Override
    protected void initialiseHandler(BeanFactory factory) 
    {
    }

    /**
     * Do the actual work.
     * @param executionContext The context to get stuff from.
     */
    public void execute(ExecutionContext executionContext) throws Exception 
    {
    }
}
