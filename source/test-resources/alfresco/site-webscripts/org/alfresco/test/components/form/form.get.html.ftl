		<#import "form.lib.ftl" as formLib />
		
         <#if error?exists>
            <div class="error">${error}</div>
         <#elseif form?exists>
            <#assign formId=args.htmlid?js_string?html + "-form">
            <#assign formUI><#if args.formUI??>${args.formUI}<#else>true</#if></#assign>
            
               <@formLib.renderFormContainer formId=formId>
                  <#list form.structure as item>
                     <#if item.kind == "set">
                        <#if item.children?size &gt; 0>
                           <@formLib.renderSet set=item />
                        </#if>
                     <#else>
                        <@formLib.renderField field=form.fields[item.id] />
                     </#if>
                  </#list>
               </@>

         <#else>
            <div class="form-container">Form doesn't exist</div>
         </#if>