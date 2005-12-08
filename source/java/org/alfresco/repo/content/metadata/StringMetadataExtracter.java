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
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Jesper Steen Møller
 */
public class StringMetadataExtracter implements MetadataExtracter
{
    public static final String PREFIX_TEXT = "text/";

    private static final Log logger = LogFactory.getLog(StringMetadataExtracter.class);

    public double getReliability(String sourceMimetype)
    {
        if (sourceMimetype.startsWith(PREFIX_TEXT))
            return 0.1;
        else
            return 0.0;
    }

    public long getExtractionTime()
    {
        return 1000;
    }

    public void extract(ContentReader reader, Map<QName, Serializable> destination) throws ContentIOException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("No metadata extracted for " + reader.getMimetype());
        }
    }
}
