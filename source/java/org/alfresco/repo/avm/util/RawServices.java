/**
 *
 */
package org.alfresco.repo.avm.util;

import org.alfresco.repo.avm.LookupCache;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.security.authentication.AuthenticationContext;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.security.AuthorityService;
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
     * The AuthenticationContext.
     */
    private AuthenticationContext fAuthenticationContext;

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

    public AuthenticationContext getAuthenticationContext()
    {
        if (fAuthenticationContext == null)
        {
            fAuthenticationContext =
                (AuthenticationContext)fContext.getBean("authenticationContext");
        }
        return fAuthenticationContext;
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

    public ApplicationContext getContext()
    {
        return fContext;
    }
}
