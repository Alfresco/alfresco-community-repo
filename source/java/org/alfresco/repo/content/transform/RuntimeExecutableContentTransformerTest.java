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
package org.alfresco.repo.content.transform;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.repo.content.transform.ContentTransformerRegistry.TransformationKey;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.BaseAlfrescoTestCase;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.xml.security.transforms.TransformationException;

/**
 * @see org.alfresco.repo.content.transform.RuntimeExecutableContentTransformer
 * 
 * @author Derek Hulley
 */
public class RuntimeExecutableContentTransformerTest extends BaseAlfrescoTestCase
{
    private RuntimeExecutableContentTransformer transformer;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        transformer = new RuntimeExecutableContentTransformer();
        // the command to execute
        RuntimeExec transformCommand = new RuntimeExec();
        Map<String, String> commandMap = new HashMap<String, String>(5);
        commandMap.put("Linux", "mv -f ${source} ${target}");
        commandMap.put(".*", "cmd /c copy /Y \"${source}\" \"${target}\"");
        transformCommand.setCommandMap(commandMap);
        transformCommand.setErrorCodes("1, 2");
        transformer.setTransformCommand(transformCommand);
        transformer.setMimetypeService(serviceRegistry.getMimetypeService());
        // set the explicit transformations
        List<TransformationKey> explicitTranformations = new ArrayList<TransformationKey>(1);
        explicitTranformations.add(
                new TransformationKey(MimetypeMap.MIMETYPE_TEXT_PLAIN, MimetypeMap.MIMETYPE_XML));
        transformer.setExplicitTransformations(explicitTranformations);
        
        // initialise so that it doesn't score 0
        transformer.register();
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
