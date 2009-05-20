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
package org.alfresco.repo.search.impl.lucene;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.AbstractResultSetRow;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.namespace.QName;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * A row in a result set. Created on the fly.
 * 
 * @author Andy Hind
 * 
 */
public class LuceneResultSetRow extends AbstractResultSetRow
{
    /**
     * The current document - cached so we do not get it for each value
     */
    private Document document;

    /**
     * Wrap a position in a lucene Hits class with node support
     * 
     * @param resultSet
     * @param position
     */
    public LuceneResultSetRow(LuceneResultSet resultSet, int index)
    {
        super(resultSet, index);
    }

    /**
     * Support to cache the document for this row
     * 
     * @return
     */
    public Document getDocument()
    {
        if (document == null)
        {
            document = ((LuceneResultSet) getResultSet()).getDocument(getIndex());
        }
        return document;
    }

    /*
     * ResultSetRow implementation
     */

    protected Map<QName, Serializable> getDirectProperties()
    {
        LuceneResultSet lrs = (LuceneResultSet) getResultSet();
        return lrs.getNodeService().getProperties(lrs.getNodeRef(getIndex()));
    }

    public Serializable getValue(Path path)
    {
        // TODO: implement path base look up against the document or via the
        // node service
        throw new UnsupportedOperationException();
    }

    public QName getQName()
    {
        Field field = getDocument().getField("QNAME");
        if (field != null)
        {
            String qname = field.stringValue();
            if((qname == null) || (qname.length() == 0))
            {
                return null;
            }
            else
            {
               return QName.createQName(qname);
            }
        }
        else
        {
            return null;
        }
    }

    public QName getPrimaryAssocTypeQName()
    {
        
        Field field = getDocument().getField("PRIMARYASSOCTYPEQNAME");
        if (field != null)
        {
            String qname = field.stringValue();
            return QName.createQName(qname);
        }
        else
        {
            return ContentModel.ASSOC_CHILDREN;
        }
    }

    public ChildAssociationRef getChildAssocRef()
    {
        Field field = getDocument().getField("PRIMARYPARENT");
        String primaryParent = null;
        if (field != null)
        {
            primaryParent = field.stringValue();
        }
        NodeRef childNodeRef = getNodeRef();
        NodeRef parentNodeRef = primaryParent == null ? null : new NodeRef(primaryParent);
        return new ChildAssociationRef(getPrimaryAssocTypeQName(), parentNodeRef, getQName(), childNodeRef);
    }

}
