package org.alfresco.repo.avm.hibernate;

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
public class AVMNodePropertyDAOHibernate extends HibernateDaoSupport 
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
        query.setCacheable(true);
        query.setCacheRegion("Property.Lookup");
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
        query.setCacheable(true);
        query.setCacheRegion("Properties.Lookup");
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
}
