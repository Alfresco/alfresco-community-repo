/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.domain.patch;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.domain.avm.AVMNodeEntity;
import org.alfresco.repo.domain.contentdata.ContentDataDAO;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;

import com.ibatis.sqlmap.client.event.RowHandler;

/**
 * Additional DAO services for patches
 *
 * @author janv
 * @author Derek Hulley
 * @since 3.2
 */
public interface PatchDAO
{
    // AVM-related
    
    public long getAVMNodesCountWhereNewInStore();
    
    public List<AVMNodeEntity> getEmptyGUIDS(int count);
    
    public List<AVMNodeEntity> getNullVersionLayeredDirectories(int count);
    
    public List<AVMNodeEntity> getNullVersionLayeredFiles(int count);
    
    public long getMaxAvmNodeID();
    
    public List<Long> getAvmNodesWithOldContentProperties(Long minNodeId, Long maxNodeId);
    
    public int updateAVMNodesNullifyAcl(List<Long> nodeIds);
    
    public int updateAVMNodesSetAcl(long aclId, List<Long> nodeIds);
    
    // DM-related
    
    public long getMaxAdmNodeID();
    
    /**
     * Migrates DM content properties from the old V3.1 format (String-based {@link ContentData#toString()})
     * to the new V3.2 format (ID based storage using {@link ContentDataDAO}).
     * 
     * @param minNodeId         the inclusive node ID to limit the updates to
     * @param maxNodeId         the exclusive node ID to limit the updates to
     */
    public void updateAdmV31ContentProperties(Long minNodeId, Long maxNodeId);
    
    /**
     * Update all <b>alf_content_data</b> mimetype references.
     * 
     * @param oldMimetypeId     the ID to search for
     * @param newMimetypeId     the ID to change to
     * @return                  the number of rows affected
     */
    public int updateContentMimetypeIds(Long oldMimetypeId, Long newMimetypeId);
    
    /**
     * A callback handler for iterating over the string results
     */
    public interface StringHandler
    {
        void handle(String string);
    }
    
    /**
     * Add a <b>cm:sizeCurrent</b> property to person nodes that don't have it.
     */
    public int addSizeCurrentProp();
    
    // ACL-related
    
    /**
     * Get the max acl id
     * 
     * @return - max acl id
     */
    public long getMaxAclId();
    
    /**
     * How many DM nodes are there?
     * 
     * @return - the count
     */
    public long getDmNodeCount();
    
    /**
     * How many DM nodes are three with new ACls (to track patch progress)
     * 
     * @param above
     * @return - the count
     */
    public long getDmNodeCountWithNewACLs(Long above);
    
    public List<Long> selectAllAclIds();
    
    public List<Long> selectNonDanglingAclIds();
    
    public int deleteDanglingAces();
    
    public int deleteAcls(List<Long> aclIds);
    
    public int deleteAclMembersForAcls(List<Long> aclIds);
    
    /**
     * @return      Returns the names of authorities with incorrect CRC values
     */
    public List<String> getAuthoritiesWithNonUtf8Crcs();
    
    /**
     * @return                      Returns the number child association rows
     */
    public int getChildAssocCount();
    
    /**
     * 
     * @return                      Returns the maximum child assoc ID or <tt>0</tt> if there are none
     */
    Long getMaxChildAssocId();
    
    /**
     * The results map contains:
     * <pre>
     * <![CDATA[
        <result property="id" column="id" jdbcType="BIGINT" javaType="java.lang.Long"/>
        <result property="typeQNameId" column="type_qname_id" jdbcType="BIGINT" javaType="java.lang.Long"/>
        <result property="qnameNamespaceId" column="qname_ns_id" jdbcType="BIGINT" javaType="java.lang.Long"/>
        <result property="qnameLocalName" column="qname_localname" jdbcType="VARCHAR" javaType="java.lang.String"/>
        <result property="childNodeNameCrc" column="child_node_name_crc" jdbcType="BIGINT" javaType="java.lang.Long"/>
        <result property="qnameCrc" column="qname_crc" jdbcType="BIGINT" javaType="java.lang.Long"/>
        <result property="childNodeUuid" column="child_node_uuid" jdbcType="VARCHAR" javaType="java.lang.String"/>
        <result property="childNodeName" column="child_node_name" jdbcType="VARCHAR" javaType="java.lang.String"/>
       ]]>
     * </pre>
     * @param minAssocId            the minimum child assoc ID
     * @param stopAtAssocId         the child assoc ID to stop at i.e. once this ID has been reached,
     *                              pull back no results
     * @param rangeMultiplier       the ration of IDs to actual rows (how many IDs to select to get a row)
     * @param maxIdRange            the largest ID range to use for selects.  Normally, the ID range should be
     *                              allowed to grow in accordance with the general distribution of rows, but
     *                              if memory problems are encountered, then the range will need to be set down.
     * @param maxResults            the number of child associations to fetch
     * @return                      Returns child associations <b>that need fixing</b>
     */
    public List<Map<String, Object>> getChildAssocsForCrcFix(
            Long minAssocId,
            Long stopAtAssocId,
            long rangeMultiplier,
            long maxIdRange,
            int maxResults);
    
    public int updateChildAssocCrc(Long assocId, Long childNodeNameCrc, Long qnameCrc);
    
    /**
     * Query for a list of nodes that have a given type and share the same name pattern (SQL LIKE syntax)
     * 
     * @param typeQName             the node type
     * @param namePattern           the SQL LIKE pattern
     * @return                      Returns the node ID and node name
     */
    public List<Pair<NodeRef, String>> getNodesOfTypeWithNamePattern(QName typeQName, String namePattern);
    
    /**
     * Migrate old Tenant attributes (if any)
     */
    public void migrateOldAttrTenants(RowHandler rowHandler);
    
    /**
     * Migrate old AVM Lock attributes (if any)
     */
    public void migrateOldAttrAVMLocks(RowHandler rowHandler);
    
    /**
     * Migrate old Property-Backed Bean attributes (if any)
     */
    public void migrateOldAttrPropertyBackedBeans(RowHandler rowHandler);
    
    /**
     * Migrate old Chaining User Registry Synchronizer attributes (if any)
     */
    public void migrateOldAttrChainingURS(RowHandler rowHandler);
    
    /**
     * Get custom global attribute names (if any)
     */
    public List<String> getOldAttrCustomNames();
    
    /**
     * Delete all old attributes (from alf_*attribute* tables)
     */
    public void deleteAllOldAttrs();
    
    /**
     * Get shared acls with inheritance issues
     * @return
     */
    public List<Map<String, Object>> getSharedAclsThatDoNotInheritCorrectlyFromThePrimaryParent();
    
    /**
     * Get defining acls with inheritance issues
     * @return
     */
    public List<Map<String, Object>> getDefiningAclsThatDoNotInheritCorrectlyFromThePrimaryParent();
    
    /**
     * Get acls that do not inherit from the primary parent.
     * @return
     */
    public List<Map<String, Object>> getAclsThatInheritFromNonPrimaryParent();
    
    /**
     * Get acls that inherit with inheritance unset
     * @return
     */
    public List<Map<String, Object>> getAclsThatInheritWithInheritanceUnset();
    
    /**
     * Get shared acls that do not inherit correctly from the defining acl
     * @return
     */
    public List<Map<String, Object>> getSharedAclsThatDoNotInheritCorrectlyFromTheirDefiningAcl();
    
    
}
