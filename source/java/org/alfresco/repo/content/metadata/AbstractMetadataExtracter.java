/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.content.metadata;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.alfresco.service.namespace.QName;

/**
 * 
 * @author Jesper Steen Møller
 */
abstract public class AbstractMetadataExtracter implements MetadataExtracter
{

    private Set<String> mimetypes;
    private double reliability;
    private long extractionTime;

    protected AbstractMetadataExtracter(String mimetype, double reliability, long extractionTime)
    {
        this.mimetypes = Collections.singleton(mimetype);
        this.reliability = reliability;
        this.extractionTime = extractionTime;
    }

    protected AbstractMetadataExtracter(Set<String> mimetypes, double reliability, long extractionTime)
    {
        this.mimetypes = mimetypes;
        this.reliability = reliability;
        this.extractionTime = extractionTime;
    }

    public double getReliability(String sourceMimetype)
    {
        if (mimetypes.contains(sourceMimetype))
            return reliability;
        else
            return 0.0;
    }

    public long getExtractionTime()
    {
        return extractionTime;
    }

    /**
     * Examines a value or string for nulls and adds it to the map (if
     * non-empty)
     * 
     * @param prop Alfresco's <code>ContentModel.PROP_</code> to set.
     * @param value Value to set it to
     * @param destination Map into which to set it
     * @return true, if set, false otherwise
     */
    protected boolean trimPut(QName prop, Object value, Map<QName, Serializable> destination)
    {
        if (value == null)
            return false;
        if (value instanceof String)
        {
            String svalue = ((String) value).trim();
            if (svalue.length() > 0)
            {
                destination.put(prop, svalue);
                return true;
            }
            return false;
        }
        else if (value instanceof Serializable)
        {
            destination.put(prop, (Serializable) value);
        }
        else
        {
            destination.put(prop, value.toString());
        }
        return true;
    }
}
