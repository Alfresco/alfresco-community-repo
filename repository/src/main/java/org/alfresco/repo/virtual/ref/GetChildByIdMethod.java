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

public class GetChildByIdMethod extends AbstractProtocolMethod<Reference>
{
    private String childId;

    public GetChildByIdMethod(String childId)
    {
        super();
        this.childId = childId;
    }

    /**
     * Provides a child {@link Reference} obtained from the parent
     * {@link Reference} and the childId. The inner template path is obtained
     * from the parent {@link Reference} and then the childId String is
     * concatenated to it. The child {@link Reference} is created by calling
     * Protocol#replaceTemplatePathMethod with the new id String as a parameter.
     * 
     * @param virtualProtocol
     * @param reference the parent {@link Reference}
     * @return the child {@link Reference}
     * @throws ProtocolMethodException
     */
    @Override
    public Reference execute(VirtualProtocol virtualProtocol, Reference reference) throws ProtocolMethodException
    {
        String path = reference.execute(new GetTemplatePathMethod()).trim();
        StringBuilder pathBuilder = new StringBuilder(path);
        if (!path.endsWith(PATH_SEPARATOR))
        {
            pathBuilder.append(PATH_SEPARATOR);
        }
        pathBuilder.append(childId);

        return virtualProtocol.replaceTemplatePath(reference,
                                                   pathBuilder.toString());
    }
}
