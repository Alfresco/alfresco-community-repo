/**
 * 
 */
package org.alfresco.repo.avm;

import org.alfresco.repo.attributes.AttributeDAO;
import org.alfresco.repo.attributes.GlobalAttributeEntryDAO;
import org.alfresco.repo.attributes.ListEntryDAO;
import org.alfresco.repo.attributes.MapEntryDAO;

/**
 * This is the (shudder) global context for AVM.  It a rendezvous
 * point for access to needed global instances.
 * @author britt
 */
public class AVMDAOs
{
    /**
     * The single instance of an AVMContext.
     */
    private static AVMDAOs fgInstance;
    
    AVMDAOs()
    {
        fgInstance = this;
    }

    /**
     * Get the instance of this.
     * @return
     */
    public static AVMDAOs Instance()
    {
        return fgInstance;
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
    
    public AttributeDAO fAttributeDAO;
    
    public MapEntryDAO fMapEntryDAO;
    
    public GlobalAttributeEntryDAO fGlobalAttributeEntryDAO;
    
    public ListEntryDAO fListEntryDAO;
    
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
    
    public void setAttributeDAO(AttributeDAO dao)
    {
        fAttributeDAO = dao;
    }
    
    public void setMapEntryDAO(MapEntryDAO dao)
    {
        fMapEntryDAO = dao;
    }
    
    public void setGlobalAttributeEntryDAO(GlobalAttributeEntryDAO dao)
    {
        fGlobalAttributeEntryDAO = dao;
    }
    
    public void setListEntryDAO(ListEntryDAO dao)
    {
        fListEntryDAO = dao;
    }
}
