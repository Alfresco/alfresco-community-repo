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

import java.io.File;
import java.util.Map;

import magick.ImageInfo;
import magick.MagickImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Makes use of the {@link http://www.textmining.org/ TextMining} library to
 * perform conversions from MSWord documents to text.
 * 
 * @author Derek Hulley
 */
public class JMagickContentTransformer extends AbstractImageMagickContentTransformer
{
    private static final Log logger = LogFactory.getLog(JMagickContentTransformer.class);
    
    public JMagickContentTransformer()
    {
    }
    
    /**
     * Uses the <b>JMagick</b> library to perform the transformation
     * 
     * @param sourceFile
     * @param targetFile
     * @throws Exception
     */
    @Override
    protected void transformInternal(File sourceFile, File targetFile, Map<String, Object> options) throws Exception
    {
        ImageInfo imageInfo = new ImageInfo(sourceFile.getAbsolutePath());
        MagickImage image = new MagickImage(imageInfo);
        image.setFileName(targetFile.getAbsolutePath());
        image.writeImage(imageInfo);
    }
}
