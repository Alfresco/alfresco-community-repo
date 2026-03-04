/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.events;

import java.io.Serial;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

class MessagingException extends RuntimeException
{
    @Serial
    private static final long serialVersionUID = 8192266871339806688L;
    private static final AtomicInteger ERROR_COUNTER = new AtomicInteger();

    public MessagingException(String message, Throwable cause)
    {
        super(buildErrorLogNumber(message), cause);
    }

    private static String buildErrorLogNumber(String message)
    {
        final LocalDate today = LocalDate.now();
        message = message == null ? "" : message;

        return "%02d%02d%04d %s".formatted(today.getMonthValue(), today.getDayOfMonth(), ERROR_COUNTER.getAndIncrement(), message);
    }
}
