/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.encoding;

import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.parser.txt.CharsetDetector;
import org.apache.tika.parser.txt.CharsetMatch;

/**
 * Uses Apache Tika as a fallback encoding detector
 * 
 * @since 3.4
 * @author Nick Burch
 */
public class TikaCharsetFinder extends AbstractCharactersetFinder
{
    private static Log logger = LogFactory.getLog(TikaCharsetFinder.class);
    
    private int threshold = 35;
    
    @Override
    protected Charset detectCharsetImpl(byte[] buffer) throws Exception
    {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(buffer);
        CharsetMatch match = detector.detect();

        if(match != null && match.getConfidence() > threshold)
        {
            try
            {
                return Charset.forName(match.getName());
            }
            catch(UnsupportedCharsetException e)
            {
                logger.info("Charset detected as " + match.getName() + " but the JVM does not support this, detection skipped");
            }
        }
        return null;
    }

    /**
     * Return the matching threshold before we decide that
     *  what we detected is a good match. In the range
     *  0-100.
     */
    public int getThreshold()
    {
        return threshold;
    }

    /**
     * At what point do we decide our match is good enough?
     * In the range 0-100. If we don't reach the threshold,
     *  we'll decline, and either another finder will work on
     *  it or the fallback encoding will be taken.
     */
    public void setThreshold(int threshold)
    {
        if(threshold < 0)
            threshold = 0;
        if(threshold > 100)
            threshold = 100;
        
        this.threshold = threshold;
    }
    
}
