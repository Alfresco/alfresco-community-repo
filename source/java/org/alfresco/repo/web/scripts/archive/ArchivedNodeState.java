package org.alfresco.repo.web.scripts.archive;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PathUtil;

/**
 * A simple POJO class for the state of an archived node. For easier passing to the FTL model.
 * 
 * @author Neil McErlean
 * @since 3.5
 */
public class ArchivedNodeState
{
    private NodeRef archivedNodeRef;
    private String archivedBy;
    private Date archivedDate;
    private String name;
    private String title;
    private String description;
    private String displayPath;
    private String firstName;
    private String lastName;
    private String nodeType;
    private boolean isContentType;
    
    /**
     * To prevent unauthorised construction.
     */
    private ArchivedNodeState() { /* Intentionally empty*/ }
    
    public static ArchivedNodeState create(NodeRef archivedNode, ServiceRegistry serviceRegistry)
    {
        ArchivedNodeState result = new ArchivedNodeState();
        
        NodeService nodeService = serviceRegistry.getNodeService();
        Map<QName, Serializable> properties = nodeService.getProperties(archivedNode);

        result.archivedNodeRef = archivedNode;
        result.archivedBy = (String) properties.get(ContentModel.PROP_ARCHIVED_BY);
        result.archivedDate = (Date) properties.get(ContentModel.PROP_ARCHIVED_DATE);
        result.name = (String) properties.get(ContentModel.PROP_NAME);
        result.title = (String) properties.get(ContentModel.PROP_TITLE);
        result.description = (String) properties.get(ContentModel.PROP_DESCRIPTION);
        QName type = nodeService.getType(archivedNode);
        result.isContentType = (type.equals(ContentModel.TYPE_CONTENT) || serviceRegistry.getDictionaryService().isSubClass(type, ContentModel.TYPE_CONTENT));
        result.nodeType = type.toPrefixString(serviceRegistry.getNamespaceService());

        PersonService personService = serviceRegistry.getPersonService();
        if (result.archivedBy != null && personService.personExists(result.archivedBy))
        {
            NodeRef personNodeRef = personService.getPerson(result.archivedBy, false);
            Map<QName, Serializable> personProps = nodeService.getProperties(personNodeRef);
            
            result.firstName = (String) personProps.get(ContentModel.PROP_FIRSTNAME);
            result.lastName = (String) personProps.get(ContentModel.PROP_LASTNAME);
        }
        
        ChildAssociationRef originalParentAssoc = (ChildAssociationRef) properties.get(ContentModel.PROP_ARCHIVED_ORIGINAL_PARENT_ASSOC);
        
        if (serviceRegistry.getPermissionService().hasPermission(originalParentAssoc.getParentRef(), PermissionService.READ).equals(AccessStatus.ALLOWED)
                && nodeService.exists(originalParentAssoc.getParentRef()))
        {
           result.displayPath = PathUtil.getDisplayPath(nodeService.getPath(originalParentAssoc.getParentRef()), true);
        }
        else
        {
           result.displayPath = "";
        }
        
        return result;
    }
    
    public NodeRef getNodeRef()
    {
        return this.archivedNodeRef;
    }
    
    public String getArchivedBy()
    {
        return this.archivedBy;
    }
    
    public Date getArchivedDate()
    {
        return this.archivedDate;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public String getTitle()
    {
        return this.title;
    }
    
    public String getDescription()
    {
        return this.description;
    }
    
    public String getDisplayPath()
    {
        return this.displayPath;
    }
    
    public String getFirstName()
    {
        return this.firstName;
    }
    
    public String getLastName()
    {
        return this.lastName;
    }
    
    public String getNodeType()
    {
        return this.nodeType;
    }
    
    public boolean getIsContentType()
    {
        return this.isContentType;
    }
}
