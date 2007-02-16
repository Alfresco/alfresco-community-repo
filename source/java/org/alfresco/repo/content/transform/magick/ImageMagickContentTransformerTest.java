/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.repo.content.transform.magick;

import java.util.Collections;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.transform.AbstractContentTransformerTest;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.util.exec.RuntimeExec;

/**
 * @see org.alfresco.repo.content.transform.magick.JMagickContentTransformer
 * 
 * @author Derek Hulley
 */
public class ImageMagickContentTransformerTest extends AbstractContentTransformerTest
{
    private ImageMagickContentTransformer transformer;
    
    public void onSetUpInTransaction() throws Exception
    {
        RuntimeExec executer = new RuntimeExec();
        executer.setCommand("imconvert.exe ${source} ${options} ${target}");
        executer.setDefaultProperties(Collections.singletonMap("options", ""));
        
        transformer = new ImageMagickContentTransformer();
        transformer.setMimetypeService(mimetypeMap);
        transformer.setExecuter(executer);
        transformer.init();
    }
    
    /**
     * @return Returns the same transformer regardless - it is allowed
     */
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }
    
    public void testReliability() throws Exception
    {
        if (!transformer.isAvailable())
        {
            return;
        }
        double reliability = 0.0;
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_IMAGE_GIF, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertEquals("Mimetype should not be supported", 0.0, reliability);
        reliability = transformer.getReliability(MimetypeMap.MIMETYPE_IMAGE_GIF, MimetypeMap.MIMETYPE_IMAGE_JPEG);
        assertEquals("Mimetype should be supported", 1.0, reliability);
    }
}
