(function($, cloudStack) {
  cloudStack.dialog = {
    /**
     * Dialog with form
     */
    createForm: function(args) {
      var $formContainer = $('<div>').addClass('form-container');
      var $message = $('<span>').addClass('message').appendTo($formContainer).html(args.form.desc);
      var $form = $('<form>').appendTo($formContainer);

      $.each(args.form.fields, function(key) {
        var $formItem = $('<div>')
              .addClass('form-item')
              .attr({ rel: key })
              .appendTo($form);
        if (this.hidden) $formItem.hide();

        // Label field
        var $name = $('<div>').addClass('name')
              .appendTo($formItem)
              .append(
                $('<label>').html(this.label + ':')
              );

        // Input area
        var $value = $('<div>').addClass('value')
              .appendTo($formItem);
        var $input, $dependsOn, selectFn, selectArgs;
        var dependsOn = this.dependsOn;

        // Determine field type of input
        if (this.select) {
          selectArgs = {
            response: {
              success: function(args) {
                $(args.data).each(function() {
                  var id = this.id;
                  var description = this.description;

                  if (args.descriptionField)
                    description = this[args.descriptionField];
                  else
                    description = this.name;

                  var $option = $('<option>')
                        .appendTo($input)
                        .val(id)
                        .html(description);
                });

                $input.trigger('change');
              }
            }
          };
          selectFn = this.select;
          $input = $('<select>').attr({ name: key }).appendTo($value);

          // Pass form item to provider for additional manipulation
          $.extend(selectArgs, { $select: $input });

          // Call select to map data
          if (this.dependsOn) {
            $dependsOn = $form.find('select').filter(function() {
              return $(this).attr('name') === dependsOn;
            });

            $dependsOn.bind('change', function(event) {
              var $target = $(this);
              var dependsOnArgs = {};
              $input.find('option').remove();
              $input.trigger('change');

              if (!$target.children().size()) return true;
              
              dependsOnArgs[dependsOn] = $target.val();
              selectFn($.extend(selectArgs, dependsOnArgs));

              return true;
            });
            
          } else {
            selectFn(selectArgs);
          }
        } else if (this.isBoolean) {
          $input = $('<input>').attr({ name: key, type: 'checkbox' }).appendTo($value);
        } else {
          $input = $('<input>').attr({ name: key, type: 'text' }).appendTo($value);
        }

        $input.data('validation-rules', this.validation);
        $('<label>').addClass('error').appendTo($value).html('*required');
      });

      $form.find('select').trigger('change');

      var getFormValues = function() {
        var formValues = {};
        $.each(args.form.fields, function(key) {
        });
      };

      // Setup form validation
      $formContainer.find('form').validate();
      $formContainer.find('input, select').each(function() {
        if ($(this).data('validation-rules')) {
          $(this).rules('add', $(this).data('validation-rules'));
        }
      });

      return $formContainer.dialog({
        dialogClass: 'create-form',
        width: 400,
        title: args.form.title,
        buttons: [
          {
            text: 'Create',
            'class': 'ok',
            click: function() {
              var $form = $formContainer.find('form');
              var data = cloudStack.serializeForm($form);

              if (!$formContainer.find('form').valid()) {
                // Ignore hidden field validation
                if ($formContainer.find('input.error:visible').size()) {
                  return false;
                }
              }

              $('div.overlay').remove();
              args.after({ data: data });
              $(this).dialog('destroy');

              return true;
            }
          },
          {
            text: 'Cancel',
            'class': 'cancel',
            click: function() {
              $('div.overlay').remove();
              $(this).dialog('destroy');
            }
          }
        ]
      }).closest('.ui-dialog').overlay();
    },

    /**
     * Confirmation dialog
     */
    confirm: function(args) {
      return $(
        $('<span>').addClass('message').html(
          args.message
        )
      ).dialog({
        title: 'Confirm',
        dialogClass: 'confirm',
        zIndex: 5000,
        buttons: [
          {
            text: 'Cancel',
            'class': 'cancel',
            click: function() {
              $(this).dialog('destroy');
              $('div.overlay').remove();
            }
          },
          {
            text: 'Yes',
            'class': 'ok',
            click: function() {
              args.action();
              $(this).dialog('destroy');
              $('div.overlay').remove();
            }
          }
        ]
      }).closest('.ui-dialog').overlay();
    },

    /**
     * Notice dialog
     */
    notice: function(args) {
      return $(
        $('<span>').addClass('message').html(
          args.message
        )
      ).dialog({
        title: 'Status',
        dialogClass: 'notice',
        zIndex: 5000,
        buttons: [
          {
            text: 'Close',
            'class': 'close',
            click: function() {
              $(this).dialog('destroy');
            }
          }
        ]
      });
    }
  };
})(jQuery, cloudStack);
