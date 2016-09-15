/*
 * #%L
 * Alfresco Data model classes
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
package org.alfresco.repo.dictionary;

import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Core dictionary registry (holding core models initialised at bootstrap).
 * 
 * @author sglover
 *
 */
public class CoreDictionaryRegistryImpl extends AbstractDictionaryRegistry
{
    public CoreDictionaryRegistryImpl(DictionaryDAO dictionaryDAO)
    {
    	super(dictionaryDAO);
    }

    @Override
    public String getTenantDomain()
    {
        return null;
    }

    @Override
    protected void initImpl()
    {
    	long startTime = System.currentTimeMillis();

        // populate the dictionary based on registered sources (only for core registry)
        for (DictionaryListener dictionaryDeployer : dictionaryDAO.getDictionaryListeners())
        {
            dictionaryDeployer.onDictionaryInit();
        }

        // notify registered listeners that dictionary has been initialised (population is complete)
        for (DictionaryListener dictionaryListener : dictionaryDAO.getDictionaryListeners())
        {
            dictionaryListener.afterDictionaryInit();
        }

        // Done
        if (logger.isInfoEnabled())
        {
        	Map<QName, CompiledModel> models = getCompiledModels(false);
            logger.info("Init core dictionary: model count = "+(models != null ? models.size() : 0)
            		+" in "+(System.currentTimeMillis()-startTime)+" msecs ["+Thread.currentThread()+"]");
        }
    }

	@Override
	public void remove()
	{
		for(DictionaryListener listener : dictionaryDAO.getDictionaryListeners())
		{
			listener.afterDictionaryDestroy();
		}
	}

	@Override
    protected QName putModelImpl(CompiledModel model)
	{
		// TODO disallow model overrides for the core dictionary

//		if(compiledModels.get(model.getModelDefinition().getName()) != null)
//		{
//			throw new AlfrescoRuntimeException("Cannot override existing model " + model.getModelDefinition().getName());
//		}
//
//		for(M2Namespace namespace : model.getM2Model().getNamespaces())
//		{
//			if(uriToModels.get(namespace.getUri()) != null)
//			{
//				throw new AlfrescoRuntimeException("Cannot override existing namespace " + namespace.getUri());
//			}
//		}

		QName qname = super.putModelImpl(model);

//		if(dictionaryDAO.isContextRefreshed())
//		{
//			for(DictionaryListener listener : dictionaryDAO.getDictionaryListeners())
//			{
//				if(listener instanceof ExtendedDictionaryListener)
//				{
//					((ExtendedDictionaryListener)listener).coreModelAdded(model);
//				}
//			}
//		}

        return qname;
	}

	@Override
	public void removeImpl()
	{
		for(DictionaryListener listener : dictionaryDAO.getDictionaryListeners())
		{
			listener.afterDictionaryDestroy();
		}
	}
}