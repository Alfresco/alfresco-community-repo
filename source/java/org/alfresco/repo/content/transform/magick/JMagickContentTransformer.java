/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
