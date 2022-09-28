package org.alfresco.cmis.dsl;

import static org.alfresco.utility.report.log.Step.STEP;

import java.util.Iterator;
import java.util.List;

import org.alfresco.cmis.CmisWrapper;
import org.alfresco.utility.LogFactory;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.objecttype.ObjectTypeHelper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.testng.Assert;

/**
 * DSL for preparing calls on getting the type children of a type.
 */
public class BaseObjectType
{
    private CmisWrapper cmisAPI;
    private String baseTypeID;
    private boolean includePropertyDefinition = false;
    private Logger LOG = LogFactory.getLogger();

    public BaseObjectType(CmisWrapper cmisAPI, String baseTypeID)
    {
        this.cmisAPI = cmisAPI;
        this.baseTypeID = baseTypeID;
    }

    public BaseObjectType withPropertyDefinitions()
    {
        this.includePropertyDefinition = true;
        return this;
    }

    public BaseObjectType withoutPropertyDefinitions()
    {
        this.includePropertyDefinition = false;
        return this;
    }

    /**
     * Example of objectTypeID:
     * "D:trx:transferReport" - see {@link ObjectTypeHelper} "D:trx:tempTransferStore"
     * "D:imap:imapAttach"
     *
     * @param objectTypeID
     */
    public PropertyDefinitionObject hasChildren(String objectTypeID)
    {
        return checkChildren(objectTypeID, true);
    }

    /**
     * Example of objectTypeID:
     * "D:trx:transferReport" - see {@link ObjectTypeHelper} "D:trx:tempTransferStore"
     * "D:imap:imapAttach"
     *
     * @param objectTypeID
     */
    public CmisWrapper doesNotHaveChildren(String objectTypeID)
    {
        checkChildren(objectTypeID, false);
        return cmisAPI;
    }

    /**
     * Example of objectTypeID:
     * "D:trx:transferReport" - see {@link ObjectTypeHelper} "D:trx:tempTransferStore"
     * "D:imap:imapAttach"
     *
     * @param objectTypeID
     */
    private PropertyDefinitionObject checkChildren(String objectTypeID, boolean exist)
    {
        ItemIterable<ObjectType> values = cmisAPI.withCMISUtil().getTypeChildren(this.baseTypeID, includePropertyDefinition);
        boolean foundChild = false;
        PropertyDefinitionObject propDefinition = null;
        for (Iterator<ObjectType> iterator = values.iterator(); iterator.hasNext();)
        {
            ObjectType type = (ObjectType) iterator.next();
            LOG.info("Found child Object Type: {}", ToStringBuilder.reflectionToString(type, ToStringStyle.MULTI_LINE_STYLE));
            if (type.getId().equals(objectTypeID))
            {
                foundChild = true;
                propDefinition = new PropertyDefinitionObject(type);
                break;
            }
        }
        Assert.assertEquals(foundChild, exist,
                String.format("Object Type with ID[%s] is found as children for Parent Type: [%s]", objectTypeID, this.baseTypeID));
        return propDefinition;
    }

    public class PropertyDefinitionObject
    {
        ObjectType type;

        public PropertyDefinitionObject(ObjectType type)
        {
            this.type = type;
        }

        public PropertyDefinitionObject propertyDefinitionIsEmpty()
        {
            STEP(String.format("%s Verify that property definitions map is empty.", CmisWrapper.STEP_PREFIX));
            Assert.assertTrue(type.getPropertyDefinitions().isEmpty(), "Property definitions is empty.");
            return this;
        }

        public PropertyDefinitionObject propertyDefinitionIsNotEmpty()
        {
            STEP(String.format("%s Verify that property definitions map is not empty.", CmisWrapper.STEP_PREFIX));
            Assert.assertFalse(type.getPropertyDefinitions().isEmpty(), "Property definitions is not empty.");
            return this;
        }
    }

    private CmisWrapper checkDescendents(int depth, boolean exist, String... objectTypeIDs)
    {
        List<Tree<ObjectType>> values = cmisAPI.withCMISUtil().getTypeDescendants(this.baseTypeID, depth, includePropertyDefinition);
        for (String objectTypeID : objectTypeIDs)
        {
            boolean foundChild = false;
            for (Tree<ObjectType> tree : values)
            {
                if (tree.getItem().getId().equals(objectTypeID))
                {
                    foundChild = true;
                    break;
                }
            }
            Assert.assertEquals(foundChild, exist,
                    String.format("Assert %b: Descendant [%s] is found as descendant for Type: [%s]", exist, objectTypeID, this.baseTypeID));

            if (foundChild)
            {
                STEP(String.format("%s Cmis object '%s' is found as descendant.", CmisWrapper.STEP_PREFIX, objectTypeID));
            }
            else
            {
                STEP(String.format("%s Cmis object '%s' is NOT found as descendant.", CmisWrapper.STEP_PREFIX, objectTypeID));
            }
        }
        return cmisAPI;
    }

    /**
     * Assert that specified descendantType is present in the depth of tree
     * Depth can be -1 or >= 1
     * Example of objectTypeID:
     * "D:trx:transferReport" - see {@link ObjectTypeHelper} "D:trx:tempTransferStore"
     * "D:imap:imapAttach"
     * 
     * @param objectTypeID
     * @param depth
     * @return
     */
    public CmisWrapper hasDescendantType(int depth, String... objectTypeIDs)
    {
        return checkDescendents(depth, true, objectTypeIDs);
    }

    /**
     * Assert that specified descendantType is NOT present in the depth of tree
     * Depth can be -1 or >= 1
     * Example of objectTypeID:
     * "D:trx:transferReport" - see {@link ObjectTypeHelper} "D:trx:tempTransferStore"
     * "D:imap:imapAttach"
     * 
     * @param objectTypeID
     * @param depth
     * @return
     */
    public CmisWrapper doesNotHaveDescendantType(int depth, String... objectTypeIDs)
    {
        checkDescendents(depth, false, objectTypeIDs);
        return cmisAPI;
    }
}
