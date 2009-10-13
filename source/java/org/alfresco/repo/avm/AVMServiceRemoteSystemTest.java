/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing" */

package org.alfresco.repo.avm;

import org.alfresco.repo.remote.ClientTicketHolder;
import org.alfresco.service.cmr.avmsync.AVMSyncService;
import org.alfresco.service.cmr.remote.AVMRemote;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.util.NameMatcher;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Remote system tests of AVM (AVMSyncService & AVMService) - requires running repo
 */
public class AVMServiceRemoteSystemTest extends AVMServiceLocalTest
{
    private final static String ADMIN_UN = "admin";
    private final static String ADMIN_PW = "admin";
    
    @Override
    protected void setUp() throws Exception
    {
        if (fContext == null)
        {
            // remote (non-embedded) test setup
            fContext = new FileSystemXmlApplicationContext("config/alfresco/remote-avm-test-context.xml");
            fService = (AVMRemote)fContext.getBean("avmRemote");
            fSyncService = (AVMSyncService)fContext.getBean("avmSyncService");
            excluder = (NameMatcher) fContext.getBean("globalPathExcluder");
            
            AuthenticationService authService = (AuthenticationService)fContext.getBean("authenticationService");
            authService.authenticate(ADMIN_UN, ADMIN_PW.toCharArray());
            String ticket = authService.getCurrentTicket();
            ((ClientTicketHolder)fContext.getBean("clientTicketHolder")).setTicket(ticket);
        }
        
        super.setUp();
    }
}
