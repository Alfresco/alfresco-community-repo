/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * This is the (shudder) global context for AVM.  It a rendezvous
 * point for access to needed global instances.
 * @author britt
 */
class AVMContext
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
     * The FileContentDAO.
     */
    public FileContentDAO fFileContentDAO;
    
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
     * The DeletedChildDAO.
     */
    public DeletedChildDAO fDeletedChildDAO;

    /**
     * The NewInAVMStoreDAO
     */
    public NewInAVMStoreDAO fNewInAVMStoreDAO;
    
    /**
     * The AVMNodePropertyDAO
     */
    public AVMNodePropertyDAO fAVMNodePropertyDAO;
    
    /**
     * The AVMStorePropertyDAO
     */
    public AVMStorePropertyDAO fAVMStorePropertyDAO;
    
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
     * @param deletedChildDAO the fDeletedChildDAO to set
     */
    public void setDeletedChildDAO(DeletedChildDAO deletedChildDAO)
    {
        fDeletedChildDAO = deletedChildDAO;
    }

    /**
     * @param fileContentDAO the fFileContentDAO to set
     */
    public void setFileContentDAO(FileContentDAO fileContentDAO)
    {
        fFileContentDAO = fileContentDAO;
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
    
    /**
     * @param newInAVMStoreDAO The DAO to set.
     */
    public void setNewInAVMStoreDAO(NewInAVMStoreDAO newInAVMStoreDAO)
    {
        fNewInAVMStoreDAO = newInAVMStoreDAO;
    }
    
    public void setAvmNodePropertyDAO(AVMNodePropertyDAO avmNodePropertyDAO)
    {
        fAVMNodePropertyDAO = avmNodePropertyDAO;
    }
    
    public void setAvmStorePropertyDAO(AVMStorePropertyDAO avmStorePropertyDAO)
    {
        fAVMStorePropertyDAO = avmStorePropertyDAO;
    }
}
