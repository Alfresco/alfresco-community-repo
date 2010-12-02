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
package org.alfresco.repo.domain.node;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.CRC32;

import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean for <b>alf_child_assoc</b> table.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public class ChildAssocEntity
{
    private static final Log logger = LogFactory.getLog(ChildAssocEntity.class);
    
    private Long id;
    private Long version;
    private NodeEntity parentNode;
    private NodeEntity childNode;
    private Long typeQNameId;
    private Long childNodeNameCrc;
    private String childNodeName;
    private Long qnameNamespaceId;
    private String qnameLocalName;
    private Long qnameCrc;
    private Boolean isPrimary;
    private int assocIndex;
    
    // Supplemental query-related parameters
    private List<Long> typeQNameIds;
    private List<Long> childNodeNameCrcs;
    private List<Long> childNodeTypeQNameIds;
    private Boolean sameStore;
    private boolean ordered;
    
    /**
     * Find a CRC value for the full QName using UTF-8 conversion.
     * 
     * @param qname                 the association qname
     * @return                      Returns the CRC value (UTF-8 compatible)
     */
    public static Long getQNameCrc(QName qname)
    {
        CRC32 crc = new CRC32();
        try
        {
            crc.update(qname.getNamespaceURI().getBytes("UTF-8"));
            crc.update(qname.getLocalName().getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding is not supported");
        }
        return crc.getValue();
        
    }

    /**
     * Find a CRC value for the association's child node name using UTF-8 conversion.
     * 
     * @param childNodeName         the child node name
     * @return                      Returns the CRC value (UTF-8 compatible)
     */
    public static Long getChildNodeNameCrc(String childNodeName)
    {
        CRC32 crc = new CRC32();
        try
        {
            // https://issues.alfresco.com/jira/browse/ALFCOM-1335
            crc.update(childNodeName.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF-8 encoding is not supported");
        }
        return crc.getValue();
    }

    private static final String TRUNCATED_NAME_INDICATOR = "~~~";

    /**
     * Truncates the association's child node name to 50 characters.
     * 
     * @param childNodeName         the child node name
     * @return                      Returns the potentially truncated value
     */
    public static String getChildNodeNameShort(String childNodeName)
    {
        int length = childNodeName.length();
        if (length <= 50)
        {
            return childNodeName;
        }
        else
        {
            StringBuilder ret = new StringBuilder(50);
            ret.append(childNodeName.substring(0, 47)).append(TRUNCATED_NAME_INDICATOR);
            return ret.toString();
        }
    }

    /**
     * Apply the <b>cm:name</b> to the child association. If the child name is <tt>null</tt> then a GUID is generated as
     * a substitute.
     * <p>
     * Unknown associations or associations that do not require unique name checking will use a GUID for the child
     * name and the CRC value used <b>will be negative</b>.
     * 
     * @param childName the <b>cm:name</b> applying to the association.
     */
    public static Pair<String, Long> getChildNameUnique(
            DictionaryService dictionaryService,
            QName assocTypeQName,
            String childName)
    {
        if (childName == null)
        {
            throw new IllegalArgumentException("Child name may not be null.  Use the Node ID ...");
        }
        
        String childNameNewShort; // 
        long childNameNewCrc = -1L; // By default, they don't compete

        AssociationDefinition assocDef = dictionaryService.getAssociation(assocTypeQName);
        if (assocDef == null || !assocDef.isChild())
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("No child association of this type could be found: " + assocTypeQName);
            }
            childNameNewShort = GUID.generate();
            childNameNewCrc = -1L * getChildNodeNameCrc(childNameNewShort);
        }
        else
        {
            ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
            if (childAssocDef.getDuplicateChildNamesAllowed())
            {
                childNameNewShort = GUID.generate();
                childNameNewCrc = -1L * getChildNodeNameCrc(childNameNewShort);
            }
            else
            {
                String childNameNewLower = childName.toLowerCase();
                childNameNewShort = getChildNodeNameShort(childNameNewLower);
                childNameNewCrc = getChildNodeNameCrc(childNameNewLower);
            }
        }
        return new Pair<String, Long>(childNameNewShort, childNameNewCrc);
    }

    /**
     * Required default constructor
     */
    public ChildAssocEntity()
    {
        ordered = true;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(512);
        sb.append("ChildAssocEntity")
          .append("[ ID=").append(id)
          .append(", parentNode=").append(parentNode)
          .append(", childNode=").append(childNode)
          .append(", typeQNameId=").append(typeQNameId)
          .append(", childNodeNameCrc=").append(childNodeNameCrc)
          .append(", childNodeName=").append(childNodeName)
          .append(", qnameNamespaceId=").append(qnameNamespaceId)
          .append(", qnameLocalName=").append(qnameLocalName)
          .append(", qnameCrc=").append(qnameCrc)
          .append("]");
        return sb.toString();
    }
    
    public ChildAssociationRef getRef(QNameDAO qnameDAO)
    {
        QName typeQName = qnameDAO.getQName(typeQNameId).getSecond();
        QName qname = QName.createQName(qnameDAO.getNamespace(qnameNamespaceId).getSecond(), qnameLocalName);
        return new ChildAssociationRef(
                typeQName,
                parentNode.getNodeRef(),
                qname,
                childNode.getNodeRef(),
                isPrimary,
                assocIndex);
    }
    
    public Pair<Long, ChildAssociationRef> getPair(QNameDAO qnameDAO)
    {
        return new Pair<Long, ChildAssociationRef>(id, getRef(qnameDAO));
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public NodeEntity getParentNode()
    {
        return parentNode;
    }

    public void setParentNode(NodeEntity parentNode)
    {
        this.parentNode = parentNode;
    }

    public NodeEntity getChildNode()
    {
        return childNode;
    }

    public void setChildNode(NodeEntity childNode)
    {
        this.childNode = childNode;
    }
    
    /**
     * Helper method to set the {@link #setTypeQNameId(Long)}.
     * 
     * @param qnameDAO                  the DAO to resolve the QName ID
     * @param typeQName                 the association type
     * @param forUpdate                 <tt>true</tt> if the QName must exist i.e. this
     *                                  entity will be used for updates and the type
     *                                  <code>QName</code> <b>must</b> exist.
     * @return                          <tt>true</tt> if the set worked otherwise <tt>false</tt>
     */
    public boolean setTypeQNameAll(QNameDAO qnameDAO, QName typeQName, boolean forUpdate)
    {
        if (forUpdate)
        {
            typeQNameId = qnameDAO.getOrCreateQName(typeQName).getFirst();
            return true;
        }
        else
        {
            Pair<Long, QName> qnamePair = qnameDAO.getQName(typeQName);
            if (qnamePair == null)
            {
                return false;
            }
            else
            {
                typeQNameId = qnamePair.getFirst();
                return true;
            }
        }
    }

    public Long getTypeQNameId()
    {
        return typeQNameId;
    }
    
    /**
     * @deprecated                      For persistence use only
     */
    public void setTypeQNameId(Long typeQNameId)
    {
        this.typeQNameId = typeQNameId;
    }

    /**
     * Helper method to set all values associated with the
     * {@link #setChildNodeName(String) child node name}.
     * 
     * @param dictionaryService         the service that determines how the CRC values are generated.
     *                                  If this is <tt>null</tt> then the CRC values are generated
     *                                  assuming that positive enforcement of the name constraint is
     *                                  required.
     * @param childNodeName             the child node name
     */
    public void setChildNodeNameAll(
            DictionaryService dictionaryService,
            QName typeQName,
            String childNodeName)
    {
        ParameterCheck.mandatory("childNodeName", childNodeName);
        
        if (dictionaryService != null)
        {
            ParameterCheck.mandatory("typeQName", typeQName);
            
            Pair<String, Long> childNameUnique = ChildAssocEntity.getChildNameUnique(
                    dictionaryService,
                    typeQName,
                    childNodeName);
            this.childNodeName = childNameUnique.getFirst();
            this.childNodeNameCrc = childNameUnique.getSecond();
        }
        else
        {
            String childNameNewLower = childNodeName.toLowerCase();
            this.childNodeName = ChildAssocEntity.getChildNodeNameShort(childNameNewLower);
            this.childNodeNameCrc = ChildAssocEntity.getChildNodeNameCrc(childNameNewLower);
        }
    }

    public Long getChildNodeNameCrc()
    {
        return childNodeNameCrc;
    }

    /**
     * @deprecated                      For persistence use
     */
    public void setChildNodeNameCrc(Long childNodeNameCrc)
    {
        this.childNodeNameCrc = childNodeNameCrc;
    }

    public String getChildNodeName()
    {
        return childNodeName;
    }

    /**
     * @deprecated                      For persistence use
     */
    public void setChildNodeName(String childNodeName)
    {
        this.childNodeName = childNodeName;
    }

    /**
     * Set all required fields associated with the patch <code>QName</code>.
     * 
     * @param forUpdate                 <tt>true</tt> if the entity is going to be used for a
     *                                  data update i.e. the <code>QName</code> <b>must</b> exist.
     * @return                          Returns <tt>true</tt> if the <code>QName</code> namespace
     *                                  exists.
     */
    public boolean setQNameAll(QNameDAO qnameDAO, QName qname, boolean forUpdate)
    {
        String assocQNameNamespace = qname.getNamespaceURI();
        String assocQNameLocalName = qname.getLocalName();
        Long assocQNameNamespaceId = null;
        if (forUpdate)
        {
            assocQNameNamespaceId = qnameDAO.getOrCreateNamespace(assocQNameNamespace).getFirst();
        }
        else
        {
            Pair<Long, String> nsPair = qnameDAO.getOrCreateNamespace(assocQNameNamespace);
            if (nsPair == null)
            {
                // We can't set anything
                return false;
            }
            else
            {
                assocQNameNamespaceId = nsPair.getFirst();
            }
        }
        Long assocQNameCrc = getQNameCrc(qname);

        this.qnameNamespaceId = assocQNameNamespaceId;
        this.qnameLocalName = assocQNameLocalName;
        this.qnameCrc = assocQNameCrc;
        
        // All set correctly
        return true;
    }
    
    public Long getQnameNamespaceId()
    {
        return qnameNamespaceId;
    }

    /**
     * @deprecated                      For persistence use
     */
    public void setQnameNamespaceId(Long qnameNamespaceId)
    {
        this.qnameNamespaceId = qnameNamespaceId;
    }

    public String getQnameLocalName()
    {
        return qnameLocalName;
    }

    /**
     * @deprecated                      For persistence use
     */
    public void setQnameLocalName(String qnameLocalName)
    {
        this.qnameLocalName = qnameLocalName;
    }

    public Long getQnameCrc()
    {
        return qnameCrc;
    }

    /**
     * @deprecated                      For persistence use
     */
    public void setQnameCrc(Long qnameCrc)
    {
        this.qnameCrc = qnameCrc;
    }

    public Boolean isPrimary()
    {
        return isPrimary;
    }

    public void setPrimary(Boolean isPrimary)
    {
        this.isPrimary = isPrimary;
    }

    public int getAssocIndex()
    {
        return assocIndex;
    }

    public void setAssocIndex(int assocIndex)
    {
        this.assocIndex = assocIndex;
    }

    public List<Long> getTypeQNameIds()
    {
        return typeQNameIds;
    }

    public void setTypeQNameIds(List<Long> typeQNameIds)
    {
        this.typeQNameIds = typeQNameIds;
    }

    public List<Long> getChildNodeNameCrcs()
    {
        return childNodeNameCrcs;
    }

    public void setChildNodeNameCrcs(List<Long> childNodeNameCrcs)
    {
        this.childNodeNameCrcs = childNodeNameCrcs;
    }

    public List<Long> getChildNodeTypeQNameIds()
    {
        return childNodeTypeQNameIds;
    }

    public void setChildNodeTypeQNameIds(List<Long> childNodeTypeQNameIds)
    {
        this.childNodeTypeQNameIds = childNodeTypeQNameIds;
    }

    public Boolean getSameStore()
    {
        return sameStore;
    }

    public void setSameStore(Boolean sameStore)
    {
        this.sameStore = sameStore;
    }

    public boolean isOrdered()
    {
        return ordered;
    }

    public void setOrdered(boolean ordered)
    {
        this.ordered = ordered;
    }
}
