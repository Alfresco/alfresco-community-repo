/**
 * 
 */
package org.alfresco.repo.avm;

/**
 * This is the (shudder) global context for AVM.  It a rendezvous
 * point for access to needed global instances.
 * @author britt
 */
public class AVMContext
{
    /**
     * The single instance of an AVMContext.
     */
    public static AVMContext fgInstance;
    
    public AVMContext()
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
     *  The Repository DAO.
     */
    public RepositoryDAO fRepositoryDAO;
    
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
     * The NewInRepositoryDAO
     */
    public NewInRepositoryDAO fNewInRepositoryDAO;
    
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
     * @param repositoryDAO the fRepositoryDAO to set
     */
    public void setRepositoryDAO(RepositoryDAO repositoryDAO)
    {
        fRepositoryDAO = repositoryDAO;
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
     * @param newInRepositoryDAO The DAO to set.
     */
    public void setNewInRepositoryDAO(NewInRepositoryDAO newInRepositoryDAO)
    {
        fNewInRepositoryDAO = newInRepositoryDAO;
    }
}
