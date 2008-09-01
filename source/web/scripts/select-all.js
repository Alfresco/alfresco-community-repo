//
// Select All JS
//
// Java script utilities to support "select all" checkboxes.
// which consist of a parent checkbox and a collection of one or more 
// child checkboxes.  
// Clicking the parent checkbox sets the children.
// Clicking the children may affect the parent.
//
// select_all is called to set or reset all child checkboxes.
//
// select_child is called when a child is selected and may set or 
// reset the parent depending upon whether all its siblings are set.
//
// prepare_select_all initialises this package
//
var formblock;
var forminputs;
   
    function prepare_select_all() {
  		formblock= document.getElementById('dialog');
  		forminputs = formblock.getElementsByTagName('input');
    }
  
  	/**
  	 * call after a child to set the value of the parent.
  	 * @param parentId the id of the parent
  	 * @param childname the name of the child checkbox
  	 */
    function select_one(parentId, childname) {
  	  	var isSelected = true;
  	  	for (i = 0; i < forminputs.length; i++) {  
  	  	  	var regex = new RegExp(childname, "i");
  	  	  	var x = forminputs[i];
  			if (regex.test(x.getAttribute('name'))) {
  			   if(x.getAttribute('type') == 'checkbox') {
  			        if(x.checked == false) { 
  			        	isSelected = false;
  			        	break;
  			        }
  			   }
 			}
  	    }
  	    var parent = document.getElementById(parentId);
  	    parent.checked = isSelected;
    }	    
  	
  
  	/**
  	 * Called after the parent is clicked to set the value of the children.
  	 * @param childname  the name of the child checkboxes.
  	 * @param value the value to set.
  	 * @return
  	 */
    function select_all(childname, value) {
  		for (i = 0; i < forminputs.length; i++) {
  			if(childname == forminputs[i].getAttribute('name')) {
   				if (value == '1') {
  					forminputs[i].checked = true;
  				} else {
  					forminputs[i].checked = false;
  				}
  			}
  		}
    }
  
  	if (window.addEventListener) {
  		window.addEventListener("load", prepare_select_all, false);
  	} else if (window.attachEvent) {
  		window.attachEvent("onload", prepare_select_all)
  	} else if (document.getElementById) {
  		window.onload = prepare_select_all;
  	}

 