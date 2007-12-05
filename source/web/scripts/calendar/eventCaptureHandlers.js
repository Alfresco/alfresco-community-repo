
function showSavingIndicator(mode)
{
	var obj = null;
	switch(mode)
	{
		case 'd':
		{
			obj = document.getElementById("spnCurrentDisplayDay");
			break;
		}
		case 'w':
		{
			obj = document.getElementById("spnCurrentDisplayWeek");
			break;
		}
		case 'c':
		{
			obj = document.getElementById("spnEventCapture");
			break;
		}
	}
	if (obj == null)		return false;
		
	var newImg = document.createElement('img');
	newImg.src = "/alfresco/yui/img/indicator.gif";
	var txt = document.createTextNode(' Saving...');
	obj.appendChild(newImg);
	obj.appendChild(txt);
}

function hideSavingIndicator(mode)
{
	var obj = null;
	switch(mode)
	{
		case 'd':
		{
			obj = document.getElementById("spnCurrentDisplayDay");
			break;
		}
		case 'w':
		{
			obj = document.getElementById("spnCurrentDisplayWeek");
			break;
		}
		case 'c':
		{
			obj = document.getElementById("spnEventCapture");
			break;
		}
	}
	if (obj == null)		return false;

	while (obj.hasChildNodes())
		obj.removeChild(obj.firstChild);
}


function removeTextBoxNode(e)
{
	var txtNode;
	if (window.event)
		txtNode = window.event.srcElement;
	else
		txtNode = e.target;
		
	var owner = txtNode.parentNode;
	owner.removeChild(txtNode);
}
