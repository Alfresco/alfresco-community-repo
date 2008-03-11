package org.alfresco.repo.domain;

import java.util.HashSet;
import java.util.Set;

import javax.transaction.UserTransaction;

import junit.framework.TestCase;

import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

/**
 * @see org.alfresco.repo.domain.ContentUrlDAO
 * 
 * @author Derek Hulley
 * @since 2.1
 */
public class ContentUrlDAOTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    private ContentUrlDAO dao;
    private TransactionService transactionService;
    private ContentService contentService;
    
    @Override
    protected void setUp() throws Exception
    {
        ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
        dao = (ContentUrlDAO) ctx.getBean("contentUrlDAO");
        contentService = serviceRegistry.getContentService();
        transactionService = serviceRegistry.getTransactionService();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
    }

    public void testCreateContentUrl() throws Throwable
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            RunAsWork<String> getTempWriterWork = new RunAsWork<String>()
            {
                public String doWork() throws Exception
                {
                    return contentService.getTempWriter().getContentUrl();
                }
            };
            String contentUrl = AuthenticationUtil.runAs(getTempWriterWork, AuthenticationUtil.SYSTEM_USER_NAME);
            // Make sure that it can be written in duplicate
            ContentUrl entity1 = dao.createContentUrl(contentUrl);
            ContentUrl entity2 = dao.createContentUrl(contentUrl);
            assertNotSame("Assigned IDs must be new", entity1.getId(), entity2.getId());
            
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }
    
    private Set<String> makeUrls(int count) throws Throwable
    {
        final Set<String> urls = new HashSet<String>(count);
        for (int i = 0; i < count; i++)
        {
            String contentUrl = String.format("%s%s/%04d", FileContentStore.STORE_PROTOCOL, getName(), i);
            dao.createContentUrl(contentUrl);
            urls.add(contentUrl);
        }
        return urls;
    }
    
    public void testGetAllContentUrls() throws Throwable
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            final Set<String> urls = makeUrls(1000);
            
            // Now iterate over them in the same transaction
            ContentUrlDAO.ContentUrlHandler handler = new ContentUrlDAO.ContentUrlHandler()
            {
                public void handle(String contentUrl)
                {
                    urls.remove(contentUrl);
                }
            };
            dao.getAllContentUrls(handler);
            assertEquals("Not all content URLs were enumerated", 0, urls.size());
            
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }
    
    public void testDeleteContentUrl() throws Throwable
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            final Set<String> urls = makeUrls(1000);
            // Delete them
            for (String url : urls)
            {
                dao.deleteContentUrl(url);
            }
            // Now iterate over them in the same transaction
            ContentUrlDAO.ContentUrlHandler handler = new ContentUrlDAO.ContentUrlHandler()
            {
                public void handle(String contentUrl)
                {
                    urls.remove(contentUrl);
                }
            };
            dao.getAllContentUrls(handler);
            // All the URLs previously deleted will not have been removed from the Set
            assertEquals("Specific content URLs were not deleted", 1000, urls.size());
            
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }
    
    public void testDeleteContentUrls() throws Throwable
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            final Set<String> urls = makeUrls(1000);
            // Delete them
            dao.deleteContentUrls(urls);
            // Now iterate over them in the same transaction
            ContentUrlDAO.ContentUrlHandler handler = new ContentUrlDAO.ContentUrlHandler()
            {
                public void handle(String contentUrl)
                {
                    urls.remove(contentUrl);
                }
            };
            dao.getAllContentUrls(handler);
            // All the URLs previously deleted will not have been removed from the Set
            assertEquals("Specific content URLs were not deleted", 1000, urls.size());
            
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }
    
    public void testDeleteAllContentUrls() throws Throwable
    {
        UserTransaction txn = transactionService.getUserTransaction();
        try
        {
            txn.begin();
            
            makeUrls(1000);
            // Delete them
            dao.deleteAllContentUrls();
            // Check that there are none left
            
            // Now iterate over them in the same transaction
            ContentUrlDAO.ContentUrlHandler handler = new ContentUrlDAO.ContentUrlHandler()
            {
                public void handle(String contentUrl)
                {
                    fail("There should not be any URLs remaining.");
                }
            };
            dao.getAllContentUrls(handler);
            
            txn.commit();
        }
        catch (Throwable e)
        {
            try { txn.rollback(); } catch (Throwable ee) {}
            throw e;
        }
    }
}
