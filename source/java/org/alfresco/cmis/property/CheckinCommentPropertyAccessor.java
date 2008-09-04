/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.cmis.property;

import java.io.Serializable;

import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISScope;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.apache.lucene.search.Query;

/**
 * Accesor for the CMIS Checkin Comment 
 * 
 * @author andyh
 *
 */
public class CheckinCommentPropertyAccessor extends AbstractNamedPropertyAccessor
{

    public Serializable getProperty(NodeRef nodeRef)
    {
        Version version = getServiceRegistry().getVersionService().getCurrentVersion(nodeRef);
        if(version != null)
        {
            return version.getDescription();
        }
        else
        {
            return null;
        }
    }

    @Override
    public String getPropertyName()
    {
        return CMISMapping.PROP_CHECKIN_COMMENT;
    }
    
    @Override
    public CMISScope getScope()
    {
       return CMISScope.DOCUMENT;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneEquality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.lang.String, java.io.Serializable)
     */
    public Query buildLuceneEquality(LuceneQueryParser lqp, String propertyName, Serializable value)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneExists(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.lang.String, java.lang.Boolean)
     */
    public Query buildLuceneExists(LuceneQueryParser lqp, String propertyName, Boolean not)
    {
        return null;
    }
    
}
