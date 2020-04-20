/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

package org.alfresco.repo.content.transform;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.management.subsystems.ChildApplicationContextFactory;
import org.alfresco.repo.rendition2.SynchronousTransformClient;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.TempFileProvider;

import java.io.File;
import java.util.Collections;

/**
 * @see org.alfresco.repo.content.transform.EMLTransformer
 * 
 * @author Jamal Kaabi-Mofrad
 *
 * @deprecated The transformations code is being moved out of the codebase and replaced by the new async RenditionService2 or other external libraries.
 */
@Deprecated
public class EMLTransformerTest extends AbstractContentTransformerTest
{
    private static final String QUICK_EML_CONTENT = "Gym class featuring a brown fox and lazy dog";

    private static final String QUICK_EML_CONTENT_SPANISH_UNICODE = "El r\u00E1pido zorro marr\u00F3n salta sobre el perro perezoso";
    
    private static final String QUICK_EML_WITH_ATTACHMENT_CONTENT =  "Mail with attachment content";
    
    private static final String QUICK_EML_ATTACHMENT_CONTENT =  "File attachment content";
    
    private static final String QUICK_EML_ALTERNATIVE_CONTENT =  "alternative plain text";
    
    private static final String QUICK_EML_NESTED_ALTERNATIVE_CONTENT =  "nested alternative plain text";
    
    private static final String HTML_SPACE_SPECIAL_CHAR = "&nbsp;";

    private EMLTransformer transformer;

    private ContentTransformerRegistry registry;
    private SynchronousTransformClient synchronousTransformClient;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        transformer = new EMLTransformer();
        transformer.setMimetypeService(mimetypeService);
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
        RemoteTransformerClient remoteTransformerClient = new RemoteTransformerClient("miscRemoteTransformerClient", "http://localhost:8090/");
        transformer.setRemoteTransformerClient(remoteTransformerClient);

        registry = (ContentTransformerRegistry) ctx.getBean("contentTransformerRegistry");
        synchronousTransformClient = (SynchronousTransformClient) ctx.getBean("synchronousTransformClient");
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
     * Test transforming a valid eml file to pdf using complex transformer ("Rfc822ToPdf") - eg. for HTML5 preview
     */
    public void testRFC822ToPdf() throws Exception
    {
        String sourceMimetype = MimetypeMap.MIMETYPE_RFC822;
        String targetMimetype = MimetypeMap.MIMETYPE_PDF;

        // force Transformers subsystem to start (this will also load the ContentTransformerRegistry - including complex/dynamic pipelines)
        // note: a call to contentService.getTransformer would also do this .. even if transformer cannot be found (returned as null)
        ChildApplicationContextFactory transformersSubsystem = (ChildApplicationContextFactory) ctx.getBean("Transformers");
        transformersSubsystem.start();

        assertNotNull(registry.getTransformer("transformer.complex.Rfc822ToPdf"));

        // note: txt -> pdf currently uses OpenOffice/LibreOffice
        if (! isOpenOfficeWorkerAvailable())
        {
            // no connection
            System.err.println("ooWorker available - skipping testRFC822ToPdf !!");
            return;
        }

        AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.getSystemUserName());
        assertTrue(synchronousTransformClient.isSupported(sourceMimetype, -1, null, targetMimetype, Collections.emptyMap(), null, null));

        String sourceExtension = mimetypeService.getExtension(sourceMimetype);
        String targetExtension = mimetypeService.getExtension(targetMimetype);

        File emlSourceFile = loadQuickTestFile("eml");
        ContentReader sourceReader = new FileContentReader(emlSourceFile);

        // make a writer for the target file
        File targetFile = TempFileProvider.createTempFile(getClass().getSimpleName() + "_"
                + getName() + "_" + sourceExtension + "_", "." + targetExtension);
        ContentWriter targetWriter = new FileContentWriter(targetFile);

        // do the transformation
        sourceReader.setMimetype(sourceMimetype);
        targetWriter.setMimetype(targetMimetype);
        synchronousTransformClient.transform(sourceReader, targetWriter, Collections.emptyMap(), null, null);

        ContentReader targetReader = new FileContentReader(targetFile);
        assertTrue(targetReader.getSize() > 0);
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
        String contentStr = reader2.getContentString();
        assertTrue(contentStr.contains(QUICK_EML_CONTENT_SPANISH_UNICODE));
    }
    
    /**
     * Test transforming a valid eml with an attachment to text; attachment should be ignored
     */
    public void testRFC822WithAttachmentToText() throws Exception
    {
        File emlSourceFile = loadQuickTestFile("attachment.eml");
        File txtTargetFile = TempFileProvider.createTempFile("test3", ".txt");
        ContentReader reader = new FileContentReader(emlSourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_RFC822);
        ContentWriter writer = new FileContentWriter(txtTargetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        transformer.transform(reader, writer);

        ContentReader reader2 = new FileContentReader(txtTargetFile);
        reader2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        String contentStr = reader2.getContentString();
        assertTrue(contentStr.contains(QUICK_EML_WITH_ATTACHMENT_CONTENT));
        assertTrue(!contentStr.contains(QUICK_EML_ATTACHMENT_CONTENT));
    }
    
    /**
     * Test transforming a valid eml with minetype multipart/alternative to text
     */
    public void testRFC822AlternativeToText() throws Exception
    {
        File emlSourceFile = loadQuickTestFile("alternative.eml");
        File txtTargetFile = TempFileProvider.createTempFile("test4", ".txt");
        ContentReader reader = new FileContentReader(emlSourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_RFC822);
        ContentWriter writer = new FileContentWriter(txtTargetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        transformer.transform(reader, writer);

        ContentReader reader2 = new FileContentReader(txtTargetFile);
        reader2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        String contentStr = reader2.getContentString();
        assertTrue(contentStr.contains(QUICK_EML_ALTERNATIVE_CONTENT));
    }
    
    /**
     * Test transforming a valid eml with nested mimetype multipart/alternative to text
     */
    public void testRFC822NestedAlternativeToText() throws Exception
    {
        File emlSourceFile = loadQuickTestFile("nested.alternative.eml");
        File txtTargetFile = TempFileProvider.createTempFile("test5", ".txt");
        ContentReader reader = new FileContentReader(emlSourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_RFC822);
        ContentWriter writer = new FileContentWriter(txtTargetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        transformer.transform(reader, writer);

        ContentReader reader2 = new FileContentReader(txtTargetFile);
        reader2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        String contentStr = reader2.getContentString();
        assertTrue(contentStr.contains(QUICK_EML_NESTED_ALTERNATIVE_CONTENT));
    }
    
    /**
     * Test transforming a valid eml with a html part containing html special characters to text
     */
    public void testHtmlSpecialCharsToText() throws Exception
    {
        File emlSourceFile = loadQuickTestFile("htmlChars.eml");
        File txtTargetFile = TempFileProvider.createTempFile("test6", ".txt");
        ContentReader reader = new FileContentReader(emlSourceFile);
        reader.setMimetype(MimetypeMap.MIMETYPE_RFC822);
        ContentWriter writer = new FileContentWriter(txtTargetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

        transformer.transform(reader, writer);

        ContentReader reader2 = new FileContentReader(txtTargetFile);
        reader2.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        String contentStr = reader2.getContentString();
        assertTrue(!contentStr.contains(HTML_SPACE_SPECIAL_CHAR));
    }
}
