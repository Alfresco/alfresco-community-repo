/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.repo.content.transform;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.ContentMinimalContextTestSuite;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;

/**
 * @see org.alfresco.repo.content.transform.RuntimeExecutableContentTransformerWorker
 * 
 * @author Derek Hulley
 */
public class RuntimeExecutableContentTransformerTest extends BaseAlfrescoTestCase
{
    private ContentTransformer transformer;
    
    @Override
    protected void setUpContext() {
       // We use a smaller context
       ctx = ContentMinimalContextTestSuite.getContext();
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        RuntimeExecutableContentTransformerWorker worker = new RuntimeExecutableContentTransformerWorker();
        // the command to execute
        RuntimeExec transformCommand = new RuntimeExec();
        Map<String, String> commandMap = new HashMap<String, String>(5);
        commandMap.put("Mac OS X", "mv -f ${source} ${target}");
        commandMap.put("Linux", "mv -f ${source} ${target}");
        commandMap.put(".*", "cmd /c copy /Y \"${source}\" \"${target}\"");
        transformCommand.setCommandMap(commandMap);
        transformCommand.setErrorCodes("1, 2");
        worker.setTransformCommand(transformCommand);
        worker.setMimetypeService(serviceRegistry.getMimetypeService());
        // set the explicit transformations
        List<ExplictTransformationDetails> explicitTranformations = new ArrayList<ExplictTransformationDetails>(1);
        explicitTranformations.add(
                new ExplictTransformationDetails(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_XML));
        worker.setExplicitTransformations(explicitTranformations);
        
        // initialise so that it doesn't score 0
        worker.afterPropertiesSet();
        
        TransformerDebug transformerDebug = (TransformerDebug) ctx.getBean("transformerDebug");
        TransformerConfig transformerConfig = (TransformerConfig) ctx.getBean("transformerConfig");

        ProxyContentTransformer transformer = new ProxyContentTransformer();
        transformer.setMimetypeService(serviceRegistry.getMimetypeService());
        transformer.setTransformerDebug(transformerDebug);
        transformer.setTransformerConfig(transformerConfig);
        transformer.setWorker(worker);
        this.transformer = transformer;
    }

    public void testCopyCommand() throws Exception
    {
        String content = "<A><B></B></A>";
        // create the source
        File sourceFile = TempFileProvider.createTempFile(getName() + "_", ".txt");
        ContentWriter tempWriter = new FileContentWriter(sourceFile);
        tempWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        tempWriter.putContent(content);
        ContentReader reader = tempWriter.getReader(); 
        // create the target
        File targetFile = TempFileProvider.createTempFile(getName() + "_", ".xml");
        ContentWriter writer = new FileContentWriter(targetFile);
        writer.setMimetype(MimetypeMap.MIMETYPE_XML);
        
        // do the transformation
        transformer.transform(reader, writer);   // no options on the copy
        
        // make sure that the content was copied over
        ContentReader checkReader = writer.getReader();
        String checkContent = checkReader.getContentString();
        assertEquals("Content not copied", content, checkContent);
    }
}
