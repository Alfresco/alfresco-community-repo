package org.alfresco.opencmis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:alfresco/application-context.xml"})
public class CMISDictionaryTest
{
	private DictionaryDAO dictionaryDAO;
    private CMISDictionaryService cmisDictionaryService;

    @Autowired
    private ApplicationContext applicationContext;

	@Before
	public void before()
	{
		this.dictionaryDAO = (DictionaryDAO)applicationContext.getBean("dictionaryDAO");
		this.cmisDictionaryService = (CMISDictionaryService)applicationContext.getBean("OpenCMISDictionaryService1.1");
	}

	@Test
	public void test1()
	{
        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
			@Override
			public Void doWork() throws Exception
			{
				M2Model customModel = M2Model.createModel(
						Thread.currentThread().getContextClassLoader().
						getResourceAsStream("dictionary/dictionarydaotest_model1.xml"));
				dictionaryDAO.putModel(customModel);
				assertNotNull(cmisDictionaryService.findType("P:cm:dublincore"));
				TypeDefinitionWrapper td = cmisDictionaryService.findType("D:daotest1:type1");
				assertNotNull(td);
				return null;
			}
		}, "user1", "tenant1");

        TenantUtil.runAsUserTenant(new TenantRunAsWork<Void>()
        {
			@Override
			public Void doWork() throws Exception
			{
				assertNotNull(cmisDictionaryService.findType("P:cm:dublincore"));
				TypeDefinitionWrapper td = cmisDictionaryService.findType("D:daotest1:type1");
				assertNull(td);
				return null;
			}
		}, "user2", "tenant2");
	}
}
