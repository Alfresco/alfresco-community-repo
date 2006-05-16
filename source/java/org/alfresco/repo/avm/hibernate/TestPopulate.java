/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.repo.avm.hibernate;

import org.alfresco.repo.avm.AVMNodeType;
import org.alfresco.repo.avm.hibernate.ContentBean;
import org.alfresco.repo.avm.hibernate.ContentBeanImpl;
import org.alfresco.repo.avm.hibernate.HibernateHelper;
import org.alfresco.repo.avm.hibernate.HibernateTxn;
import org.alfresco.repo.avm.hibernate.HibernateTxnCallback;
import org.alfresco.repo.avm.hibernate.Issuer;
import org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBean;
import org.alfresco.repo.avm.hibernate.PlainDirectoryNodeBeanImpl;
import org.alfresco.repo.avm.hibernate.PlainFileNodeBean;
import org.alfresco.repo.avm.hibernate.PlainFileNodeBeanImpl;
import org.alfresco.repo.avm.hibernate.RepositoryBean;
import org.alfresco.repo.avm.hibernate.RepositoryBeanImpl;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import junit.framework.TestCase;

public class TestPopulate extends TestCase
{
    /**
     * The SessionFactory.
     */
    private SessionFactory fSessionFactory;

    public TestPopulate(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        fSessionFactory = HibernateHelper.GetSessionFactory();
        Configuration cfg = HibernateHelper.GetConfiguration();
        SchemaExport se = new SchemaExport(cfg);
        se.drop(false, true);
        se.create(false, true);
    }

    protected void tearDown() throws Exception
    {
    }
    
