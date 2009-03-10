<?xml version="1.0" encoding="UTF-8"?>
<!--****************************************************************************
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.                    *
 *                                                                             *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.                  *
 *                                                                             * 
 * The contents of this file are subject to the terms of either the GNU General* 
 * Public License Version 2 only ("GPL") or the Common Development and         *  
 * Distribution License("CDDL") (collectively, the "License").                 * 
 * You may not use this file except in  compliance with the License.           *
 * You can obtain a copy of the License at AlfrescoAddon/licenses/license.txt. * 
 * See the License for the specific language governing permissions and         *
 * limitations under the License.  When distributing the software, include this* 
 * License Header Notice in each file and include the License file at          *
 * AlfrescoAddon/licenses/license.txt. If applicable, add the following below  *
 * the License Header, with the fields enclosed by brackets [] replaced by your* 
 * own identifying information:                                                * 
 * "Portions Copyrighted [year] [name of copyright owner]"                     * 
 *                                                                             * 
 * Contributor(s):                                                             * 
 * If you wish your version of this file to be governed by only the CDDL or    * 
 * only the GPL Version 2, indicate your decision by adding "[Contributor]     * 
 * elects to include this software in this distribution under the              *
 * [CDDL or GPL Version 2] license." If you don't indicate a single choice of  * 
 * license, a recipient has the option to distribute your version of this file *
 * under either the CDDL, the GPL Version 2 or  to extend the choice of        *
 * license to its licensees as provided above.  However, if you add            *
 * GPL Version 2 code and therefore, elected the GPL Version 2 license, then   * 
 * the option applies only if the new code is made subject to such option by   * 
 * the copyright holder.                                                       *
 ****************************************************************************-->

<#if args.e??><#assign extn=args.e><#else><#assign extn="doc"></#if>
<#if args.t??>
   <#assign taskid = args.t>
   <#if taskid != "">
      <#assign task = workflow.getTaskById(taskid)>
   </#if>
</#if>
<taskDetails>
<#if task??>
            <description>${task.description?html}</description>
                  <status>${task.properties["bpm:status"]}</status>
                  <priority>${task.properties["bpm:priority"]}</priority>
               <startDate>${task.startDate?date}</startDate>
               <type>${task.type?html}</type>
              <complete>${task.properties["bpm:percentComplete"]}%</complete>
   <label>${task.name?html}:</label>
   <#list task.packageResources as res>
      
               <url>${url.context}${res.url}?ticket=${session.ticket}</url>
		<resName>${res.name}</resName>
   </#list>
   <#list task.transitions as wt>
         <taskAction>${url.context}/command/task/end/${task.id}<#if wt.id??>/${wt.id}</#if></taskAction>
   </#list>
      <taskManage>${url.context}/command/ui/managetask?id=${task.id}&amp;type=${task.qnameType}&amp;container=plain&amp;ticket=${session.ticket}</taskManage>
</#if>
</taskDetails>
