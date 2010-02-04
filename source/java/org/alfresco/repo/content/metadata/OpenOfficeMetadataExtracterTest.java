/*
 * Copyright (C) 2005 Jesper Steen Møller
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
 * http://www.alfresco.com/legal/licensing" */
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Map;

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
            double reliability = extracter.getReliability(mimetype);
            assertTrue("Expected above zero reliability", reliability > 0.0);
        }
    }

    public void testSupportedMimetypes() throws Exception
    {
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

   /** Extractor only does the usual basic three properties */
    public void testFileSpecificMetadata(String mimetype, Map<QName, Serializable> properties) {}
}