    /**
     * Add some data to persistent store.
     */
    public void testPopulate()
    {
        try
        {
            HibernateTxn hTxn = new HibernateTxn(fSessionFactory);
            boolean result = hTxn.perform(
            new HibernateTxnCallback() 
            {
                public void perform(Session session)
                {
                    // Set up issuers.
                    Issuer nodeIssuer = new Issuer("node", 0);
                    Issuer contentIssuer = new Issuer("content", 0);
                    Issuer repositoryIssuer = new Issuer("repository", 0);
                    // Make the initial root directory.
                    long time = System.currentTimeMillis();
                    BasicAttributesBean attrs = new BasicAttributesBeanImpl("britt",
                                                                            "britt",
                                                                            "britt",
                                                                            time,
                                                                            time,
                                                                            time);
                    session.save(attrs);
                    PlainDirectoryNodeBean root =   
                        new PlainDirectoryNodeBeanImpl(nodeIssuer.issue(),        
                                                       0,
                                                       0,
                                                       null,
                                                       null,
                                                       null,
                                                       null,
                                                       attrs,
                                                       true);
                    // Make a new repository.
                    RepositoryBean rep = 
                        new RepositoryBeanImpl("main", root);
                    root.setRepository(rep);
                    session.save(rep);
                    rep.getRoots().put(rep.getNextVersionID(), root);
                    rep.setNextVersionID(rep.getNextVersionID() + 1);
                    session.save(nodeIssuer);
                    session.save(contentIssuer);
                    session.save(repositoryIssuer);
                    root.setIsNew(false);
                }
            });
            assertTrue(result);
            System.out.println("--------------------------------------------");
            result = hTxn.perform(
            new HibernateTxnCallback()
            {
                public void perform(Session session)
                {
                    // Now read some things back, and modify some stuff.
                    Issuer nodeIssuer = (Issuer)session.get(Issuer.class, "node");
                    Issuer contentIssuer = (Issuer)session.get(Issuer.class, "content");
                    RepositoryBean rep = (RepositoryBean)session.get(RepositoryBeanImpl.class, "main");
                    long version = rep.getNextVersionID();
                    rep.setNextVersionID(version + 1);
                    assertTrue(rep != null);
                    PlainDirectoryNodeBean root = (PlainDirectoryNodeBean)rep.getRoot();
                    long time = System.currentTimeMillis();
                    BasicAttributesBean attrs = new BasicAttributesBeanImpl("britt",
                                                                            "britt",
                                                                            "britt",
                                                                            time,
                                                                            time,
                                                                            time);
                    session.save(attrs);
                    PlainDirectoryNodeBean newRoot = new PlainDirectoryNodeBeanImpl(nodeIssuer.issue(),
                                                                                    version,
                                                                                    0L,
                                                                                    root,
                                                                                    null,
                                                                                    null,
                                                                                    rep,
                                                                                    attrs,
                                                                                    true);
                    ContentBean content = new ContentBeanImpl(contentIssuer.issue());
                    attrs = new BasicAttributesBeanImpl(attrs);
                    session.save(attrs);
                    PlainFileNodeBean file = new PlainFileNodeBeanImpl(nodeIssuer.issue(),
                                                                       version,
                                                                       0L,
                                                                       null,
                                                                       null,
                                                                       newRoot,
                                                                       rep,
                                                                       attrs,
                                                                       content);
                    content.setRefCount(content.getRefCount() + 1);
                    newRoot.getChildren().put("foo", new DirectoryEntry(AVMNodeType.PLAIN_FILE, file));
                    session.save(content);
                    session.save(newRoot);
                    content = new ContentBeanImpl(contentIssuer.issue());
                    content.setRefCount(content.getRefCount() + 1);
                    file.setIsNew(false);
                    attrs = new BasicAttributesBeanImpl(attrs);
                    session.save(attrs);
                    file = new PlainFileNodeBeanImpl(nodeIssuer.issue(),
                                                     version,
                                                     0L,
                                                     null,
                                                     null,
                                                     newRoot,
                                                     rep,
                                                     attrs,
                                                     content);
                    session.save(content);
                    file.setIsNew(false);
                    newRoot.getChildren().put("bar", new DirectoryEntry(AVMNodeType.PLAIN_FILE, file));
                    rep.setRoot(newRoot);
                    rep.getRoots().put(version, newRoot);
                    newRoot.setIsNew(false);
                }
            });
            assertTrue(result);
            System.out.println("-----------------------------------------------");
            result = hTxn.perform(
            new HibernateTxnCallback()
            {
                public void perform(Session session)
                {
                    Query query = session.createQuery("from RepositoryBeanImpl r where r.name = :name");
                    query.setString("name", "main");
                    RepositoryBean rep = (RepositoryBean)query.uniqueResult();
                    PlainDirectoryNodeBean root = (PlainDirectoryNodeBean)rep.getRoot();
                    assertEquals(2, root.getChildren().size());
                    for (String name : root.getChildren().keySet())
                    {
                        System.out.println(name);
                    }
                    for (DirectoryEntry entry : root.getChildren().values())
                    {
                        assertEquals(AVMNodeType.PLAIN_FILE, entry.getEntryType());
                    }
                    assertEquals("britt", root.getBasicAttributes().getCreator());
                }
            });
            assertTrue(result);
            System.out.println("----------------------------------------------");
            // Just check cascading deletes for the children of a directory.
            result = hTxn.perform(
            new HibernateTxnCallback()
            {
                public void perform(Session session)
                {
                    RepositoryBean rep = (RepositoryBean)session.get(RepositoryBeanImpl.class, "main");
                    PlainDirectoryNodeBean root = (PlainDirectoryNodeBean)rep.getRoot();
                    PlainDirectoryNodeBean prev = (PlainDirectoryNodeBean)root.getAncestor();
                    rep.getRoots().remove(rep.getRoot().getId());
                    rep.setRoot(prev);
                    for (String name : root.getChildren().keySet())
                    {
                        AVMNodeBean child = root.getChildren().get(name).getChild();
                        child.setParent(null);
                    }
                    session.delete(root);
                }
            });
            assertTrue(result);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail();
        }
    }
}
