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
package org.alfresco.web.bean.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Lighweight client side representation of a node held in the repository. 
 * 
 * @author gavinc
 */
public class Node implements Serializable
{
   private static final long serialVersionUID = 3544390322739034169L;

   protected static Log logger = LogFactory.getLog(Node.class);
   
   protected NodeRef nodeRef;
   private String name;
   private QName type;
   private String path;
   private String id;
   private Set<QName> aspects = null;
   private Map<String, Boolean> permissions;
   protected QNameNodeMap<String, Object> properties;
   protected boolean propsRetrieved = false;
   protected ServiceRegistry services = null;
   
   private boolean childAssocsRetrieved = false;
   private QNameNodeMap childAssociations;
   private Map<String, Map<String, ChildAssociationRef>> childAssociationsAdded;
   private Map<String, Map<String, ChildAssociationRef>> childAssociationsRemoved;
   
   private boolean assocsRetrieved = false;
   private QNameNodeMap associations;
   private Map<String, Map<String, AssociationRef>> associationsAdded;
   private Map<String, Map<String, AssociationRef>> associationsRemoved;
   
   /**
    * Constructor
    * 
    * @param nodeRef The NodeRef this Node wrapper represents
    */
   public Node(NodeRef nodeRef)
   {
      if (nodeRef == null)
      {
         throw new IllegalArgumentException("NodeRef must be supplied for creation of a Node.");
      }
      
      this.nodeRef = nodeRef;
      this.id = nodeRef.getId();
      
      this.properties = new QNameNodeMap<String, Object>(getServiceRegistry().getNamespaceService(), this);
   }

   /**
    * @return All the properties known about this node.
    */
   public Map<String, Object> getProperties()
   {
      if (this.propsRetrieved == false)
      {
         Map<QName, Serializable> props = getServiceRegistry().getNodeService().getProperties(this.nodeRef);
         
         for (QName qname: props.keySet())
         {
            Serializable propValue = props.get(qname);
            this.properties.put(qname.toString(), propValue);
         }
         
         this.propsRetrieved = true;
      }
      
      return this.properties;
   }
   
   /**
    * @return All the associations this node has as a Map, using the association
    *         type as the key
    */
   public final Map getAssociations()
   {
      if (this.assocsRetrieved == false)
      {
         associations = new QNameNodeMap(getServiceRegistry().getNamespaceService(), this);
         
         List<AssociationRef> assocs = getServiceRegistry().getNodeService().getTargetAssocs(this.nodeRef, RegexQNamePattern.MATCH_ALL);
         
         for (AssociationRef assocRef: assocs)
         {
            String assocName = assocRef.getTypeQName().toString();
            
            List list = (List)this.associations.get(assocName);
            // create the list if this is first association with 'assocName'
            if (list == null)
            {
               list = new ArrayList<AssociationRef>();
               this.associations.put(assocName, list);
            }
            
            // add the association to the list
            list.add(assocRef);
         }
         
         this.assocsRetrieved = true;
      }
      
      return this.associations;
   }
   
   /**
    * Returns all the associations added to this node in this UI session
    * 
    * @return Map of Maps of AssociationRefs
    */
   public final Map<String, Map<String, AssociationRef>> getAddedAssociations()
   {
      if (this.associationsAdded == null)
      {
         this.associationsAdded = new HashMap<String, Map<String, AssociationRef>>();
      }
      return this.associationsAdded;
   }
   
   /**
    * Returns all the associations removed from this node is this UI session
    * 
    * @return Map of Maps of AssociationRefs
    */
   public final Map<String, Map<String, AssociationRef>> getRemovedAssociations()
   {
      if (this.associationsRemoved == null)
      {
         this.associationsRemoved = new HashMap<String, Map<String, AssociationRef>>();
      }
      return this.associationsRemoved;
   }
   
   /**
    * @return All the child associations this node has as a Map, using the association
    *         type as the key
    */
   public final Map getChildAssociations()
   {
      if (this.childAssocsRetrieved == false)
      {
         this.childAssociations = new QNameNodeMap(getServiceRegistry().getNamespaceService(), this);
         
         List<ChildAssociationRef> assocs = getServiceRegistry().getNodeService().getChildAssocs(this.nodeRef);
         
         for (ChildAssociationRef assocRef: assocs)
         {
            String assocName = assocRef.getTypeQName().toString();
            
            List list = (List)this.childAssociations.get(assocName);
            // create the list if this is first association with 'assocName'
            if (list == null)
            {
               list = new ArrayList<ChildAssociationRef>();
               this.childAssociations.put(assocName, list);
            }
            
            // add the association to the list
            list.add(assocRef);
         }
         
         this.childAssocsRetrieved = true;
      }
      
      return this.childAssociations;
   }
   
   /**
    * Returns all the child associations added to this node in this UI session
    * 
    * @return Map of Maps of ChildAssociationRefs
    */
   public final Map<String, Map<String, ChildAssociationRef>> getAddedChildAssociations()
   {
      if (this.childAssociationsAdded == null)
      {
         this.childAssociationsAdded = new HashMap<String, Map<String, ChildAssociationRef>>();
      }
      return this.childAssociationsAdded;
   }
   
