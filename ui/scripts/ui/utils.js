(function($, cloudStack) {
  // General utils
  cloudStack.serializeForm = function($form, options) {
    if (!options) options = {};
    
    var data = {};

    $($form.serializeArray()).each(function() {
      var dataItem = data[this.name];
      var value = this.value;

      if (options.escapeSlashes) {
        value = value.replace(/\//g, '__forwardSlash__');
      }

      if (!dataItem) {
        data[this.name] = value;
      } else if (dataItem && !$(dataItem).size()) {
        data[this.name] = [dataItem, value];
      } else {
        dataItem.push(value);
      }
    });

    return data;
  };

  // Even/odd row handling
  cloudStack.evenOdd = function($container, itemSelector, args) {
    var even = false;

    $container.find(itemSelector).each(function() {
      var $elem = $(this);
      
      if (even) {
        even = false;
        args.odd($elem);
      } else {
        even = true;
        args.even($elem);
      }
    });
  };

  /**
   * Localization -- shortcut _l
   *
   * Takes string and runs through localization function -- if no code
   * exists or function isn't present, return string as-is
   */
  cloudStack.localize = window._l = function(str) {
    var localized = cloudStack.localizationFn ?
          cloudStack.localizationFn(str) : null;

    return localized ? localized : str;
  };

  /**
   * Sanitize user input (HTML Encoding) -- shortcut _s
   * 
   * Strip unwanted characters from user-based input
   */
  cloudStack.sanitize = window._s = function(value) {
	  if(typeof(value) == "number") {		  
		  //alert("number does not need to be sanitized. Only string needs to be sanitized.");
			return value;
		}
		else if(typeof(value) == "boolean") {		  
		  //alert("boolean does not need to be sanitized. Only string needs to be sanitized.");
			return value;
		}
		else if(typeof(value) == "object") {		  
		  //alert("object cant not be sanitized. Only string can be sanitized.");
			return value;
		}
	  else if(typeof(value) == null || typeof(value) == "undefined") {		 
			return '';
		}
           
    var sanitized = value
          .replace(/&/g, "&amp;")
          .replace(/</g, "&lt;")
          .replace(/>/g, "&gt;");

    return sanitized;
  };
	
	/**
   * Reverse sanitization (HTML Decoding)   
   */
  cloudStack.sanitizeReverse = function(value) {	  
    var reversedValue = value
          .replace(/&amp;/g, "&")
          .replace(/&lt;/g, "<")
          .replace(/&gt;/g, ">");

    return reversedValue;
  };
	
})(jQuery, cloudStack);
