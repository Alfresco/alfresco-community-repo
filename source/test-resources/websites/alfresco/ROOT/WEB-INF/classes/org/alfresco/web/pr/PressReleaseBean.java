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
package org.alfresco.web.pr;

import java.util.Date;

public class PressReleaseBean
{
    private final String title;
    private final String theAbstract;
    private final Date launchDate;
    private final String href;

    public PressReleaseBean(final String title, 
			    final String theAbstract, 
			    final Date launchDate, 
			    final String href)
    {
        this.title = title;
        this.theAbstract = theAbstract;
        this.launchDate = launchDate;
        this.href = href;
    }

    public String getTitle() 
    { 
	return this.title; 
    }

    public String getAbstract()
    {
	return this.theAbstract;
    }

    public Date getLaunchDate()
    {
	return this.launchDate;
    }

    public String getHref()
    {
	return this.href;
    }
}