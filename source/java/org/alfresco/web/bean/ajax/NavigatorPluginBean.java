package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.context.IContextListener;
import org.alfresco.web.app.context.UIContextService;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.repo.component.UITree.TreeNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Bean used by the navigator component to manage the tree data.
 * 
 * @author gavinc
 */
public class NavigatorPluginBean implements IContextListener
{
   public static final String BEAN_NAME = "NavigatorPluginBean";
   
   protected List<TreeNode> companyHomeRootNodes;
   protected List<TreeNode> myHomeRootNodes;
   protected List<TreeNode> guestHomeRootNodes;
   protected Map<String, TreeNode> companyHomeNodes;
   protected Map<String, TreeNode> myHomeNodes;
   protected Map<String, TreeNode> guestHomeNodes;
   protected NodeRef previouslySelectedNode;
   
   private NodeService nodeService;
   private DictionaryService dictionaryService;
   
   private static final Log logger = LogFactory.getLog(NavigatorPluginBean.class);
   
   public NavigatorPluginBean()
   {
      UIContextService.getInstance(FacesContext.getCurrentInstance()).registerBean(this);
   }
   
   // ------------------------------------------------------------------------------
   // AJAX handler methods
   
   /**
    * Retrieves the child folders for the noderef given in the 
    * 'noderef' parameter and caches the nodes against the area in
    * the 'area' parameter.
    */
   public void retrieveChildren() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      
      Map params = context.getExternalContext().getRequestParameterMap();
      String nodeRefStr = (String)params.get("nodeRef");
      String area = (String)params.get("area");
      
      if (logger.isDebugEnabled())
         logger.debug("retrieveChildren: area = " + area + ", nodeRef = " + nodeRefStr);
      
      // work out which list to cache the nodes in
      Map<String, TreeNode> currentNodes = getNodesMapForArea(area);
      
      if (nodeRefStr != null && currentNodes != null)
      {
         // get the given node's details
         NodeRef parentNodeRef = new NodeRef(nodeRefStr);
         TreeNode parentNode = currentNodes.get(parentNodeRef.toString());
         parentNode.setExpanded(true);
         
         if (logger.isDebugEnabled())
            logger.debug("retrieving children for noderef: " + parentNodeRef);
         
         // remove any existing children as the latest ones will be added below
         parentNode.removeChildren();
         
         // get all the child folder objects for the parent
         List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(parentNodeRef, 
               ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
         
         StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><nodes>");
         for (ChildAssociationRef ref: childRefs)
         {
            NodeRef nodeRef = ref.getChildRef();
            
            if (isAddableChild(nodeRef))
            {
               // build the XML representation of the child node
               TreeNode childNode = createTreeNode(nodeRef);
               parentNode.addChild(childNode);
               currentNodes.put(childNode.getNodeRef(), childNode);
               xml.append(childNode.toXML());
            }
         }
         xml.append("</nodes>");
         
         // send the generated XML back to the tree
         out.write(xml.toString());
         
         if (logger.isDebugEnabled())
            logger.debug("returning XML: " + xml.toString());
      }
   }
   
   /**
    * Sets the state of the node given in the 'nodeRef' parameter to collapsed
    */
   public void nodeCollapsed() throws IOException
   {
      FacesContext context = FacesContext.getCurrentInstance();
      ResponseWriter out = context.getResponseWriter();
      Map params = context.getExternalContext().getRequestParameterMap();
      String nodeRefStr = (String)params.get("nodeRef");
      String area = (String)params.get("area");
      
      if (logger.isDebugEnabled())
         logger.debug("nodeCollapsed: area = " + area + ", nodeRef = " + nodeRefStr);
      
      // work out which list to cache the nodes in
      Map<String, TreeNode> currentNodes = getNodesMapForArea(area);
      
      if (nodeRefStr != null && currentNodes != null)
      {
         TreeNode treeNode = currentNodes.get(nodeRefStr);
         if (treeNode != null)
         {
            treeNode.setExpanded(false);
            
            // we need to return something for the client to be happy!
            out.write("<ok/>");
            
            if (logger.isDebugEnabled())
               logger.debug("Set node " + treeNode + " to collapsed state");
         }
      }
   }
   
