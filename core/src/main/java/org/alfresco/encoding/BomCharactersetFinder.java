/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.encoding;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Byte Order Marker encoding detection.
 * 
 * @since 2.1
 * @author Pacific Northwest National Lab
 * @author Derek Hulley
 */
public class BomCharactersetFinder extends AbstractCharactersetFinder
{
    private static Log logger = LogFactory.getLog(BomCharactersetFinder.class);
    
    @Override
    public void setBufferSize(int bufferSize)
    {
        logger.warn("Setting the buffersize has no effect for charset finder: " + BomCharactersetFinder.class.getName());
    }

    /**
     * @return          Returns 64
     */
    @Override
    protected int getBufferSize()
    {
        return 64;
    }

    /**
     * Just searches the Byte Order Marker, i.e. the first three characters for a sign of
     * the encoding.
     */
    protected Charset detectCharsetImpl(byte[] buffer) throws Exception
    {
        Charset charset = null;
        ByteArrayInputStream bis = null;
        try
        {
            bis = new ByteArrayInputStream(buffer);
            bis.mark(3);
            char[] byteHeader = new char[3];
            InputStreamReader in = new InputStreamReader(bis);
            int bytesRead = in.read(byteHeader);
            bis.reset();

            if (bytesRead < 2)
            {
                // ASCII
                charset = Charset.forName("Cp1252");
            }
            else if (
                    byteHeader[0] == 0xFE &&
                    byteHeader[1] == 0xFF)
            {
                // UCS-2 Big Endian
                charset = Charset.forName("UTF-16BE");
            }
            else if (
                    byteHeader[0] == 0xFF &&
                    byteHeader[1] == 0xFE)
            {
                // UCS-2 Little Endian
                charset = Charset.forName("UTF-16LE");
            }
            else if (
                    bytesRead >= 3 &&
                    byteHeader[0] == 0xEF &&
                    byteHeader[1] == 0xBB &&
                    byteHeader[2] == 0xBF)
            {
                // UTF-8
                charset = Charset.forName("UTF-8");
            }
            else
            {
                // No idea
                charset = null;
            }
            // Done
            return charset;
        }
        finally
        {
            if (bis != null)
            {
                try { bis.close(); } catch (Throwable e) {}
            }
        }
    }
}
