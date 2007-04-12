/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing
 */

package org.alfresco.repo.admin.patch.impl;

import java.io.Serializable;
import java.util.List;

import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.importer.ImporterBootstrap;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.ConstraintException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.cmr.repository.datatype.TypeConversionException;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;

/**
 * Patch usr:user and cm:person objects so that the user name properties are in the index in untokenized form. If not authentication may fail in mixed language use.
 * 
 * @author andyh
 */
public class InvalidUserPersonAndGroupPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.invalidUserPersonAndGroup.result";

    private ImporterBootstrap spacesImporterBootstrap;

    private ImporterBootstrap userImporterBootstrap;

    private DictionaryService dictionaryService;

    public InvalidUserPersonAndGroupPatch()
    {

    }

    public void setSpacesImporterBootstrap(ImporterBootstrap spacesImporterBootstrap)
    {
        this.spacesImporterBootstrap = spacesImporterBootstrap;
    }

    public void setUserImporterBootstrap(ImporterBootstrap userImporterBootstrap)
    {
        this.userImporterBootstrap = userImporterBootstrap;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    @Override
    protected String applyInternal() throws Exception
    {
        int users = deleteInvalid(ContentModel.PROP_USER_USERNAME, userImporterBootstrap.getStoreRef(), "USER_");
        int people = deleteInvalid(ContentModel.PROP_USERNAME, spacesImporterBootstrap.getStoreRef(), "USER_");
        int authorities = deleteInvalid(ContentModel.PROP_AUTHORITY_NAME, userImporterBootstrap.getStoreRef(), "GROUP_");
        return I18NUtil.getMessage(MSG_SUCCESS, users, people, authorities);
    }

    private int deleteInvalid(QName property, StoreRef store, String prefix)
    {
        PropertyDefinition propDef = dictionaryService.getProperty(property);
        if (propDef == null)
        {
            return 0;
        }
        ClassDefinition typeDef = propDef.getContainerClass();

        String query;
        if (typeDef.isAspect())
        {
            query = "ASPECT:\"" + typeDef.getName() + "\"";
        }
        else
        {
            query = "TYPE:\"" + typeDef.getName() + "\"";
        }

        List<ConstraintDefinition> conDefs = propDef.getConstraints();

        SearchParameters sp = new SearchParameters();
        sp.setLanguage(SearchService.LANGUAGE_LUCENE);
        sp.setQuery(query);
        sp.addStore(store);
        ResultSet rs = null;
        int invalidCount = 0;
        try
        {
            rs = searchService.query(sp);
            for (ResultSetRow row : rs)
            {
                NodeRef nodeRef = row.getNodeRef();
                boolean valid = true;
                Serializable  value = nodeService.getProperty(nodeRef, property);
                String checkValue = null;
                try
                {
                    checkValue = DefaultTypeConverter.INSTANCE.convert(String.class, value);
                }
                catch (TypeConversionException e)
                {
                   continue;
                }
                
                for (ConstraintDefinition con : conDefs)
                {
                    try
                    {
                        con.getConstraint().evaluate(value);
                    }
                    catch (ConstraintException e)
                    {
                        valid = false;
                    }
                }
                if(!valid)
                {
                    nodeService.setProperty(nodeRef, property, prefix+checkValue);
                    invalidCount++;
                }
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        return invalidCount;
    }
}
