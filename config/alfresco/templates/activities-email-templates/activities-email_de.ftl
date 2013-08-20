<html>
   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }
      
      a, a:visited
      {
         color: #0072cf;
      }
      
      .activity a
      {
         text-decoration: none;
      }
      
      .activity a:hover
      {
         text-decoration: underline;
      }
      --></style>
   </head>
   
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 20px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <div style="font-size: 22px; padding-bottom: 4px;">
                                             Letzte Aktivitäten
                                          </div>
                                          <div style="font-size: 13px;">
                                             ${date?datetime?string.full}
                                          </div>
                                          <div style="font-size: 14px; margin: 18px 0px 24px 0px; padding-top: 18px; border-top: 1px solid #aaaaaa;">
                                             <#if activities?exists && activities?size &gt; 0>
                                             <#list activities as activity>
                                                <#if activity.siteNetwork??>
                                                <#assign userLink="<a href=\"${shareUrl}/page/user/${activity.postUserId?html}/profile\">${activity.activitySummary.firstName!\"\"} ${activity.activitySummary.lastName!\"\"}</a>">
                                                <#assign secondUserLink="">
                                                <#assign itemLink="<a href=\"${shareUrl}/page/site/${activity.siteNetwork?html}/${activity.activitySummary.page!\"\"}\">${activity.activitySummary.title!\"\"}</a>">
                                                <#assign siteLink="<a href=\"${shareUrl}/page/site/${activity.siteNetwork?html}/dashboard\">${siteTitles[activity.siteNetwork]!activity.siteNetwork?html}</a>">
                                                
                                                <#assign suppressSite=false>
                                                
                                                <#switch activity.activityType>
                                                   <#case "org.alfresco.site.user-joined">
                                                   <#case "org.alfresco.site.user-left">
                                                      <#assign suppressSite=true>
                                                   <#case "org.alfresco.site.user-role-changed">
                                                      <#assign custom0=message("role."+activity.activitySummary.role)!"">
                                                      <#assign userLink="<a href=\"${shareUrl}/page/user/${activity.activitySummary.memberUserName?html}/profile\">${activity.activitySummary.memberFirstName!\"\"} ${activity.activitySummary.memberLastName!\"\"}</a>">
                                                      <#break>
                                                   <#case "org.alfresco.site.group-added">
                                                   <#case "org.alfresco.site.group-removed">
                                                      <#assign suppressSite=true>
                                                   <#case "org.alfresco.site.group-role-changed">
                                                      <#assign custom0=message("role."+activity.activitySummary.role)!"">
                                                      <#assign userLink=activity.activitySummary.groupName?replace("GROUP_", "")>
                                                      <#break>
                                                   <#case "org.alfresco.subscriptions.followed">
                                                      <#assign userLink="<a href=\"${shareUrl}/page/user/${activity.activitySummary.followerUserName?html}/profile\">${activity.activitySummary.followerFirstName!\"\"} ${activity.activitySummary.followerLastName!\"\"}</a>">
                                                      <#assign secondUserLink="<a href=\"${shareUrl}/page/user/${activity.activitySummary.userUserName?html}/profile\">${activity.activitySummary.userFirstName!\"\"} ${activity.activitySummary.userLastName!\"\"}</a>">                                                   
                                                      <#assign suppressSite=true>
                                                      <#break>
                                                   <#case "org.alfresco.subscriptions.subscribed">
                                                      <#assign userLink="<a href=\"${shareUrl}/page/user/${activity.activitySummary.subscriberUserName?html}/profile\">${activity.activitySummary.subscriberFirstName!\"\"} ${activity.activitySummary.subscriberLastName!\"\"}</a>">
                                                      <#assign custom0=(activity.activitySummary.node!"")?html>
                                                      <#assign suppressSite=true>
                                                      <#break>                                                   
                                                   <#case "org.alfresco.profile.status-changed">
                                                      <#assign custom0=(activity.activitySummary.status!"")?html>
                                                      <#assign suppressSite=true>
                                                      <#break>
                                                   <#default>
                                                </#switch>
                                                
                                                <#assign detail=message(activity.activityType?html, itemLink, userLink, custom0, activity.activitySummary.custom1!"", siteLink, secondUserLink)!"">
                                                
                                                <div class="activity">
                                                   <#if suppressSite>${detail}<#else>${message("in.site", detail, siteLink)!""}</#if>
                                                </div>
                                                <div style="font-size: 11px; padding: 4px 0px 12px 0px;">
                                                   ${activity.postDate?datetime?string.medium}
                                                </div>
                                                </#if>
                                             </#list>
                                             </#if>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-top: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 0px 30px; font-size: 13px;">
                                 Sie können Benachrichtigungen deaktivieren, indem Sie auf diesen Link klicken:<br />
                                 <br /><a href="${shareUrl}/page/user/${personProps["cm:userName"]}/user-notifications">${shareUrl}/page/user/${personProps["cm:userName"]}/user-notifications</a>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 10px 30px;">
                                 <img src="${shareUrl}/res/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
                              </td>
                           </tr>
                        </table>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>