   // ------------------------------------------------------------------------------
   // IContextListener implementation

   /**
    * @see org.alfresco.web.app.context.IContextListener#contextUpdated()
    */
   public void contextUpdated()
   {
      // nothing to do
   }
   
   /**
    * @see org.alfresco.web.app.context.IContextListener#areaChanged()
    */
   public void areaChanged()
   {
      // nothing to do
      
      // NOTE: The code below is WIP for synchronizing the tree with
      //       the main navigation area of the application.
      
      /*
      this.resetSelectedNode();
      */
   }

   /**
    * @see org.alfresco.web.app.context.IContextListener#spaceChanged()
    */
   public void spaceChanged()
   {
      // nothing to do
      
      // NOTE: The code below is WIP for synchronizing the tree with
      //       the main navigation area of the application.
      
      /*
      NavigationBean navBean = getNavigationBean();
      if (navBean != null)
      {
         // get the current area and the new parent
         String area = navBean.getToolbarLocation();
         Node parent = navBean.getDispatchContextNode();
         
         if (parent != null)
         {
            if (logger.isDebugEnabled())
               logger.debug("Space changed, parent node is now: " + parent.getNodeRef().toString());
            
            // select the new parent node
            selectNode(parent.getNodeRef(), area);
            
            // get the nodes for the current area
            BrowseBean browseBean = getBrowseBean();
            Map<String, TreeNode> currentNodes = getNodesMapForArea(area);

            if (browseBean != null && currentNodes != null)
            {
               // find the parent node in the cache
               TreeNode parentNode = currentNodes.get(parent.getNodeRef().toString());
               if (parentNode != null)
               {
                  // reset the previously selected node
                  resetSelectedNode();
                  
                  // set the parent to expanded and selected
                  parentNode.setExpanded(true);
                  selectNode(parent.getNodeRef(), area);
               
                  for (Node child : browseBean.getNodes())
                  {
                     NodeRef nodeRef = child.getNodeRef();
                     
                     // check the child is applicable for the tree and is not already
                     // in the cache
                     if (isAddableChild(nodeRef) && 
                         currentNodes.containsKey(nodeRef.toString()) == false)
                     {
                        // create the child tree node and add to the cache
                        TreeNode childNode = createTreeNode(nodeRef);
                        parentNode.addChild(childNode);
                        currentNodes.put(childNode.getNodeRef(), childNode);
                     }
                  }
               }
            }
         }
      }
      */
   }
   
   // ------------------------------------------------------------------------------
   // Bean getters and setters
   
