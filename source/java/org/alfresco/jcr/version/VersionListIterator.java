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
package org.alfresco.jcr.version;

import java.util.List;

import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.alfresco.jcr.util.AbstractRangeIterator;


/**
 * Alfresco implementation of a Property Iterator
 * 
 * @author David Caruana
 */
public class VersionListIterator extends AbstractRangeIterator
    implements VersionIterator
{
    private VersionHistoryImpl versionHistory;
    private List<org.alfresco.service.cmr.version.Version> versions;
    
    
    /**
     * Construct
     * 
     * @param context  session context
     * @param versions  version list
     */
    public VersionListIterator(VersionHistoryImpl versionHistory, List<org.alfresco.service.cmr.version.Version> versions)
    {
        this.versionHistory = versionHistory;
        this.versions = versions;
    }
    
    /*
     *  (non-Javadoc)
     * @see javax.jcr.version.VersionIterator#nextVersion()
     */
    public Version nextVersion()
    {
        long position = skip();
        org.alfresco.service.cmr.version.Version version = versions.get((int)position);
        return new VersionImpl(versionHistory, version).getProxy();
    }
    
    /* (non-Javadoc)
     * @see javax.jcr.RangeIterator#getSize()
     */
    public long getSize()
    {
        return versions.size();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    public Object next()
    {
        return nextVersion();
    }

}
