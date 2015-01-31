<#macro renderFormContainer formId>
   <div id="${formId}-container" class="form-container">
      <#if form.showCaption?? && form.showCaption>
         <div id="${formId}-caption" class="caption"><span class="mandatory-indicator">*</span>${msg("form.required.fields")}</div>
      </#if>
      
      <#if form.mode != "view">
         <form id="${formId}" method="${form.method}" accept-charset="utf-8" enctype="${form.enctype}" action="${form.submissionUrl?html}">
      </#if>
      
      <#if form.mode == "create" && form.destination?? && form.destination?length &gt; 0>
         <input id="${formId}-destination" name="alf_destination" type="hidden" value="${form.destination?html}" />
      </#if>
      
      <#if form.mode != "view" && form.redirect?? && form.redirect?length &gt; 0>
         <input id="${formId}-redirect" name="alf_redirect" type="hidden" value="${form.redirect?html}" />
      </#if>
      
      <div id="${formId}-fields" class="form-fields">
         <#nested>
      </div>
      
      <#if form.mode != "view">
         <@renderFormButtons formId=formId />
         </form>
      </#if>
   </div>
</#macro>

<#macro renderFormButtons formId>         
   <div id="${formId}-buttons" class="form-buttons">
      <#if form.showSubmitButton?? && form.showSubmitButton>
         <input id="${formId}-submit" type="submit" value="${msg("form.button.submit.label")}" />&nbsp;
      </#if>
      <#if form.showResetButton?? && form.showResetButton>
         <input id="${formId}-reset" type="reset" value="${msg("form.button.reset.label")}" />&nbsp;
      </#if>
      <#if form.showCancelButton?? && form.showCancelButton>
         <input id="${formId}-cancel" type="button" value="${msg("form.button.cancel.label")}" />
      </#if>
   </div>
</#macro>   

<#macro renderField field>
   <#if field.control?? && field.control.template??>
      <#assign fieldHtmlId=args.htmlid?html + "_" + field.id >
      <#include "${field.control.template}" />
   </#if>
</#macro>

<#macro renderSet set>
   <div class="set">
   <#if set.appearance??>
      <#if set.appearance == "fieldset">
         <fieldset><legend>${set.label}</legend>
      <#elseif set.appearance == "bordered-panel">
         <div class="set-bordered-panel">
            <div class="set-bordered-panel-heading">${set.label}</div>
            <div class="set-bordered-panel-body">
      <#elseif set.appearance == "panel">
         <div class="set-panel">
            <div class="set-panel-heading">${set.label}</div>
            <div class="set-panel-body">
      <#elseif set.appearance == "title">
         <div class="set-title">${set.label}</div>
      <#elseif set.appearance == "whitespace">
         <div class="set-whitespace"></div>
      </#if>
   </#if>
   
   <#if set.template??>
      <#include "${set.template}" />
   <#else>
      <#list set.children as item>
         <#if item.kind == "set">
            <@renderSet set=item />
         <#else>
            <@renderField field=form.fields[item.id] />
         </#if>
      </#list>
   </#if>
   
   <#if set.appearance??>
      <#if set.appearance == "fieldset">
         </fieldset>
      <#elseif set.appearance == "panel" || set.appearance == "bordered-panel">
            </div>
         </div>
      </#if>
   </#if>
   </div>
</#macro>
