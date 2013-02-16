/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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

package org.alfresco.repo.content.transform;

import java.io.File;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;

/**
 * @see org.alfresco.repo.content.transform.EMLTransformer
 * 
 * @author Jamal Kaabi-Mofrad
 */
public class EMLTransformerTest extends AbstractContentTransformerTest
{
    private static final String QUICK_EML_CONTENT = "Gym class featuring a brown fox and lazy dog";

    private static final String QUICK_EML_CONTENT_SPANISH_UNICODE = "El r\u00E1pido zorro marr\u00F3n salta sobre el perro perezoso";

    private EMLTransformer transformer;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        transformer = new EMLTransformer();
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
    }

    @Override
    protected ContentTransformer getTransformer(String sourceMimetype, String targetMimetype)
    {
        return transformer;
    }

    public void testIsTransformable() throws Exception
    {
        assertFalse(transformer.isTransformable(MimetypeMap.MIMETYPE_TEXT_PLAIN, -1, MimetypeMap.MIMETYPE_RFC822,
                new TransformationOptions()));
        assertTrue(transformer.isTransformable(MimetypeMap.MIMETYPE_RFC822, -1, MimetypeMap.MIMETYPE_TEXT_PLAIN,
                new TransformationOptions()));
    }

    /**
     * Test transforming a valid eml file to text
     */
    public void testRFC822ToText() throws Exception
    {
        File emlSourceFile = loadQuickTestFile("eml");
        File txtTargetFile = TempFileProvider.createTempFile("test", ".txt");
        ContentReader reader = new FileContentReader(emlSourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_RFC822);
        ContentWriter writer = new FileContentWriter(txtTargetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        transformer.transform(reader, writer);

        ContentReader reader2 = new FileContentReader(txtTargetFile);
        reader2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertTrue(reader2.getContentString().contains(QUICK_EML_CONTENT));
    }

    /**
     * Test transforming a non-ascii eml file to text
     */
    public void testNonAsciiRFC822ToText() throws Exception
    {
        File emlSourceFile = loadQuickTestFile("spanish.eml");
        File txtTargetFile = TempFileProvider.createTempFile("test2", ".txt");
        ContentReader reader = new FileContentReader(emlSourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_RFC822);
        ContentWriter writer = new FileContentWriter(txtTargetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        transformer.transform(reader, writer);

        ContentReader reader2 = new FileContentReader(txtTargetFile);
        reader2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertTrue(reader2.getContentString().contains(new String(QUICK_EML_CONTENT_SPANISH_UNICODE.getBytes("UTF-8"))));
    }
}
