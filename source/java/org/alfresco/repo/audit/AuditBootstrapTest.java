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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.repo.audit;

import junit.framework.TestCase;

import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * Tests that auditing is loaded properly on repository startup.
 * 
 * @see AuditBootstrap
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class AuditBootstrapTest extends TestCase
{
    private static final String APPLICATION_REPOSITORY = "Alfresco Repository";
    
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private AuditModelRegistry auditModelRegistry;
    
    @Override
    public void setUp() throws Exception
    {
        auditModelRegistry = (AuditModelRegistry) ctx.getBean("auditModel.registry");
    }
    
    public void testSetUp()
    {
        // Just here to fail if the basic startup fails
    }
    
    public void testGetModelId()
    {
        Long repoId = auditModelRegistry.getAuditModelId(APPLICATION_REPOSITORY);
        assertNotNull("No audit model ID for " + APPLICATION_REPOSITORY, repoId);
    }
}
