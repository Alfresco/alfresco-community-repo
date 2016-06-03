package org.alfresco.rest.api.tests;

import org.alfresco.opencmis.OpenCMISClientContext;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.service.namespace.QName;
import org.springframework.context.ApplicationContext;

/**
 * Base class for Chemistry OpenCMIS TCK tests.
 * 
 * @author steveglover
 *
 */
public abstract class AbstractEnterpriseOpenCMISTCKTest extends EnterpriseTestApi
{
	protected static OpenCMISClientContext clientContext;
	
   @Override
    protected TestFixture getTestFixture() throws Exception
    {
        return EnterprisePublicApiTestFixture.getInstance();
    }
   
    protected void overrideVersionableAspectProperties(ApplicationContext ctx)
    {
        final DictionaryDAO dictionaryDAO = (DictionaryDAO) ctx.getBean("dictionaryDAO");
        dictionaryDAO.removeModel(QName.createQName("cm:contentmodel"));
        M2Model contentModel = M2Model.createModel(getClass().getClassLoader().getResourceAsStream("alfresco/model/contentModel.xml"));

        M2Aspect versionableAspect = contentModel.getAspect("cm:versionable");
        M2Property prop = versionableAspect.getProperty("cm:initialVersion"); 
        prop.setDefaultValue(Boolean.FALSE.toString());
        prop = versionableAspect.getProperty("cm:autoVersion"); 
        prop.setDefaultValue(Boolean.FALSE.toString());
        prop = versionableAspect.getProperty("cm:autoVersionOnUpdateProps"); 
        prop.setDefaultValue(Boolean.FALSE.toString());

        dictionaryDAO.putModel(contentModel);
    }
}
