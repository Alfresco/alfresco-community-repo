/**
 * 
 */
package org.alfresco.repo.avm.util;

import org.alfresco.repo.avm.LookupCache;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.simple.permission.AuthorityCapabilityRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Simple access to Raw versions of service singletons.
 * @author britt
 */
public class RawServices implements ApplicationContextAware
{
    /**
     * The instance of RawServices
     */
    private static RawServices fgInstance;
    
    /**
     * The Application Context.
     */
    private ApplicationContext fContext; 
    
    /**
     * The AuthenticationComponent.
     */
    private AuthenticationComponent fAuthenticationComponent;
    
    /**
     * The Content Service.
     */
    private ContentService fContentService;
    
    /**
     * The Mimetype Service.
     */
    private MimetypeService fMimetypeService;
    
    /**
     * The Dictionary Service.
     */
    private DictionaryService fDictionaryService;
    
    /**
     * The Content Store.
     */
    private ContentStore fContentStore;
    
    /**
     * The LookupCache.
     */
    private LookupCache fLookupCache;
    
    /**
     * The Authority Service.
     */
    private AuthorityService fAuthorityService;
    
    /**
     * The CapabilityRegistry.
     */
    private AuthorityCapabilityRegistry fCapabilityRegistry;
    
    /**
     * Default constructor.
     */
    public RawServices()
    {
        fgInstance = this;    
    }
    
    public static RawServices Instance()
    {
        return fgInstance;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        fContext = applicationContext;
    }
    
    public AuthenticationComponent getAuthenticationComponent()
    {
        if (fAuthenticationComponent == null)
        {
            fAuthenticationComponent = 
                (AuthenticationComponent)fContext.getBean("authenticationComponent");
        }
        return fAuthenticationComponent;
    }

    public ContentService getContentService()
    {
        if (fContentService == null)
        {
            fContentService = 
                (ContentService)fContext.getBean("contentService");
        }
        return fContentService;
    }

    public MimetypeService getMimetypeService()
    {
        if (fMimetypeService == null)
        {
            fMimetypeService = 
                (MimetypeService)fContext.getBean("mimetypeService");
        }
        return fMimetypeService;
    }
    
    public DictionaryService getDictionaryService()
    {
        if (fDictionaryService == null)
        {
            fDictionaryService =
                (DictionaryService)fContext.getBean("dictionaryService");
        }
        return fDictionaryService;
    }
    
    public ContentStore getContentStore()
    {
        if (fContentStore == null)
        {
            fContentStore =
                (ContentStore)fContext.getBean("fileContentStore");
        }
        return fContentStore;
    }
    
    public LookupCache getLookupCache()
    {
        if (fLookupCache == null)
        {
            fLookupCache = (LookupCache)fContext.getBean("lookupCache");
        }
        return fLookupCache;
    }
    
    public AuthorityService getAuthorityService()
    {
        if (fAuthorityService == null)
        {
            // TODO change this back to the unwrapped bean before production.
            fAuthorityService = (AuthorityService)fContext.getBean("AuthorityService");
        }
        return fAuthorityService;
    }

    public AuthorityCapabilityRegistry getAuthorityCapabilityRegistry()
    {
        if (fCapabilityRegistry == null)
        {
            fCapabilityRegistry = (AuthorityCapabilityRegistry)fContext.getBean("authorityCapabilityRegistry");
        }
        return fCapabilityRegistry;
    }
    
    public ApplicationContext getContext()
    {
        return fContext;
    }
}
