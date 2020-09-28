/*
 * #%L
 * Alfresco Repository
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

package org.alfresco.repo.virtual.ref;

/**
 * Returns a virtual parent reference upon execution by subtracting the last
 * template path element from of the given reference.<br>
 * For root template path references a <code>null</code> will be returned upon
 * execution.
 * 
 * @see VirtualProtocol#replaceTemplatePath(Reference, String)
 * @author Bogdan Horje
 */
public class GetParentReferenceMethod extends AbstractProtocolMethod<Reference>
{
    @Override
    public Reference execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        String path = virtualProtocol.getTemplatePath(reference);
        if (path.trim().endsWith(PATH_SEPARATOR))
        {

            int trailingPathIndex = path.lastIndexOf(PATH_SEPARATOR);
            if (trailingPathIndex == 0)
            {
                return null;
            }
            else
            {
                path = path.substring(0,
                                      trailingPathIndex);
            }
        }

        int index = path.lastIndexOf(PATH_SEPARATOR);
        if (index < 0)
        {
            return null;
        }
        else
        {
            String parentPath = path.substring(0,
                                               index);

            if (parentPath.isEmpty())
            {
                if (path.length() > 1)
                {
                    parentPath = PATH_SEPARATOR;
                }
                else
                {
                    return null;
                }
            }

            return virtualProtocol.replaceTemplatePath(reference,
                                                       parentPath);
        }
    }

    @Override
    public Reference execute(NodeProtocol protocol, Reference reference) throws ProtocolMethodException
    {
        return ((ReferenceParameter) reference.getParameters().get(0)).getValue();
    }
}
