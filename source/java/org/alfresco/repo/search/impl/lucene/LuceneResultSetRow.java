package org.alfresco.repo.search.impl.lucene;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.AbstractResultSetRow;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
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
    
    private TenantService tenantService;

    /**
     * Wrap a position in a lucene Hits class with node support
     * 
     * @param resultSet LuceneResultSet
     * @param index int
     */
    public LuceneResultSetRow(LuceneResultSet resultSet, int index)
    {
        super(resultSet, index);
        
        tenantService = resultSet.getTenantService();
    }

    /**
     * Support to cache the document for this row
     * 
     * @return Document
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

    @Override
    public ChildAssociationRef getChildAssocRef()
    {
        Field field = getDocument().getField("PRIMARYPARENT");
        String primaryParent = null;
        if (field != null)
        {
            primaryParent = field.stringValue();
        }
        NodeRef childNodeRef = getNodeRef();
        NodeRef parentNodeRef = primaryParent == null ? null : tenantService.getBaseName(new NodeRef(primaryParent));
        return new ChildAssociationRef(getPrimaryAssocTypeQName(), parentNodeRef, getQName(), childNodeRef);
    }

    public NodeRef getNodeRef(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, NodeRef> getNodeRefs()
    {
        throw new UnsupportedOperationException();
    }

    public float getScore(String selectorName)
    {
        throw new UnsupportedOperationException();
    }

    public Map<String, Float> getScores()
    {
        throw new UnsupportedOperationException();
    }

    public int doc()
    {
        return ((LuceneResultSet)getResultSet()).doc(getIndex());  
    }
}
