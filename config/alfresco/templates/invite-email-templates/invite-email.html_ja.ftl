<html>
   <#assign inviterPersonRef=args["inviterPersonRef"]/>
   <#assign inviterPerson=companyhome.nodeByReference[inviterPersonRef]/>
   <#assign inviteePersonRef=args["inviteePersonRef"]/>
   <#assign inviteePerson=companyhome.nodeByReference[inviteePersonRef]/>

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
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <img src="${shareUrl}/res/components/site-finder/images/site-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      ${args["siteName"]} サイト参加へのご招待
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>${inviteePerson.properties["cm:firstName"]!""} 様</p>
                                             
                                             <p>${inviterPerson.properties["cm:lastName"]!""} ${inviterPerson.properties["cm:firstName"]!""} 
                                             さんから、${args["siteName"]} サイトに「${args["inviteeSiteRole"]}」の役割で参加するよう招待を受けました。</p>
                                             
                                             <p>次のリンクをクリックすると、${inviterPerson.properties["cm:firstName"]!""} さんの招待を承諾できます。<br />
                                             <br /><a href="${args["acceptLink"]}">${args["acceptLink"]}</a></p>
                                             
                                             <#if args["inviteeGenPassword"]?exists>
                                             <p>あなたのアカウントが作成されました。ログイン情報は次のとおりです。<br />
                                             <br />ユーザー名: ${args["inviteeUserName"]}
                                             <br />パスワード: ${args["inviteeGenPassword"]}
                                             </p>
                                             
                                             <p>パスワードは、初回ログイン時に変更するようにしてください。
                                             パスワードを変更するには、[<b>あなたのプロフィール</b>]に移動して、[<b>パスワードの変更</b>]をクリックします。</p>
                                             </#if>
                                             
                                             <p>${inviterPerson.properties["cm:firstName"]!""} さんの招待を辞退する場合は、次のリンクをクリックしてください。<br />
                                             <br /><a href="${args["rejectLink"]}">${args["rejectLink"]}</a></p>
                                             
                                             <p>ご利用ありがとうございます。<br />
                                             Alfresco ${productName!""}</p>
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
                                 Alfresco ${productName!""}の詳細については、<a href="http://www.alfresco.com">http://www.alfresco.com</a> をご覧ください。
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 10px 30px;">
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
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