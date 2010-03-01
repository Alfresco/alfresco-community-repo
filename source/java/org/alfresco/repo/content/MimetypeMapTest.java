/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.content;

import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.content.MimetypeMap
 * 
 * @author Derek Hulley
 */
public class MimetypeMapTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private MimetypeService mimetypeService;
    
    @Override
    public void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        mimetypeService = serviceRegistry.getMimetypeService();
    }
    
    public void testExtensions() throws Exception
    {
        Map<String, String> extensionsByMimetype = mimetypeService.getExtensionsByMimetype();
        Map<String, String> mimetypesByExtension = mimetypeService.getMimetypesByExtension();
        
        // plain text
        assertEquals("txt", extensionsByMimetype.get("text/plain"));
        assertEquals("text/plain", mimetypesByExtension.get("txt"));
        assertEquals("text/plain", mimetypesByExtension.get("csv"));
        assertEquals("text/plain", mimetypesByExtension.get("java"));
        
        // JPEG
        assertEquals("jpg", extensionsByMimetype.get("image/jpeg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpeg"));
        assertEquals("image/jpeg", mimetypesByExtension.get("jpe"));
        
        // MS Word
        assertEquals("doc", extensionsByMimetype.get("application/msword"));
        assertEquals("application/msword", mimetypesByExtension.get("doc"));
        
        // Star Office
        assertEquals("sds", extensionsByMimetype.get("application/vnd.stardivision.chart"));
    }
    
    public void testIsText() throws Exception
    {
        assertTrue(mimetypeService.isText(MimetypeMap.MIMETYPE_HTML));
    }
    
    public void testGetContentCharsetFinder() throws Exception
    {
        assertNotNull("No charset finder", mimetypeService.getContentCharsetFinder());
    }

}
