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
import org.alfresco.repo.search.impl.lucene.ParseException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Get the CMIS object type id property
 * 
 * @author andyh
 *
 */
public class ObjectTypeIdPropertyAccessor extends AbstractNamedPropertyAccessor
{

    public Serializable getProperty(NodeRef nodeRef)
    {
        QName typeQName = getCMISMapping().getCmisType(getServiceRegistry().getNodeService().getType(nodeRef));
        CMISScope scope;
        if(getCMISMapping().isValidCmisDocument(typeQName))
        {
            scope = CMISScope.DOCUMENT;
        }
        else if(getCMISMapping().isValidCmisFolder(typeQName))
        {
            scope = CMISScope.FOLDER;
        }
        else
        {
            scope = CMISScope.UNKNOWN;
        }
        return getCMISMapping().getCmisTypeId(scope, typeQName).getTypeId();
    }

    @Override
    public String getPropertyName()
    {
        return CMISMapping.PROP_OBJECT_TYPE_ID;
    }
    
    @Override
    public CMISScope getScope()
    {
       return CMISScope.OBJECT;
    }
   
    /* (non-Javadoc)
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneEquality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.lang.String, java.io.Serializable)
     */
    public Query buildLuceneEquality(LuceneQueryParser lqp, String propertyName, Serializable value) throws ParseException
    {
        // TODO: version label form
        String asString =  DefaultTypeConverter.INSTANCE.convert(String.class, value);
        return lqp.getFieldQuery("TYPE", asString);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneExists(org.alfresco.repo.search.impl.lucene.LuceneQueryParser,
     *      java.lang.String, java.lang.Boolean)
     */
    public Query buildLuceneExists(LuceneQueryParser lqp, String propertyName, Boolean not) throws ParseException
    {
        if (not)
        {
            return new TermQuery(new Term("NO_TOKENS", "__"));
        }
        else
        {
            return new MatchAllDocsQuery();
        }
    }
    
    

}
