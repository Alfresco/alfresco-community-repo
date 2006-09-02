/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.audit.hibernate;

import java.util.Date;

public interface AuditFact
{

    public abstract String getArg1();

    public abstract String getArg2();

    public abstract String getArg3();

    public abstract String getArg4();

    public abstract String getArg5();

    public abstract AuditConfig getAuditConfig();

    public abstract AuditDate getAuditDate();

    public abstract AuditSource getAuditSource();

    public abstract String getClientInetAddress();

    public abstract Date getDate();

    public abstract String getException();

    public abstract boolean isFail();

    public abstract boolean isFiltered();

    public abstract String getHostInetAddress();

    public abstract long getId();

    public abstract String getMessage();

    public abstract String getNodeUUID();

    public abstract String getPath();

    public abstract String getReturnValue();

    public abstract String getSerialisedURL();

    public abstract String getSessionId();

    public abstract String getStoreId();

    public abstract String getStoreProtocol();

    public abstract String getTransactionId();

    public abstract String getUserId();

}