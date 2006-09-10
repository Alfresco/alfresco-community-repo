/**
 * 
 */
package org.alfresco.repo.avm;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.avm.AVMService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This is the (shudder) global context for AVM.  It a rendezvous
 * point for access to needed global instances.
 * @author britt
 */
public class AVMContext implements ApplicationContextAware
{
    /**
     * The single instance of an AVMContext.
     */
    public static AVMContext fgInstance;
    
    AVMContext()
    {
        fgInstance = this;
    }

    /**
     * The IssuerDAO.
     */
    public IssuerDAO fIssuerDAO;
    
    /**
     * The AVMNodeDAO.
     */
    public AVMNodeDAO fAVMNodeDAO;
    
    /**
     *  The AVMStore DAO.
     */
    public AVMStoreDAO fAVMStoreDAO;
    
    /**
     * The VersionRootDAO.
     */
    public VersionRootDAO fVersionRootDAO;
    
    /**
     * The ChildEntryDAO.
     */
    public ChildEntryDAO fChildEntryDAO;
    
    /**
     * The HistoryLinkDAO.
     */
    public HistoryLinkDAO fHistoryLinkDAO;
    
    /**
     * The MergeLinkDAO.
     */
    public MergeLinkDAO fMergeLinkDAO;
    
    /**
     * The AVMNodePropertyDAO
     */
    public AVMNodePropertyDAO fAVMNodePropertyDAO;
    
    /**
     * The AVMStorePropertyDAO
     */
    public AVMStorePropertyDAO fAVMStorePropertyDAO;
    
    /**
     * The AVMAspectNameDAO
     */
    public AVMAspectNameDAO fAVMAspectNameDAO;
    
    /**
     * The ContentService.
     */
    private ContentService fContentService;
    
    /**
     * The Mimetype Service.
     */
    private MimetypeService fMimetypeService;
 
    /**
     * The AVMService.
     */
    private AVMService fAVMService;
    
    /**
     * The Content Store.
     */
    private ContentStore fContentStore;
    
    /**
     * The DictionaryService
     */
    private DictionaryService fDictionaryService;
        
    /**
     * The Node Service.
     */
    private NodeService fNodeService;
    
    private AuthenticationComponent fAuthenticationComponent;
    
    /**
     * The application context.
     */
    public ApplicationContext fAppContext;
    
    public void setApplicationContext(ApplicationContext context)
    {
        fAppContext = context;
    }
    
    /**
     * @param nodeDAO the fAVMNodeDAO to set
     */
    public void setNodeDAO(AVMNodeDAO nodeDAO)
    {
        fAVMNodeDAO = nodeDAO;
    }

    /**
     * @param childEntryDAO the fChildEntryDAO to set
     */
    public void setChildEntryDAO(ChildEntryDAO childEntryDAO)
    {
        fChildEntryDAO = childEntryDAO;
    }

    /**
     * @param historyLinkDAO the fHistoryLinkDAO to set
     */
    public void setHistoryLinkDAO(HistoryLinkDAO historyLinkDAO)
    {
        fHistoryLinkDAO = historyLinkDAO;
    }

    /**
     * @param mergeLinkDAO the fMergeLinkDAO to set
     */
    public void setMergeLinkDAO(MergeLinkDAO mergeLinkDAO)
    {
        fMergeLinkDAO = mergeLinkDAO;
    }

    /**
     * @param aVMStoreDAO The fAVMStoreDAO to set
     */
    public void setAvmStoreDAO(AVMStoreDAO aVMStoreDAO)
    {
        fAVMStoreDAO = aVMStoreDAO;
    }

    /**
     * @param versionRootDAO the fVersionRootDAO to set
     */
    public void setVersionRootDAO(VersionRootDAO versionRootDAO)
    {
        fVersionRootDAO = versionRootDAO;
    }

    /**
     * @param issuerDAO the fIssuerDAO to set
     */
    public void setIssuerDAO(IssuerDAO issuerDAO)
    {
        fIssuerDAO = issuerDAO;
    }
    
    public void setAvmNodePropertyDAO(AVMNodePropertyDAO avmNodePropertyDAO)
    {
        fAVMNodePropertyDAO = avmNodePropertyDAO;
    }
    
    public void setAvmStorePropertyDAO(AVMStorePropertyDAO avmStorePropertyDAO)
    {
        fAVMStorePropertyDAO = avmStorePropertyDAO;
    }
    
    public void setAvmAspectNameDAO(AVMAspectNameDAO avmAspectNameDAO)
    {
        fAVMAspectNameDAO = avmAspectNameDAO;
    }
    
    /**
     * Get the Content Service.
     * @return The ContentService object.
     */
    public ContentService getContentService()
    {
        if (fContentService == null)
        {
            fContentService = (ContentService)fAppContext.getBean("contentService");
        }
        return fContentService;
    }
    
    /**
     * Get the mime type service.
     * @return The mime type service.
     */
    public MimetypeService getMimetypeService()
    {
        if (fMimetypeService == null)
        {
            fMimetypeService = (MimetypeService)fAppContext.getBean("mimetypeService");
        }
        return fMimetypeService;
    }
    
    /**
     * Get the AVM Service.
     * @return The AVMService instance.
     */
    public AVMService getAVMService()
    {
        if (fAVMService == null)
        {
            fAVMService = (AVMService)fAppContext.getBean("avmService");
        }
        return fAVMService;
    }
    
    /**
     * Get the ContentStore.
     * @return The content store.
     */
    public ContentStore getContentStore()
    {
        if (fContentStore == null)
        {
            fContentStore = (ContentStore)fAppContext.getBean("fileContentStore");
        }
        return fContentStore;
    }
    
    /**
     * Get the DictionaryService.
     * @return The dictionary service.
     */
    public DictionaryService getDictionaryService()
    {
        if (fDictionaryService == null)
        {
            // TODO Should this be DictionaryService or dictionaryService.
            fDictionaryService = (DictionaryService)fAppContext.getBean("dictionaryService");
        }
        return fDictionaryService;
    }
    
    /**
     * Get the NodeService
     * @return The Node service.
     */
    public NodeService getNodeService()
    {
        if (fNodeService == null)
        {
            fNodeService = (NodeService)fAppContext.getBean("nodeService");
        }
        return fNodeService;
    }
    
    public AuthenticationComponent getAuthenticationComponent()
    {
        if (fAuthenticationComponent == null)
        {
            fAuthenticationComponent = (AuthenticationComponent)fAppContext.getBean("authenticationComponentImpl");
        }
        return fAuthenticationComponent;
    }
}
