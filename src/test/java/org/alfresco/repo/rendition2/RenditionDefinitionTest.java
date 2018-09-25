/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.repo.rendition2;

import junit.framework.TestCase;
import org.alfresco.repo.rendition.RenditionServiceImpl;
import org.alfresco.repo.security.authentication.AuthenticationComponent;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.repo.thumbnail.ThumbnailRenditionConvertor;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.alfresco.util.ApplicationContextHelper;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Tests that the replacement {@link RenditionDefinition2} generates the same {@link TransformationOptions} as the
 * original {@link RenditionDefinition} and that there are the same definitions available.
 *
 * @deprecated will be removed when the original {@link RenditionService} is removed.
 *
 * @author adavis
 */
@Deprecated
public class RenditionDefinitionTest extends TestCase
{
    private RenditionServiceImpl renditionService;
    private RenditionDefinitionRegistry2 renditionDefinitionRegistry2;

    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();

    private AuthenticationComponent authenticationComponent;

    @Override
    protected void setUp() throws Exception
    {
        authenticationComponent = (AuthenticationComponent) ctx.getBean("AuthenticationComponent");
        renditionService = (RenditionServiceImpl) ctx.getBean("renditionService");
        renditionDefinitionRegistry2 = (RenditionDefinitionRegistry2) ctx.getBean("renditionDefinitionRegistry2");
        authenticationComponent.setSystemUserAsCurrentUser();
    }

    @Override
    protected void tearDown() throws Exception
    {
        try
        {
            authenticationComponent.clearCurrentSecurityContext();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private RenditionDefinition getRenditionDefinition(List<RenditionDefinition> renditionDefinitions, String renditionName)
    {
        for (RenditionDefinition renditionDefinition: renditionDefinitions)
        {
            String name = renditionDefinition.getRenditionName().getLocalName();
            if (name.equals(renditionName))
            {
                return renditionDefinition;
            }
        }
        return null;
    }

    public void testGetRenderingEngineDefinition() throws Exception
    {
        ThumbnailRenditionConvertor converter = new ThumbnailRenditionConvertor();
        List<RenditionDefinition> renditionDefinitions = new ArrayList(renditionService.loadRenditionDefinitions());
        Set<String> renditionNames = renditionDefinitionRegistry2.getRenditionNames();

        for (String renditionName: renditionNames)
        {
            System.out.println("renditionName="+renditionName);

            RenditionDefinition definition = getRenditionDefinition(renditionDefinitions, renditionName);
            assertNotNull("There is no RenditionDefinition for "+renditionName, definition);
            renditionDefinitions.remove(definition);

            ThumbnailDefinition thumbnailDefinition = converter.convert(definition);
            TransformationOptions transformationOptions = thumbnailDefinition.getTransformationOptions();

            RenditionDefinition2 definition2 = renditionDefinitionRegistry2.getRenditionDefinition(renditionName);
            Map<String, String> options = definition2.getTransformOptions();
            TransformationOptions transformationOptions2 = LocalTransformClient.getTransformationOptions(renditionName, options);
            transformationOptions2.setUse(null); // The use is not set in the original until much later

            // These 2 original thumbnails are wrong, as they don't include the 'limits' and in the
            // case of 'pdf' used the wrong TransformationOptions subclass, so don't use them.
            if (!renditionName.equals("pdf") && !renditionName.equals("webpreview"))
            {
                assertEquals("The TransformationOptions used in transforms for " + renditionName + " should be the same",
                        transformationOptions.toStringAll(), transformationOptions2.toStringAll());
            }
        }

        if (!renditionDefinitions.isEmpty())
        {
            StringJoiner sj = new StringJoiner(", ");
            for (RenditionDefinition renditionDefinition : renditionDefinitions)
            {
                String name = renditionDefinition.getRenditionName().getLocalName();
                sj.add(name);
            }
            fail("There is no RenditionDefinition2 for existing RenditionDefinitions "+sj);
        }
    }
}
