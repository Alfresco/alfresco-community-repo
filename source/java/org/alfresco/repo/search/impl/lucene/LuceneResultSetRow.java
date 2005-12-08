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
