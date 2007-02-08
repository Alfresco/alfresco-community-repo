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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.content.metadata;

import net.sf.jooreports.openoffice.connection.OpenOfficeConnection;
import net.sf.jooreports.openoffice.connection.SocketOpenOfficeConnection;


/**
 * @author Jesper Steen Møller
 */
public class OpenOfficeMetadataExtracterTest extends AbstractMetadataExtracterTest
{
    private OpenOfficeMetadataExtracter extracter;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        OpenOfficeConnection connection = new SocketOpenOfficeConnection();
        
        extracter = new OpenOfficeMetadataExtracter();
        extracter.setMimetypeService(mimetypeMap);
        extracter.setConnection(connection);
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
}
