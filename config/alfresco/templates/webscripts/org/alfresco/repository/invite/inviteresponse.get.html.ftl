<#if response == "accept">
   <p>Your acceptance to join site ${siteShortName} has been processed</p>
<#elseif response == "reject">
   <p>Your rejection to join site ${siteShortName} has been processed</p>
<#else>
   <p>Error: unknown invite response ${response}</p>
</#if> 
