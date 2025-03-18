/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rest.api.tests;

import org.springframework.context.ApplicationContext;

import org.alfresco.opencmis.OpenCMISClientContext;
import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.dictionary.M2Aspect;
import org.alfresco.repo.dictionary.M2Model;
import org.alfresco.repo.dictionary.M2Property;
import org.alfresco.service.namespace.QName;

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
