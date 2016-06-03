package org.alfresco.repo.domain.patch;

import java.util.List;
import java.util.Set;


import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

/**
 * Additional DAO services for patches
 *
 * @author janv
 * @author Derek Hulley
 * @since 3.2
 */
public interface PatchDAO
{
    // DM-related
    
    /**
     * @deprecated in 4.1: use {@link NodeDAO#getMaxNodeId()}
     */
    @Deprecated
    public long getMaxAdmNodeID();
    
    /**
     * Update all <b>alf_content_data</b> mimetype references.
     * 
     * @param oldMimetypeId     the ID to search for
     * @param newMimetypeId     the ID to change to
     * @return                  the number of rows affected
     */
    public int updateContentMimetypeIds(Long oldMimetypeId, Long newMimetypeId);
    
    /**
     * Update all <b>alf_node_properties</b> of 'sizeCurrent' name to have correct persisted type of Long.
     * 
     * @return                  the number of rows affected
     */
    public int updatePersonSizeCurrentType();
    
    /**
     * Query for a list of nodes that have a given type and share the same name pattern (SQL LIKE syntax)
     * 
     * @param typeQName             the node type
     * @param namePattern           the SQL LIKE pattern
     * @return                      Returns the node ID and node name
     */
    public List<Pair<NodeRef, String>> getNodesOfTypeWithNamePattern(QName typeQName, String namePattern);
    
    /**
     * @param qnames                the qnames to search for
     * @return                      Returns a count of the number of nodes that have either of the aspects
     */
    public long getCountNodesWithAspects(Set<QName> qnames);
    
    /**
     * Find all the nodes ids with the given type
     * @param typeQNameId - the id of the type qname
     * @param minNodeId - min node id in the result set - inclusive
     * @param maxNodeId - max node id in the result set - exclusive
     * @return          IDs of the nodes
     */
    public List<Long> getNodesByTypeQNameId(Long typeQNameId, Long minNodeId, Long maxNodeId);
    
    /**
     * Find all the nodes ids with the given type uri
     * @param uriId - the id of the type qname uri
     * @param minNodeId - min node id in the result set - inclusive
     * @param maxNodeId - max node id in the result set - exclusive
     * @return          IDs of the nodes
     */
    public List<Long> getNodesByTypeUriId(Long uriId, Long minNodeId, Long maxNodeId);
    
    /**
     * Find all the nodes ids with the given aspect
     * @param aspectQNameId - the id of the aspect qname
     * @param minNodeId - min node id in the result set - inclusive
     * @param maxNodeId - max node id in the result set - exclusive
     * @return          IDs of the nodes
     */
    public List<Long> getNodesByAspectQNameId(Long aspectQNameId, Long minNodeId, Long maxNodeId);
    
    /**
     * Find all the nodes ids with the given content property set with the given mimetype
     * @param mimetypeId - the id of the content data mimetype
     * @param minNodeId - min node id in the result set - inclusive
     * @param maxNodeId - max node id in the result set - exclusive
     * @return          IDs of the nodes
     */
    public List<Long> getNodesByContentPropertyMimetypeId(Long mimetypeId, Long minNodeId, Long maxNodeId);

    /**
     * Find all the nodes ids with the given aspect and type
     * @param typeQNameId - the id of the type qname
     * @param aspectQNameId - the id of the aspect qname
     * @param minNodeId - min node id in the result set - inclusive
     * @param maxNodeId - max node id in the result set - exclusive
     * @return List
     */
    public List<Long> getNodesByTypeQNameAndAspectQNameId(long typeQNameId, long aspectQNameId, long minNodeId, long maxNodeId);
    
    /**
     * Gets the total number of nodes which match the given Type QName.
     * 
     * @param typeQName the qname to search for
     * @return count of nodes that match the typeQName
     */
    public long getCountNodesWithTypId(QName typeQName);
    
    /**
     * Finds folders of the shared surf-config (for all tenants):
     * <ul>
     * <li> company_home/sites/surf-config/components </li>
     * <li>company_home/sites/surf-config/pages </li>
     * <li>company_home/sites/surf-config/pages/user </li>
     * <li>company_home/sites/surf-config/pages/user{userId} </li>
     * </ul>
     * @param minNodeId - min node id in the result set - inclusive
     * @param maxNodeId - max node id in the result set - exclusive
     * @return list of children nodeRefs
     */
    public List<NodeRef> getChildrenOfTheSharedSurfConfigFolder(Long minNodeId, Long maxNodeId);

}
