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

<#assign doc_actions="${url.serviceContext}/office/docActions">
<#assign path=args.p!"">
<#assign extn="doc" extn1="odt" extn2="sxw">
<#assign extnx=extn+"x">
<#assign nav=args.n!"">
<#-- resolve the path (from Company Home) into a node -->
<#if companyhome.childByNamePath[path]??>
   <#assign d=companyhome.childByNamePath[path]>
<#else>
   <#assign d=companyhome>
</#if>
<documentDetails>
<#if d.isDocument>
               <img>${url.context}${d.icon32}</img>
   <#if d.isLocked >
                  <locked>YES</locked>
   </#if>
                <name>${d.name}</name>
   <#if d.properties.title??>
                  <title>${d.properties.title}</title>
   <#else>
                  <title>""</title>
   </#if>
   <#if d.properties.description??>
                  <description>${d.properties.description}</description>
   <#else>
                  <description>""</description>
   </#if>
                  <creator>${d.properties.creator}</creator>
                  <created>${d.properties.created?datetime}</created>
                  <modifier>${d.properties.modifier}</modifier>
                  <modified>${d.properties.modified?datetime}</modified>
                  <size>${d.size / 1024} Kb</size>
                  
   <#if d.hasAspect("cm:generalclassifiable")>
      <#list d.properties.categories as category>
                       <categories> ${companyhome.nodeByReference[category].name};</categories>
      </#list>
   <#else>
                        <categories>None.</categories>
   </#if>
<#else>
               <nonAlfrescoDoc>The current document is not managed by Alfresco.</nonAlfrescoDoc>
</#if>
<versionHistory>
<#if d.isDocument >
   <#if d.hasAspect("cm:versionable")>
      <#assign versionRow=0>
      <#list d.versionHistory as record>
         <#assign versionRow=versionRow+1>
	<version>
            <title>${record.versionLabel}</title> 
		<url>${url.context}${record.url}?ticket=${session.ticket}</url>
            <author>${record.creator}</author>
            <date>${record.createdDate?datetime}</date>
         <#if record.description??>
            <notes>${record.description}</notes>
         </#if>
         <#-- Only Word supports document compare -->
         <#if extn = "doc">
            <compareDocument>${record.url}</compareDocument>
	</version>
         </#if>
      </#list>
   <#else>
<notVersioned>The current document is not versioned.</notVersioned>
<makeVersionURL>${doc_actions}?a=makeversion&amp;n=${d.id}</makeVersionURL>
   </#if>
<#else>
            <nonAlfresco>The current document is not managed by Alfresco.</nonAlfresco>
</#if>
</versionHistory>
<documentActions>
<#if d.isDocument>
   <#if d.isLocked >
   <#elseif d.hasAspect("cm:workingcopy")>
	<checkin>${doc_actions}?a=checkin&amp;n=${d.id}</checkin>
   <#else>
	<checkout>${doc_actions}?a=checkout&amp;n=${d.id}</checkout>
   </#if>
	<workflow>${url.serviceContext}/office/docActions?a=workflow&amp;n=${d.id}</workflow>
    <#if d.name?ends_with(extn) || d.name?ends_with(extnx) || d.name?ends_with(extn1) || d.name?ends_with(extn2)>
         <makePDF>${doc_actions}?a=makepdf&amp;n=${d.id}</makePDF>
    </#if>
	<openFullDetails>${url.context}/navigate/showDocDetails/workspace/SpacesStore/${d.id}?ticket=${session.ticket}</openFullDetails>
<#else>
	     <save>Save to Alfresco</save>
</#if>
</documentActions>
</documentDetails>
