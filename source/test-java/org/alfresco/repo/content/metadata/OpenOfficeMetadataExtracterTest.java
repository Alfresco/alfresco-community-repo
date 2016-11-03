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
/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.namespace.QName;


/**
 * Note - this test can sometimes fail if run on its own, as there
 *  can be a race condition with the OO process. Try running it as 
 *  part of a suite if so, that normally seems to fix it!  
 * 
 * @author Jesper Steen Møller
 */
public class OpenOfficeMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private OpenOfficeMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        OpenOfficeMetadataWorker worker = (OpenOfficeMetadataWorker) ctx.getBean("extracter.worker.OpenOffice");
        
        extracter = new OpenOfficeMetadataExtracter();
        extracter.setMimetypeService(mimetypeMap);
        extracter.setDictionaryService(dictionaryService);
        extracter.setWorker(worker);
        extracter.init();
    }

    /**
     * @return Returns the same extracter regardless - it is allowed
     */
    protected MetadataExtracter getExtracter()
    {
        return extracter;
    }

    public void testReliability() throws Exception
    {
        if (!extracter.isConnected())
        {
            return;
        }
        
        for (String mimetype : OpenOfficeMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            assertTrue("Expected above zero reliability", extracter.isSupported(mimetype));
        }
    }

    public void testSupportedMimetypes() throws Exception
    {
    	// If this test method is run on its own, then it may run to completion before the OOo connection is reconnected.
    	// To fully run this test method (with full execution of the various extractions) you need to debug it,
    	// put a breakpoint below (at extracter.isConnected()) and wait for
    	// "[alfresco.util.OpenOfficeConnectionTester] The OpenOffice connection was re-established" in the log before
    	// proceeding. Otherwise the extracter is not "connected" and the tests are short-circuited.
    	//
    	// When run on the build server, the timings are such that the OOo connection is available.
        if (!extracter.isConnected())
        {
            return;
        }
        for (String mimetype : OpenOfficeMetadataExtracter.SUPPORTED_MIMETYPES)
        {
            testExtractFromMimetype(mimetype);
        }
    }

    /**
     * Only run the check if we have a connection
     *  to an OpenOffice instance
     */
    protected void testCommonMetadata(String mimetype,
         Map<QName, Serializable> properties) {
       if(extracter.isConnected()) {
           super.testCommonMetadata(mimetype, properties);
       }
   }
    
    protected boolean skipAuthorCheck(String mimetype)
    {
    	// The following 'quick' files have no author/creator property and so should not
    	// have its value checked.
    	List<String> mimeTypesWithNoAuthor = new ArrayList<String>();
    	mimeTypesWithNoAuthor.add(MimetypeMap.MIMETYPE_STAROFFICE5_IMPRESS);
    	mimeTypesWithNoAuthor.add(MimetypeMap.MIMETYPE_OPENOFFICE1_IMPRESS);
    	
    	return mimeTypesWithNoAuthor.contains(mimetype);
    }


   /** Extractor only does the usual basic three properties */
    public void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties) {}
}