   /**
    * Returns all the child associations removed from this node is this UI session
    * 
    * @return Map of Maps of ChildAssociationRefs
    */
   public final Map<String, Map<String, ChildAssociationRef>> getRemovedChildAssociations()
   {
      if (this.childAssociationsRemoved == null)
      {
         this.childAssociationsRemoved = new HashMap<String, Map<String, ChildAssociationRef>>();
      }
      return this.childAssociationsRemoved;
   }
   
   /**
    * Register a property resolver for the named property.
    * 
    * @param name       Name of the property this resolver is for
    * @param resolver   Property resolver to register
    */
   public final void addPropertyResolver(String name, NodePropertyResolver resolver)
   {
      this.properties.addPropertyResolver(name, resolver);
   }
   
   /**
    * Determines whether the given property name is held by this node 
    * 
    * @param propertyName Property to test existence of
    * @return true if property exists, false otherwise
    */
   public final boolean hasProperty(String propertyName)
   {
      return getProperties().containsKey(propertyName);
   }

   /**
    * @return Returns the NodeRef this Node object represents
    */
   public final NodeRef getNodeRef()
   {
      return this.nodeRef;
   }
   
   /**
    * @return Returns the type.
    */
   public final QName getType()
   {
      if (this.type == null)
      {
         this.type = getServiceRegistry().getNodeService().getType(this.nodeRef);
      }
      
      return type;
   }
   
   /**
    * @return The display name for the node
    */
   public final String getName()
   {
      if (this.name == null)
      {
         // try and get the name from the properties first
         this.name = (String)getProperties().get("cm:name");
         
         // if we didn't find it as a property get the name from the association name
         if (this.name == null)
         {
            this.name = getServiceRegistry().getNodeService().getPrimaryParent(this.nodeRef).getQName().getLocalName(); 
         }
      }
      
      return this.name;
   }

   /**
    * @return The list of aspects applied to this node
    */
   public final Set<QName> getAspects()
   {
      if (this.aspects == null)
      {
         this.aspects = getServiceRegistry().getNodeService().getAspects(this.nodeRef);
      }
      
      return this.aspects;
   }
   
   /**
    * @param aspect The aspect to test for
    * @return true if the node has the aspect false otherwise
    */
   public final boolean hasAspect(QName aspect)
   {
      Set aspects = getAspects();
      return aspects.contains(aspect);
   }
   
   /**
    * Return whether the current user has the specified access permission on this Node
    * 
    * @param permission     Permission to validate against
    * 
    * @return true if the permission is applied to the node for this user, false otherwise
    */
   public final boolean hasPermission(String permission)
   {
      Boolean valid = null;
      if (permissions != null)
      {
         valid = permissions.get(permission);
      }
      else
      {
         permissions = new HashMap<String, Boolean>(5, 1.0f);
      }
      
      if (valid == null)
      {
         PermissionService service = Repository.getServiceRegistry(FacesContext.getCurrentInstance()).getPermissionService();
         valid = Boolean.valueOf(service.hasPermission(this.nodeRef, permission) == AccessStatus.ALLOWED);
         permissions.put(permission, valid);
      }
      
      return valid.booleanValue();
   }

   /**
    * @return The GUID for the node
    */
   public final String getId()
   {
      return this.id;
   }

   /**
    * @return The path for the node
    */
   public final String getPath()
   {
      if (this.path == null)
      {
         this.path = getServiceRegistry().getNodeService().getPath(this.nodeRef).toString();
      }
      
      return this.path;
   }
   
   /**
    * Resets the state of the node to force re-retrieval of the data
    */
   public void reset()
   {
      this.name = null;
      this.type = null;
      this.path = null;
      this.properties.clear();
      this.propsRetrieved = false;
      this.aspects = null;
      this.permissions = null;
      
      this.associations = null;
      this.associationsAdded = null;
      this.associationsRemoved = null;
      this.assocsRetrieved = false;
      
      this.childAssociations = null;
      this.childAssociationsAdded = null;
      this.childAssociationsRemoved = null;
      this.childAssocsRetrieved = false;
   }
   
   /**
    * Override Object.toString() to provide useful debug output
    */
   public String toString()
   {
      if (getServiceRegistry().getNodeService() != null)
      {
         if (getServiceRegistry().getNodeService().exists(nodeRef))
         {
            return "Node Type: " + getType() + 
                   "\nNode Properties: " + this.getProperties().toString() + 
                   "\nNode Aspects: " + this.getAspects().toString();
         }
         else
         {
            return "Node no longer exists: " + nodeRef;
         }
      }
      else
      {
         return super.toString();
      }
   }
   
   protected ServiceRegistry getServiceRegistry()
   {
      if (this.services == null)
      {
          this.services = Repository.getServiceRegistry(FacesContext.getCurrentInstance());
      }
      return this.services;
   }
}
