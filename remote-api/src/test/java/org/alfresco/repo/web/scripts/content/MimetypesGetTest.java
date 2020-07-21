/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts.content;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.LocalTransformServiceRegistry;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.springframework.context.ApplicationContext;

/**
 * Tests the {@link MimetypesGet} endpoint
 */
public class MimetypesGetTest extends BaseWebScriptTest
{
    private ApplicationContext ctx;
    private LocalTransformServiceRegistry localTransformServiceRegistry;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ctx = getServer().getApplicationContext();
        localTransformServiceRegistry = (LocalTransformServiceRegistry) ctx.getBean("localTransformServiceRegistry");
    }
    
    /**
     * Tests the <code>mimetypesGet.getTransformer</code> method directly for
     * varefication of label text
     * 
     * @throws Exception
     */
    public void testGetTransformer() throws Exception
    {
        MimetypesGet mimetypesGet = new MimetypesGet();
        mimetypesGet.setLocalTransformServiceRegistry(localTransformServiceRegistry);

        String transformerName = mimetypesGet.getTransformer(MimetypeMap.MIMETYPE_PDF, 1000, MimetypeMap.MIMETYPE_PDF);
        assertEquals("PassThrough", transformerName);

        transformerName = mimetypesGet.getTransformer(MimetypeMap.MIMETYPE_PDF, 1000, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("PdfBox", transformerName);
    }
}
