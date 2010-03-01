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
package org.alfresco.repo.content.transform;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * 
 * Wraps an exception that could be thrown in any transformer to
 * propagate it up to <code>NodeInfoBean.sendNodeInfo</code> method.
 * <code>NodeInfoBean</code> can handle this exception to display it in NodeInfo frame
 * to avoid error message box with "Exception in Transaction" message.
 * 
 * See {@link org.alfresco.repo.content.transform.PoiHssfContentTransformer} for pattern.
 * 
 * @author Arseny Kovalchuk
 * 
 */
public class TransformerInfoException extends AlfrescoRuntimeException
{
    private static final long serialVersionUID = -4343331677825559617L;

    public TransformerInfoException(String msg)
    {
        super(msg);
    }

    public TransformerInfoException(String msg, Throwable err)
    {
        super(msg, err);
    } 
}
