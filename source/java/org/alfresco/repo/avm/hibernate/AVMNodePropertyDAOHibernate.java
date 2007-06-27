package org.alfresco.repo.avm.hibernate;

import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMNodeProperty;
import org.alfresco.repo.avm.AVMNodePropertyDAO;
import org.alfresco.service.namespace.QName;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implemenation for DAO for AVMNodeProperties.
 * @author britt
 */
class AVMNodePropertyDAOHibernate extends HibernateDaoSupport 
    implements AVMNodePropertyDAO
{
    /**
     * Get a property by node and name.
     * @param node The AVMNode.
     * @param name The QName.
     * @return The found property or null.
     */
    public AVMNodeProperty get(AVMNode node, QName name)
    {
        Query query = 
            getSession().createQuery(
                    "from AVMNodePropertyImpl anp where anp.node = :node and anp.name = :name");
        query.setEntity("node", node);
        query.setParameter("name", name);
        return (AVMNodeProperty)query.uniqueResult();
    }

    /**
     * Get all properties owned by the given node.
     * @param node The AVMNode.
     * @return A List of properties.
     */
    @SuppressWarnings("unchecked")
    public List<AVMNodeProperty> get(AVMNode node)
    {
        Query query = 
            getSession().createQuery(
                    "from AVMNodePropertyImpl anp where anp.node = :node");
        query.setEntity("node", node);
        return (List<AVMNodeProperty>)query.list();
    }

    /**
     * Save a property.
     * @param prop The property to save.
     */
    public void save(AVMNodeProperty prop)
    {
        getSession().save(prop);
    }
    
    /**
     * Update a property entry.
     * @param prop The property.
     */
    public void update(AVMNodeProperty prop)
    {
        // Do nothing for Hibernate.
    }
    
    /**
     * Delete all properties associated with a node.
     * @param node The AVMNode whose properties should be deleted.
     */
    public void deleteAll(AVMNode node)
    {
        Query delete =
            getSession().createQuery("delete from AVMNodePropertyImpl anp where anp.node = :node");
        delete.setEntity("node", node);
        delete.executeUpdate();
    }
    
    /**
     * Delete the given property from the given node.
     * @param node The node to delete the property to delete.
     * @param name The name of the property to delete.
     */
    public void delete(AVMNode node, QName name)
    {
        Query delete =
            getSession().createQuery("delete from AVMNodePropertyImpl anp where anp.node = :node " + 
                                     "and name = :name");
        delete.setEntity("node", node);
        delete.setParameter("name", name);
        delete.executeUpdate();
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.avm.AVMNodePropertyDAO#iterate()
     */
    @SuppressWarnings("unchecked")
    public Iterator<AVMNodeProperty> iterate()
    {
        Query query =
            getSession().createQuery("from AVMNodePropertyImpl anp");
        return (Iterator<AVMNodeProperty>)query.iterate();
    }
}
