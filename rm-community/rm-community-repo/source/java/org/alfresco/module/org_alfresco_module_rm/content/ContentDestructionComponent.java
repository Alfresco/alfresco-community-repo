package org.alfresco.module.org_alfresco_module_rm.content;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * Content destruction component.
 *
 * @author Roy Wetherall
 * @since 2.4.a
 */
@BehaviourBean
public class ContentDestructionComponent
{
    /** eager content store cleaner */
    private EagerContentStoreCleaner eagerContentStoreCleaner;

    /** dictionary service */
    private DictionaryService dictionaryService;

    /** node service */
    private NodeService nodeService;

    /** behaviour filter */
    private BehaviourFilter behaviourFilter;

    /** indicates whether cleansing is enabled or not */
    private boolean cleansingEnabled = false;

    /**
     * @return the eagerContentStoreCleaner
     */
    protected EagerContentStoreCleaner getEagerContentStoreCleaner()
    {
        return this.eagerContentStoreCleaner;
    }

    /**
     * @return the dictionaryService
     */
    protected DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
    }

    /**
     * @return the nodeService
     */
    protected NodeService getNodeService()
    {
        return this.nodeService;
    }

    /**
     * @return the behaviourFilter
     */
    protected BehaviourFilter getBehaviourFilter()
    {
        return this.behaviourFilter;
    }

    /**
     * @return  true if cleansing is enabled, false otherwise
     */
    public boolean isCleansingEnabled()
    {
        return cleansingEnabled;
    }

    /**
     * @param eagerContentStoreCleaner  eager content store cleaner
     */
    public void setEagerContentStoreCleaner(EagerContentStoreCleaner eagerContentStoreCleaner)
    {
        this.eagerContentStoreCleaner = eagerContentStoreCleaner;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param behaviourFilter   behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @param cleansingEnabled  true if cleansing enabled, false otherwise
     */
    public void setCleansingEnabled(boolean cleansingEnabled)
    {
        this.cleansingEnabled = cleansingEnabled;
    }

    /**
     * Destroy content
     *
     * @param nodeRef
     */
    public void destroyContent(NodeRef nodeRef)
    {
        destroyContent(nodeRef, true);
    }

    /**
     * Destroy content
     *
     * @param nodeRef
     * @param includeRenditions
     */
    @SuppressWarnings("deprecation")
    public void destroyContent(NodeRef nodeRef, boolean includeRenditions)
    {
        // destroy the nodes content properties
        registerAllContentForDestruction(nodeRef, true);

        // Remove the renditioned aspect (and its properties and associations) if it is present.
        //
        // From Alfresco 3.3 it is the rn:renditioned aspect which defines the
        // child-association being considered in this method.
        // Note also that the cm:thumbnailed aspect extends the rn:renditioned aspect.
        //
        // We want to remove the rn:renditioned aspect, but due to the possibility
        // that there is Alfresco 3.2-era data with the cm:thumbnailed aspect
        // applied, we must consider removing it too.
      if (includeRenditions
              && (getNodeService().hasAspect(nodeRef, RenditionModel.ASPECT_RENDITIONED)
                      || getNodeService().hasAspect(nodeRef, ContentModel.ASPECT_THUMBNAILED)))
      {
          // get the rendition assoc types
          Set<QName> childAssocTypes = dictionaryService.getAspect(RenditionModel.ASPECT_RENDITIONED).getChildAssociations().keySet();
          for (ChildAssociationRef child : getNodeService().getChildAssocs(nodeRef))
          {
              if (childAssocTypes.contains(child.getTypeQName()))
              {
                  // destroy renditions content
                  destroyContent(child.getChildRef(), false);
                  
                  //delete the rendition node
                  getNodeService().deleteNode(child.getChildRef());
              }
          }
       }
    }

    /**
     * Registers all content on the given node for destruction.
     *
     * @param nodeRef               node reference
     * @param clearContentProperty  if true then clear content property, otherwise false
     */
    protected void registerAllContentForDestruction(NodeRef nodeRef, boolean clearContentProperty)
    {
        Map<QName, Serializable> properties = getNodeService().getProperties(nodeRef);

        for (Map.Entry<QName, Serializable> entry : properties.entrySet())
        {
            if (entry.getValue() instanceof ContentData)
            {
                // get content data
                ContentData dataContent = (ContentData)entry.getValue();

                // if enabled cleanse content
                if (isCleansingEnabled())
                {
                    // register for cleanse then immediate destruction
                    getEagerContentStoreCleaner().registerOrphanedContentUrlForCleansing(dataContent.getContentUrl());
                }
                else
                {
                    // register for immediate destruction
                    getEagerContentStoreCleaner().registerOrphanedContentUrl(dataContent.getContentUrl(), true);
                }

                // clear the property
                if (clearContentProperty)
                {
                    // disable behaviours to ensure no side effects
                    behaviourFilter.disableBehaviour();
                    try
                    {
                        getNodeService().removeProperty(nodeRef, entry.getKey());
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour();
                    }
                }
            }
        }
    }
}
