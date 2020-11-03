<html>

<head>
   <style type="text/css">
      body {
         -webkit-font-smoothing: antialiased;
         -webkit-text-size-adjust: none;
         width: 100% ! important;
         height: 100% !important;
         color: #727174;
         font-weight: 400;
         font-size: 18px;
         margin: 0;
      }
   </style>
</head>

<body>
<div bgcolor="transparent">
   <center>
      <table align="center" bgcolor="transparent" border="0" cellpadding="0" cellspacing="0" height="100%"
             width="100%">
         <tbody>
         <tr>
            <td align="center" valign="top" style="padding-bottom:60px">
               <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%" style="padding: 25px;">
                  <tbody>
                  <tr>
                     <td align="center" valign="top">
                        <table align="center" bgcolor="#FFFFFF" border="0" cellpadding="0"
                               cellspacing="0"
                               style="background-color:#ffffff;max-width: 600px;max-height: 600px;"
                               width="100%">
                           <tbody>
                           <tr>
                              <td align="" valign="top" bgcolor="#FFFFFF"
                                  style="padding: 40px;">
                                 <img style="border:none; display:block; border-collapse:collapse; outline:none; text-decoration:none;"
                                      src="${template_assets_url}/logo/workspace.png" border="0"
                                      alt="Alfresco" width="180" height="36">
                              </td>
                           </tr>
                           <tr>
                              <td style="padding-right:40px;padding-bottom:30px;padding-left:40px">
                                 <table border="0" cellspacing="0" cellpadding="0">
                                    <tbody>
                                    <tr>
                                       <td>
                                          <h3 style="letter-spacing: 0; color: #707070;font-size: 20px; margin: 0">
                                             Join Request
                                          </h3>
                                       </td>
                                    </tr>
                                    </tbody>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding-right:40px;padding-bottom:10px;padding-left:40px">
                                 <table border="0" cellspacing="0" cellpadding="0">
                                    <tbody>
                                    <tr>
                                       <td>
                                          <p style="letter-spacing: 0px; color: #707070; display: block; font-weight: bold; font-size: 1.17em; margin: 0; ">
                                             ${inviteeName}
                                          </p>

                                          <p style="letter-spacing: 0px; color: #707070;">
                                             ${message("templates.workspace.invite-email-moderated.html.body")}
                                          </p>

                                          <p style="letter-spacing: 0px; color: #707070; display: block; font-weight: bold; font-size: 1.17em; ">
                                             ${siteName}
                                          </p>

                                          <p style="letter-spacing: 0px; color: #707070; display: block;">
                                          <table bgcolor="#FFFFFF" border="0"
                                                 cellpadding="0" cellspacing="0"
                                                 style="background: #2A7DE1 0 0 no-repeat padding-box;border-radius: 5px;color: #FFFFFF;">
                                             <tbody>
                                             <tr>
                                                <td>
                                                   <a href="${workspacePendingInvitesLink}"
                                                      style="color:#ffffff;display:inline-block;border-radius: 5px;font-size:16px;font-family:Helvetica,Arial,Verdana,sans-serif;font-weight:400;padding: 10px;text-decoration:none;"
                                                      target="_blank"
                                                      data-saferedirecturl="${workspacePendingInvitesLink}">
                                                      ${message("templates.workspace.invite-email-moderated.html.action")}
                                                   </a>
                                                </td>
                                             </tr>
                                             </tbody>
                                          </table>
                                          </p>
                                       </td>
                                    </tr>
                                    </tbody>
                                 </table>
                              </td>
                           </tr>

                           <tr>
                              <td align="center" valign="top" style="border-top:2px solid #efeeea;color:#6a655f;font-family:Helvetica,Arial,Verdana,sans-serif;font-size:12px;font-weight:400;line-height:24px;padding-top:40px;padding-bottom:40px;text-align:center">
                                 <p style="color:#6a655f;font-family:Helvetica,Arial,Verdana,sans-serif;font-size:12px;font-weight:400;line-height:24px;padding:0 20px;margin:0;text-align:center">
                                     © 2020 Alfresco Software, Inc. All rights reserved.
                                 </p>
                              </td>
                           </tr>
                           </tbody>
                        </table>
                     </td>
                  </tr>
                  </tbody>
               </table>
            </td>
         </tr>
         </tbody>
      </table>
   </center>
</div>
</body>
</html>