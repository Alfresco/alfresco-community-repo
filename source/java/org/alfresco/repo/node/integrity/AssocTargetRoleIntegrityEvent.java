package org.alfresco.repo.node.integrity;

import java.util.List;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Event to check the association target role name
 * 
 * @author Derek Hulley
 */
public class AssocTargetRoleIntegrityEvent extends AbstractIntegrityEvent
{
    public AssocTargetRoleIntegrityEvent(
            NodeService nodeService,
            DictionaryService dictionaryService,
            NodeRef sourceNodeRef,
            QName assocTypeQName,
            QName assocName)
    {
        super(nodeService, dictionaryService, sourceNodeRef, assocTypeQName, assocName);
    }
    
    public void checkIntegrity(List<IntegrityRecord> eventResults)
    {
        QName assocTypeQName = getTypeQName();
        QName assocQName = getQName();
        NodeRef sourceNodeRef = getNodeRef();
        
        // get the association def
        AssociationDefinition assocDef = getAssocDef(eventResults, assocTypeQName);
        // the association definition must exist
        if (assocDef == null)
        {
            IntegrityRecord result = new IntegrityRecord(
                    "Association type does not exist: \n" +
                    "   Association Type: " + assocTypeQName);
            eventResults.add(result);
            return;
        }
        
        // check that we are dealing with child associations
        if (assocQName == null)
        {
            throw new IllegalArgumentException("The association qualified name must be supplied");
        }
        if (!assocDef.isChild())
        {
            throw new UnsupportedOperationException("This operation is only relevant to child associations");
        }
        ChildAssociationDefinition childAssocDef = (ChildAssociationDefinition) assocDef;
        
        // perform required checks
        checkAssocQNameRegex(eventResults, childAssocDef, assocQName, sourceNodeRef);
    }

    /**
     * Checks that the association name matches the constraints imposed by the model.
     */
    protected void checkAssocQNameRegex(
            List<IntegrityRecord> eventResults,
            ChildAssociationDefinition assocDef,
            QName assocQName,
            NodeRef sourceNodeRef)
    {
        // check the association name
        QName assocRoleQName = assocDef.getTargetRoleName();
        if (assocRoleQName != null)
        {
            // the assoc defines a role name - check it
            RegexQNamePattern rolePattern = new RegexQNamePattern(assocRoleQName.getNamespaceURI(), assocRoleQName.getLocalName());
            if (!rolePattern.isMatch(assocQName))
            {
                IntegrityRecord result = new IntegrityRecord(
                        "The association name does not match the allowed role names: \n" +
                        "   Source Node: " + sourceNodeRef + "\n" +
                        "   Association: " + assocDef + "\n" +
                        "   Allowed roles: " + rolePattern + "\n" +
                        "   Name assigned: " + assocRoleQName);
                eventResults.add(result);
            }
        }
    }
}
