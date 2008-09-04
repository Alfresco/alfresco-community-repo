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

import org.alfresco.cmis.CMISService;
import org.alfresco.cmis.dictionary.CMISMapping;
import org.alfresco.cmis.dictionary.CMISScope;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.search.impl.lucene.ParseException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Get the CMIS parent property
 * 
 * @author andyh
 *
 */
public class ParentPropertyAccessor extends AbstractNamedPropertyAccessor
{
    private CMISService cmisService;


    /**
     * @param cmisService
     */
    public void setCMISService(CMISService cmisService)
    {
        this.cmisService = cmisService;
    }

    
    public Serializable getProperty(NodeRef nodeRef)
    {
        if (nodeRef.equals(cmisService.getDefaultRootNodeRef()))
        {
            return null;
        }
        
        ChildAssociationRef car = getServiceRegistry().getNodeService().getPrimaryParent(nodeRef);
        if ((car != null) && (car.getParentRef() != null))
        {
            return car.getParentRef().toString();
        }
        else
        {
            return null;
        }
    }
    

    @Override
    public String getPropertyName()
    {
        return CMISMapping.PROP_PARENT;
    }

    
    @Override
    public CMISScope getScope()
    {
       return CMISScope.FOLDER;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.cmis.property.NamedPropertyAccessor#buildLuceneEquality(org.alfresco.repo.search.impl.lucene.LuceneQueryParser, java.lang.String, java.io.Serializable)
     */
    public Query buildLuceneEquality(LuceneQueryParser lqp, String propertyName, Serializable value) throws ParseException
    {
        // TODO: version label form
        
        Object converted = DefaultTypeConverter.INSTANCE.convert(getServiceRegistry().getDictionaryService().getDataType(DataTypeDefinition.NODE_REF), value);
        String asString =  DefaultTypeConverter.INSTANCE.convert(String.class, converted);
        
        return lqp.getFieldQuery("PARENT", asString);
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
            return new TermQuery(new Term("ISROOT", "T"));
        }
        else
        {
            return new MatchAllDocsQuery();
        }
    }

}
