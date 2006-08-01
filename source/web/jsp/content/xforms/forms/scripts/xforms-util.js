document.getElementsByClassName = function(className, parentElement) {
  var children = (document.body || $(parentElement)).getElementsByTagName('*');
  return $A(children).inject([], function(elements, child) {
    if (Element.hasClassName(child, className))
      elements.push(child);
    return elements;
  });
}
var $A = Array.from = function(iterable) {
  if (iterable.toArray) {
    return iterable.toArray();
  } else {
    var results = [];
    for (var i = 0; i < iterable.length; i++)
      results.push(iterable[i]);
    return results;
  }
}

function _setElementText (element, text) {
    if (document.all) {
        element.innerText = text;
    }
    else {
        if (element.firstChild) {
            element.firstChild.data = text;
        }
        else {
            element.appendChild(document.createTextNode(text));
        }
    }

    return true;
}

function _getElementById (element, id) {
    if (element.getAttribute("id") == id) {
        return element;
    }

    var hit;
    var children = element.childNodes;
    for (var index = 0; index < children.length; index++) {
        if (children[index].nodeType == 1) {
            hit = _getElementById(children[index], id);
            if (hit) {
                return hit;
            }
        }
    }

    return null;
}

function _hasClass (element, clazz) {
    if (!element || !element.className) {
        return false;
    }

    // surround classes with spaces to guarantee non-ambigous lookups
    if ((" " + element.className + " ").indexOf(" " + clazz + " ") == -1) {
        return false;
    }

    return true;
}

function _addClass (element, clazz) {
    if (!element || !element.className) {
        return false;
    }

    // surround classes with spaces to guarantee non-ambigous lookups
    if ((" " + element.className + " ").indexOf(" " + clazz + " ") == -1) {
        element.className = element.className + " " + clazz;
        return true;
    }

    return false;
}

function _removeClass (element, clazz) {
    if (!element || !element.className) {
        return false;
    }

    // surround classes with spaces to guarantee non-ambigous lookups
    if ((" " + element.className + " ").indexOf(" " + clazz + " ") > -1) {
        var classList = (" " + element.className + " ").replace(new RegExp(" " + clazz + " "), " ");
        element.className = classList.slice(1, classList.length - 1);
        return true;
    }

    return false;
}

/**
 * replaces a CSS class within the css attribute of given element. 'current' class will be replaced by 'update'.
 */
function _replaceClass (element, current, update) {
    if (!element || !element.className) {
        return false;
    }

    var oldClassName = element.className;

    // surround all strings with spaces to guarantee non-ambigous lookups
    var classList = " " + oldClassName + " ";
    var classCurrent = " " + current + " ";
    var classUpdate = " " + update + " ";

    if (classList.indexOf(classUpdate) == -1) {
        var newClassName = classList.replace(new RegExp(classCurrent), classUpdate);
        if (newClassName.indexOf(classUpdate) == -1) {
            // ensure the new class name, even if no replacement happened
            newClassName = classList + update + " ";
        }

        // remove leading and trailing spaces and update the element's class name
        newClassName = newClassName.slice(1, newClassName.length - 1);
        element.className = newClassName;

        return true;
    }

    return false;
}

function _debug(message){
    var debugArea = document.getElementById("debugarea");
    if(debugArea){
        debugArea.value = debugArea.value + message + "\n";
    }
    return true;
}

function _clear(){
    var debugAppend = document.getElementById("debugappend");
    if (debugAppend && debugAppend.checked) {
        return false;
    }

    var debugArea = document.getElementById("debugarea");
    if(debugArea){
        debugArea.value = "";
    }

    return true;
}
