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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.wcm.preview;

import java.text.MessageFormat;
import java.util.Map;

import org.alfresco.wcm.AbstractWCMServiceImplTest;
import org.alfresco.wcm.webproject.WebProjectInfo;

/**
 * Preview URI Service implementation unit test
 * 
 * @author janv
 */
public class PreviewURIServiceImplTest extends AbstractWCMServiceImplTest
{
    //
    // services
    //
    private PreviewURIServiceRegistry previewURIServiceRegistry;
    private PreviewURIService prevService;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        // Get the required services
        previewURIServiceRegistry = (PreviewURIServiceRegistry)ctx.getBean("previewURIServiceRegistry");
        prevService = (PreviewURIService)ctx.getBean("WCMPreviewURIService");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public void testSetup()
    {
        Map<String, PreviewURIServiceProvider> prevURIServiceProviders = previewURIServiceRegistry.getPreviewURIServiceProviders();
        
        System.out.println(prevURIServiceProviders.keySet());
    }
    
    public void testDefaultAndNOOP()
    {
        // Create a web project
        WebProjectInfo wpInfo = wpService.createWebProject(TEST_WEBPROJ_DNS+"-previewSimple", TEST_WEBPROJ_NAME+"-previewSimple", TEST_WEBPROJ_TITLE, TEST_WEBPROJ_DESCRIPTION);
        assertNotNull(wpInfo);
        
        String stagingPreviewURL = prevService.getPreviewURI(wpInfo.getStagingStoreName(), null);
        assertNotNull(stagingPreviewURL);
        
        String expectedURL = MessageFormat.format(VirtualisationServerPreviewURIService.PREVIEW_SANDBOX_URL, 
                wpInfo.getStoreId(), 
                VirtualisationServerPreviewURIService.DEFAULT_VSERVER_IP,
                ""+VirtualisationServerPreviewURIService.DEFAULT_VSERVER_PORT);
        
        assertEquals(expectedURL, stagingPreviewURL);
        
        String nullProvName = null;
        Map<String, PreviewURIServiceProvider> prevURIServiceProviders = previewURIServiceRegistry.getPreviewURIServiceProviders();
        for (Map.Entry<String, PreviewURIServiceProvider> entry : prevURIServiceProviders.entrySet())
        {
            PreviewURIServiceProvider prov = entry.getValue();
            if (prov instanceof NullPreviewURIService)
            {
                nullProvName = entry.getKey();
                break;
            }
        }
        assertNotNull(nullProvName);
        
        wpInfo.setPreviewProviderName(nullProvName);
        wpService.updateWebProject(wpInfo);
        
        stagingPreviewURL = prevService.getPreviewURI(wpInfo.getStagingStoreName(), null); // fails - returns 2 ?
        assertNull(stagingPreviewURL);
    }
}
