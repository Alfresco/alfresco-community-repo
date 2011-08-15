<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">   
      <meta name="Generator" content="Alfresco Repository">
      <title>${msg("authForm.title")}</title>
      <style type="text/css">
         body {margin:3em;font-family:arial,helvetica,clean,sans-serif;}
         div.header {background:#56A3D9;}
         h1 {color: white;font-size: 1.3em;padding:5px 6px 3px;}
      </style>
   </head>
   <body>
      <img src="http://www.alfresco.com/images/alfresco-logo.png" alt="Alfresco" />
      <div class="header"><h1>${msg("authForm.heading", channel.name)}</h1></div>
		<p>${msg("authForm.directions", channel.name)}</p>
		<form id="loginform" action="#" method="post" accept-charset="UTF-8">
			<div>
				<label id="txt-username" for="username">${msg("authForm.user")}</label>
			</div>
			<div style="padding-top:4px">
				<input id="username" type="text" value="admin" style="width:200px" maxlength="255" name="username">
			</div>
			<div style="padding-top:12px">
				<label id="txt-password" for="password">${msg("authForm.password")}</label>
			</div>
			<div style="padding-top:4px">
				<input id="password" type="password" style="width:200px" maxlength="255" name="password">
			</div>
			<div style="padding-top:16px">
				<input id="btn-login" class="login-button" type="submit" value="${msg("authForm.login")}">
			</div>
		</form>
	</body>
</html>