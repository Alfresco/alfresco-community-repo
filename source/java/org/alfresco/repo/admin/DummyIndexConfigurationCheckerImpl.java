/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.admin;

import java.util.Collections;
import java.util.List;

import org.alfresco.service.cmr.repository.StoreRef;

/**
 * @author Andy
 *
 */
public class DummyIndexConfigurationCheckerImpl implements IndexConfigurationChecker
{

    /* (non-Javadoc)
     * @see org.alfresco.repo.admin.IndexConfigurationChecker#checkIndexConfiguration()
     */
    @Override
    public List<StoreRef> checkIndexConfiguration()
    {
        return Collections.<StoreRef>emptyList();
    }

}
