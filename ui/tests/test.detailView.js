(function($) {
  module('Detail view', {
    setup: function() {
      cloudStack.dialog.__confirm = cloudStack.dialog.confirm;
      cloudStack.ui.notifications.__add = cloudStack.ui.notifications.add;
      $.fn.__cloudBrowser = $.fn.cloudBrowser;
      $.fn.__listView = $.fn.listView;
    },

    teardown: function() {
      cloudStack.dialog.confirm = cloudStack.dialog.__confirm;
      cloudStack.ui.notifications.add = cloudStack.ui.notifications.__add;
      $.fn.cloudBrowser = $.fn.__cloudBrowser;
      $.fn.listView = $.fn.__listView;
    }
  });

  test('Basic', function() {
    var detailView = {
      tabs: {
        tabA: {
          title: 'tabA',
          fields: [{}],
          dataProvider: function() {}
        },
        tabB: {
          title: 'tabB',
          fields: [{}],
          dataProvider: function() {}
        }
      }
    };

    var $detailView = $('<div>');

    ok($detailView.detailView(detailView), 'Create detail view');
    equal($detailView.find('.ui-tabs-nav li').size(), 2, 'Detail view has correct tab count');
    equal($detailView.find('.ui-tabs-nav li:first a').html(), 'tabA', 'First tab has correct title');
    equal($detailView.find('.ui-tabs-nav li:last a').html(), 'tabB', 'Last tab has correct title');
  });

  test('Data provider', function() {
    var detailView = {
      tabs: {
        tabA: {
          title: 'tabA',
          fields: [{
            fieldA: { label: 'fieldA' },
            fieldB: { label: 'fieldB' }
          }],
          dataProvider: function(args) {
            start();
            ok(args.response.success({
              data: {
                fieldA: 'dataProviderFieldA',
                fieldB: 'dataProviderFieldB'
              }
            }), 'Call success');
            equal($detailView.find('tr').size(), 2, 'Correct fields rendered');
            equal($detailView.find('tr:first td:first').html(), 'fieldA', 'First field has correct label');
            equal($detailView.find('tr:first td:last').html(), 'dataProviderFieldA', 'First field has correct content');
            equal($detailView.find('tr:last td:first').html(), 'fieldB', 'Last field has correct label');
            equal($detailView.find('tr:last td:last').html(), 'dataProviderFieldB', 'Last field has correct content');
          }
        }
      }
    };
    var $detailView = $('<div>');

    stop();

    // Test first tab
    $detailView = $detailView.detailView(detailView);

    // Test last tab
    $detailView.find('.ui-tabs-nav li:last').click();
  });

  test('Data provider, multiple tabs', function() {
    var detailView = {
      tabs: {
        tabA: {
          title: 'tabA',
          fields: [{
            fieldA: { label: 'fieldA' },
            fieldB: { label: 'fieldB' }
          }],
          dataProvider: function(args) {
            start();
            ok(args.response.success({
              data: {
                fieldA: 'dataProviderFieldA',
                fieldB: 'dataProviderFieldB'
              }
            }), 'Call success');
            equal($detailView.find('tr').size(), 2, 'Correct fields rendered');
            equal($detailView.find('tr:first td:first').html(), 'fieldA', 'First field has correct label');
            equal($detailView.find('tr:first td:last').html(), 'dataProviderFieldA', 'First field has correct content');
            equal($detailView.find('tr:last td:first').html(), 'fieldB', 'Last field has correct label');
            equal($detailView.find('tr:last td:last').html(), 'dataProviderFieldB', 'Last field has correct content');
          }
        },

        tabB: {
          title: 'tabB',
          fields: [{
            fieldC: { label: 'fieldC' },
            fieldD: { label: 'fieldD' },
            fieldC: { label: 'fieldE' },
            fieldD: { label: 'fieldF' }
          }],
          dataProvider: function(args) {
            start();
            ok(args.response.success({
              data: {
                fieldC: 'dataProviderFieldC',
                fieldD: 'dataProviderFieldD',
                fieldE: 'dataProviderFieldE',
                fieldF: 'dataProviderFieldF'
              }
            }), 'Call success');
            equal($detailView.find('tr').size(), 4, 'Correct fields rendered');
            equal($detailView.find('tr:first td:first').html(), 'fieldC', 'First field has correct label');
            equal($detailView.find('tr:first td:last').html(), 'dataProviderFieldC', 'First field has correct content');
            equal($detailView.find('tr:last td:first').html(), 'fieldF', 'Last field has correct label');
            equal($detailView.find('tr:last td:last').html(), 'dataProviderFieldF', 'Last field has correct content');
          }
        }
      }
    };
    var $detailView = $('<div>');

    stop();

    // Test first tab
    $detailView = $detailView.detailView(detailView);

    // Test last tab
    $detailView.find('.ui-tabs-nav li:last').click();
  });

  test('Field pre-filter', function() {
    var detailView = {
      tabs: {
        tabA: {
          title: 'tabA',
          fields: {
            fieldA: { label: 'fieldA' },
            fieldB: { label: 'fieldB' },
            fieldC: { label: 'fieldC' },
            fieldD: { label: 'fieldD' }
          },
          preFilter: function(args) {
            return ['fieldB', 'fieldC'];
          },
          dataProvider: function (args) {
            args.response.success({
              data: {
                fieldA: 'fieldAContent',
                fieldB: 'fieldBContent',
                fieldC: 'fieldCContent',
                fieldD: 'fieldDContent'
              }
            });

            start();
            equal($detailView.find('tr').size(), 2, 'Correct fields rendered');
            equal($detailView.find('tr:first td:first').html(), 'fieldA', 'First field has correct label');
            equal($detailView.find('tr:first td:last').html(), 'fieldAContent', 'First field has correct content');
            equal($detailView.find('tr:last td:first').html(), 'fieldD', 'Last field has correct label');
            equal($detailView.find('tr:last td:last').html(), 'fieldDContent', 'Last field has correct content');
          }
        }
      }
    };
    var $detailView = $('<div>');

    stop();

    $detailView.detailView(detailView);
  });

  test('Action', function() {
    var detailView = {
      actions: {
        actionA: {
          label: 'testActionA',
          action: function(args) {
            start();
            ok(args.response.success(), 'Call success from action A');
          },
          messages: {
            confirm: function() { return 'testActionAConfirm'; },
            notification: function() { return 'testActionANotification'; }
          }
        },
        actionB: {
          label: 'testActionB',
          action: function(args) {
            start();
            ok(args.response.success(), 'Call success from action B');
          },
          messages: {
            confirm: function() { return 'testActionBConfirm'; },
            notification: function() { return 'testActionBNotification'; }
          },
          notification: {
            poll: function(args) {
              start();
              ok(args.complete(), 'Call complete from async action B');
            }
          }
        }
      },
      tabs: {
        tabA: {
          title: 'tabA',
          fields: {
            fieldA: { label: 'fieldA' },
            fieldB: { label: 'fieldB' }
          },
          dataProvider: function(args) {
            args.response.success({
              data: {
                fieldA: 'fieldAContent',
                fieldB: 'fieldBContent'
              }
            });
          }
        }
      }
    };
    var $detailView = $('<div>');

    $detailView.detailView(detailView).appendTo('#qunit-fixture');

    equal($detailView.find('.detail-actions').size(), 1, 'Action container present');
    equal($detailView.find('.detail-actions .action').size(), 2, 'Correct action count');
    equal($detailView.find('.detail-actions .action.actionA').size(), 1, 'actionA present');
    equal($detailView.find('.detail-actions .action.actionB').size(), 1, 'actionB present');

    cloudStack.dialog.confirm = function(args) {
      start();
      equal(args.message, 'testActionAConfirm', 'Correct confirmation message for action A');
      stop();

      args.action(); // Perform action
    };

    cloudStack.ui.notifications.add = function(notification, success, successArgs) {
      stop();
      equal(notification.desc, 'testActionANotification', 'Correct notification message for action A');
      start();
    };

    $detailView.find('.detail-actions .action.actionA a').click(); // <a> triggers action, not action's container

    cloudStack.dialog.confirm = function(args) {
      start();
      equal(args.message, 'testActionBConfirm', 'Correct confirmation message for action B');
      stop();

      args.action(); // Perform action
    };

    cloudStack.ui.notifications.add = function(notification, success, successArgs) {
      start();
      equal(notification.desc, 'testActionBNotification', 'Correct notification message for action B');
      stop();
      notification.poll({ complete: function() { return true; } });
    };

    $detailView.find('.detail-actions .action.actionB a').click(); // <a> triggers action, not action's container
  });

  test('Action filter', function() {
    var detailView = {
      actions: {
        actionA: {
          label: 'testActionA',
          action: function(args) {}
        },
        actionB: {
          label: 'testActionB',
          action: function(args) {}
        }
      },
      tabs: {
        tabA: {
          title: 'tabA',
          fields: {
            fieldA: { label: 'fieldA' },
            fieldB: { label: 'fieldB' }
          },
          dataProvider: function(args) {
            args.response.success({
              actionFilter: function() {
                return ['actionA'];
              },
              data: {
                fieldA: 'fieldAContent',
                fieldB: 'fieldBContent'
              }
            });
          }
        }
      }
    };
    var $detailView = $('<div>');

    $detailView.detailView(detailView).appendTo('#qunit-fixture');

    equal($detailView.find('.detail-actions .action').size(), 1, 'Correct action count');
    equal($detailView.find('.detail-actions .action.actionA').size(), 1, 'actionA present');
    notEqual($detailView.find('.detail-actions .action.actionB').size(), 1, 'actionB not present');
  });

  test('Refresh', function() {
    var dataA = ['dataLoad1A', 'dataLoad2A'];
    var dataB = ['dataLoad1B', 'dataLoad2B'];
    var index = 0;

    var detailView = {
      tabs: {
        tabA: {
          title: 'tabA',
          fields: {
            fieldA: { label: 'fieldA' }
          },
          dataProvider: function(args) {
            args.response.success({ data: { fieldA: dataA[index]  }});
            start();
            equal($detailView.find('tr td:last').html(), dataA[index], 'Tab A data correct for load ' + (index + 1));
            index++;
          }
        },
        tabB: {
          title: 'tabB',
          fields: {
            fieldB: { label: 'fieldB' }
          },
          dataProvider: function(args) {
            args.response.success({ data: { fieldB: dataB[index]  }});
            start();
            equal($detailView.find('tr td:last').html(), dataB[index], 'Tab B data correct for load ' + (index + 1));
            index++;
          }
        }
      }
    };
    var $detailView = $('<div>');

    stop();
    $detailView.detailView(detailView).appendTo('#qunit-fixture');

    stop();
    $detailView.find('.button.refresh').click();

    stop();
    index = 0;
    $detailView.find('.ui-tabs-nav li.last a').click();

    stop();
    $detailView.find('.button.refresh').click();
  });

  test('View all, 1 section', function() {
    var $browser = $('<div>').appendTo('#qunit-fixture');
    var detailView = {
      $browser: $browser,
      context: {},
      viewAll: { label: 'testListView', path: 'testListView' },
      tabs: {
        tabA: {
          title: 'tabA',
          fields: [{ fieldA: { label: 'fieldA' }}],
          dataProvider: function(args) { args.response.success({ data: { fieldA: 'fieldAContent' } }); start(); }
        }
      }
    };
    var testListView = {};
    var $detailView = $('<div>').appendTo('#qunit-fixture');

    $('<div>').attr('cloudStack-container', true).data('cloudStack-args', {
      sections: {
        testListView: testListView
      }
    }).appendTo('#qunit-fixture');

    stop();

    $.fn.cloudBrowser = function(cmd, args) {};
    $browser.cloudBrowser();
    $detailView.detailView(detailView);

    equal($detailView.find('.detail-group.actions td.view-all').size(), 1, 'Detail view has view all button');

    stop();

    $.fn.listView = function(args, options) {
      start();
      ok(true, 'List view called');
      equal(args, testListView, 'Correct list view passed');
      ok(args.$browser.size(), 'Browser passed in args');
      ok($.isPlainObject(args.ref), 'Ref passed in args');
      equal(args.id, 'testListView', 'Correct section ID');

      return this;
    };
    
    $.fn.cloudBrowser = function(cmd, args) {
      start();
      equal(cmd, 'addPanel', 'Browser add panel called');
      stop();
      args.complete($('<div>'));
    };

    $detailView.find('.view-all a').click();
  });

  test('View all, subsections', function() {
    var $browser = $('<div>').appendTo('#qunit-fixture');
    var detailView = {
      $browser: $browser,
      context: {},
      viewAll: { label: 'testListView', path: 'testSection.listViewB' },
      tabs: {
        tabA: {
          title: 'tabA',
          fields: [{ fieldA: { label: 'fieldA' }}],
          dataProvider: function(args) { args.response.success({ data: { fieldA: 'fieldAContent' } }); start(); }
        }
      }
    };
    var listViewA = {};
    var listViewB = {};
    var $detailView = $('<div>').appendTo('#qunit-fixture');

    $('<div>').attr('cloudStack-container', true).data('cloudStack-args', {
      sections: {
        testSection: {
          sections: {
            listViewA: { listView: listViewA },
            listViewB: { listView: listViewB }
          }
        }
      }
    }).appendTo('#qunit-fixture');

    stop();

    $.fn.cloudBrowser = function(cmd, args) {};
    $browser.cloudBrowser();
    $detailView.detailView(detailView);

    equal($detailView.find('.detail-group.actions td.view-all').size(), 1, 'Detail view has view all button');

    stop();

    $.fn.listView = function(args, options) {
      start();
      ok(true, 'List view called');
      equal(args.listView, listViewB, 'Correct list view passed');
      ok(args.$browser.size(), 'Browser passed in args');
      ok($.isPlainObject(args.ref), 'Ref passed in args');
      equal(args.id, 'testSection', 'Correct section ID');

      return this;
    };
    
    $.fn.cloudBrowser = function(cmd, args) {
      start();
      equal(cmd, 'addPanel', 'Browser add panel called');
      stop();
      args.complete($('<div>'));
    };

    $detailView.find('.view-all a').click();
  });
}(jQuery));