   /**
    * Returns the root nodes for the company home panel.
    * <p>
    * As the user expands and collapses nodes in the client this
    * cache will be updated with the appropriate nodes and states.
    * </p>
    * 
    * @return List of root nodes for the company home panel
    */
   public List<TreeNode> getCompanyHomeRootNodes()
   {
      if (this.companyHomeRootNodes == null)
      {
         this.companyHomeRootNodes = new ArrayList<TreeNode>();
         this.companyHomeNodes = new HashMap<String, TreeNode>();

         // query for the child nodes of company home
         NodeRef root = new NodeRef(Repository.getStoreRef(),
               Application.getCompanyRootId());         
         List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(root, 
               ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
         
         for (ChildAssociationRef ref: childRefs)
         {
            NodeRef child = ref.getChildRef();
            
            if (isAddableChild(child))
            {
               TreeNode node = createTreeNode(child);
               this.companyHomeRootNodes.add(node);
               this.companyHomeNodes.put(node.getNodeRef(), node);
            }
         }         
      }
      
      return this.companyHomeRootNodes;
   }
   
   /**
    * Returns the root nodes for the my home panel.
    * <p>
    * As the user expands and collapses nodes in the client this
    * cache will be updated with the appropriate nodes and states.
    * </p>
    * 
    * @return List of root nodes for the my home panel
    */
   public List<TreeNode> getMyHomeRootNodes()
   {
      if (this.myHomeRootNodes == null)
      {
         this.myHomeRootNodes = new ArrayList<TreeNode>();
         this.myHomeNodes = new HashMap<String, TreeNode>();

         // query for the child nodes of the user's home
         NodeRef root = new NodeRef(Repository.getStoreRef(),
               Application.getCurrentUser(FacesContext.getCurrentInstance()).getHomeSpaceId());         
         List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(root, 
               ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
         
         for (ChildAssociationRef ref: childRefs)
         {
            NodeRef child = ref.getChildRef();
            
            if (isAddableChild(child))
            {
               TreeNode node = createTreeNode(child);
               this.myHomeRootNodes.add(node);
               this.myHomeNodes.put(node.getNodeRef(), node);
            }
         }         
      }
      
      return this.myHomeRootNodes;
   }
   
   /**
    * Returns the root nodes for the guest home panel.
    * <p>
    * As the user expands and collapses nodes in the client this
    * cache will be updated with the appropriate nodes and states.
    * </p>
    * 
    * @return List of root nodes for the guest home panel
    */
   public List<TreeNode> getGuestHomeRootNodes()
   {
      if (this.guestHomeRootNodes == null)
      {
         this.guestHomeRootNodes = new ArrayList<TreeNode>();
         this.guestHomeNodes = new HashMap<String, TreeNode>();

         // query for the child nodes of the guest home space
         NavigationBean navBean = getNavigationBean();
         if (navBean != null)
         {
            NodeRef root = navBean.getGuestHomeNode().getNodeRef();
            List<ChildAssociationRef> childRefs = this.nodeService.getChildAssocs(root, 
                  ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            
            for (ChildAssociationRef ref: childRefs)
            {
               NodeRef child = ref.getChildRef();
               
               if (isAddableChild(child))
               {
                  TreeNode node = createTreeNode(child);
                  this.guestHomeRootNodes.add(node);
                  this.guestHomeNodes.put(node.getNodeRef(), node);
               }
            }
         }
      }
      
      return this.guestHomeRootNodes;
   }
   
   /**
    * @param nodeService The NodeService to set.
    */
   public void setNodeService(NodeService nodeService)
   {
      this.nodeService = nodeService;
   }
   
   /**
    * @param dictionaryService The DictionaryService to set.
    */
   public void setDictionaryService(DictionaryService dictionaryService)
   {
      this.dictionaryService = dictionaryService;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Sets the currently selected node in the tree
    * 
    * @param selectedNode The node that has been selected
    */
   public void selectNode(NodeRef selectedNode, String area)
   {
      // if there is a currently selected node, get hold of
      // it (from any of the areas) and reset to unselected
      if (this.previouslySelectedNode != null)
      {
         if (NavigationBean.LOCATION_COMPANY.equals(area) && 
             this.companyHomeNodes != null)
         {
            TreeNode node = this.companyHomeNodes.get(this.previouslySelectedNode.toString());
            if (node != null)
            {
               node.setSelected(false);
            }
         }
         else if (NavigationBean.LOCATION_HOME.equals(area) &&
                  this.myHomeNodes != null)
         {
            TreeNode node = this.myHomeNodes.get(this.previouslySelectedNode.toString());
            if (node != null)
            {
               node.setSelected(false);
            }
         }
         else if (NavigationBean.LOCATION_GUEST.equals(area) &&
                  this.guestHomeNodes != null)
         {
            TreeNode node = this.guestHomeNodes.get(this.previouslySelectedNode.toString());
            if (node != null)
            {
               node.setSelected(false);
            }
         }
      }

      // find the node just selected and set its state to selected
      if (selectedNode != null)
      {
         if (NavigationBean.LOCATION_COMPANY.equals(area) && 
             this.companyHomeNodes != null)
         {
            TreeNode node = this.companyHomeNodes.get(selectedNode.toString());
            if (node != null)
            {
               node.setSelected(true);
            }
         }
         else if (NavigationBean.LOCATION_HOME.equals(area) &&
                  this.myHomeNodes != null)
         {
            TreeNode node = this.myHomeNodes.get(selectedNode.toString());
            if (node != null)
            {
               node.setSelected(true);
            }
         }
         else if (NavigationBean.LOCATION_GUEST.equals(area) &&
                  this.guestHomeNodes != null)
         {
            TreeNode node = this.guestHomeNodes.get(selectedNode.toString());
            if (node != null)
            {
               node.setSelected(true);
            }
         }
      }

      this.previouslySelectedNode = selectedNode;
      
      if (logger.isDebugEnabled())
         logger.debug("Selected node: " + selectedNode);
   }
   
   /**
    * Resets the selected node
    */
   public void resetSelectedNode()
   {
      if (this.previouslySelectedNode != null)
      {
         if (this.companyHomeNodes != null)
         {
            TreeNode node = this.companyHomeNodes.get(this.previouslySelectedNode.toString());
            if (node != null)
            {
               node.setSelected(false);
            }
         }
         if (this.myHomeNodes != null)
         {
            TreeNode node = this.myHomeNodes.get(this.previouslySelectedNode.toString());
            if (node != null)
            {
               node.setSelected(false);
            }
         }
         if (this.guestHomeNodes != null)
         {
            TreeNode node = this.guestHomeNodes.get(this.previouslySelectedNode.toString());
            if (node != null)
            {
               node.setSelected(false);
            }
         }
         
         if (logger.isDebugEnabled())
            logger.debug("Reset selected node: " + this.previouslySelectedNode);
      }
   }
   
   /**
    * Resets all the caches held by the bean.
    */
   public void reset()
   {
      this.companyHomeNodes = null;
      this.companyHomeRootNodes = null;
      this.myHomeNodes = null;
      this.myHomeRootNodes = null;
      this.guestHomeNodes = null;
      this.guestHomeRootNodes = null;
      
      resetSelectedNode();
   }
   
   /**
    * Determines whether the given NodeRef can be added to the tree as 
    * a child for example, if it's a folder.
    * 
    * @param nodeRef The NodeRef to check
    * @return true if the node should be added to the tree
    */
   protected boolean isAddableChild(NodeRef nodeRef)
   {
      boolean addable = false;
      
      if (this.nodeService.exists(nodeRef))
      {
         // find it's type so we can see if it's a node we are interested in
         QName type = this.nodeService.getType(nodeRef);
         
         // make sure the type is defined in the data dictionary
         TypeDefinition typeDef = this.dictionaryService.getType(type);
         
         if (typeDef != null)
         {
            // look for folder node types
            if (this.dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true && 
                this.dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER) == false)
            {
               addable = true;
            }
         }
      }
      
      return addable;
   }
   
   /**
    * Creates a TreeNode object from the given NodeRef
    * 
    * @param nodeRef The NodeRef to create the TreeNode from
    */
   protected TreeNode createTreeNode(NodeRef nodeRef)
   {
      TreeNode node = new TreeNode(nodeRef.toString(), 
            Repository.getNameForNode(this.nodeService, nodeRef),
            (String)this.nodeService.getProperty(nodeRef, ApplicationModel.PROP_ICON));
      
      return node;
   }
   
   /**
    * Retrieves the instance of the NavigationBean being used by the application
    * 
    * @return NavigationBean instance
    */
   protected NavigationBean getNavigationBean()
   {
      return (NavigationBean)FacesHelper.getManagedBean(
            FacesContext.getCurrentInstance(), "NavigationBean");
   }
   
   /**
    * Retrieves the instance of the BrowseBean being used by the application
    * 
    * @return BrowseBean instance
    */
   protected BrowseBean getBrowseBean()
   {
      return (BrowseBean)FacesHelper.getManagedBean(
            FacesContext.getCurrentInstance(), "BrowseBean");
   }
   
   /**
    * Returns the map of tree nodes for the given area
    * 
    * @param area The area to retrieve the map for
    * @return The map of nodes
    */
   protected Map<String, TreeNode> getNodesMapForArea(String area)
   {
      Map<String, TreeNode> nodes = null;
      
      if (NavigationBean.LOCATION_COMPANY.equals(area))
      {
         nodes = this.companyHomeNodes;
      }
      else if (NavigationBean.LOCATION_HOME.equals(area))
      {
         nodes = this.myHomeNodes;
      }
      else if (NavigationBean.LOCATION_GUEST.equals(area))
      {
         nodes = this.guestHomeNodes;
      }
      
      return nodes;
   }
}
