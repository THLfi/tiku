(function ($, thl) {
  $(document).ready(function () {

    var stickyHeadersSupported = function() {
      // Sticky headers works with Chrome and Firefox (Safari to be tested)
      // Original stickytable code was unusable with IE11 due performance issues.
      return /Chrome/.test(navigator.userAgent) && /Google Inc/.test(navigator.vendor) // Chrome
      ||
      !(window.mozInnerScreenX == null) // Firefox
      ||
      navigator.vendor.indexOf('Apple') > -1; // Safari and other browsers running under Apple OS
    }();

    $('.fa-expand-alt-cube').click(function() {
      var t = $(this);
      if (t.hasClass('fa-expand-alt') && stickyHeadersSupported) {
        $('table.cube').wrap('<div class="sticky-table sticky-ltr-cells"></div>');
        $(document).trigger('stickyTable');
      }
      else if ($('.sticky-table').length) {
        $('table.cube th.sticky-cell').removeAttr('style');
        $('table.cube tr.sticky-header th').removeAttr('style');
        $('table.cube tr.sticky-header').removeAttr('style');
        $('table.cube').unwrap();
      }
      t.toggleClass('fa-expand-alt fa-compress-alt');
      $('body').toggleClass('full-screen');
    });

    function changeView (inputElement, value) {
      if (typeof inputElement.val === 'undefined') {
        inputElement = $(inputElement);
      }
      var original = inputElement.val();
      inputElement.val(value);
      var f = function () {
        inputElement.val(original);
        $(window).off('beforeunload', f);
      };
      $(window).on('beforeunload', f);
      $('#pivot').submit();
    }

    function changeViewRemove (inputElement, index) {
      $(inputElement.get(index)).remove();
      $('#pivot').submit();
    }

    var treeBrowser = $('.tree-browser');
    $('.browser-toggle').click(function () {
      treeBrowser.find('li, .tree').removeClass('open closed');
      treeBrowser.find('.caret').addClass('caret-right');
      treeBrowser.toggleClass('active');
    });

    var traverseDimensionTree = function (dimension, node, i) {
      var children = $('<ul>').addClass('tree')
      for (var n = 0; n < node.children.length; ++n) {
        var li = $('<li>')
          .append(
            $('<span>')
              .attr('dim-ref', dimension)
              .attr('node-ref', node.children[n].sid)
              .append(node.children[n].label)
          );
        if (node.children[n].children.length > 0) {
          li.find('span').prepend($('<span>').addClass('caret caret-right'));
          li.append(traverseDimensionTree(dimension, node.children[n]));
        }
        children.append(li);
      }
      return children;
    };
    var nthRow = function (e) {
      return e.index() + 1;
    };
    var nthCol = function (e) {
      return e.parent().index() + 1;
    };

    $('.label').hover(function () {
      $(this).removeClass('hover');
    }, function () {
      $(this).addClass('hover');
    });

    $.each(thl.pivot.dim, function (i, v) {
      var labelSpan = $('<span>')
        .html(v.label)
        .attr('dim-ref', v.id)
        .attr('node-ref', '0');
      var subtree = $('<li>').append(labelSpan);
      if (v.children.length > 0) {
        labelSpan.prepend($('<span>').addClass('caret caret-right'));
        subtree.append(traverseDimensionTree(v.id, v));
      }
      treeBrowser.append(subtree);
    });

    var nodes = $('.tree-browser>li>span,.tree>li>span')
      .click(function () {
        var t = $(this);
        var span = t.find('span').toggleClass('caret-right');
        if (span.length > 0) {
          t.siblings('ul').toggleClass('open');
          t.closest('li').siblings().toggleClass('closed');
        }
        if(window.sessionStorage) {
          var k = thl.id + ':' + t.attr('dim-ref') + '-' + t.attr('node-ref');
          if(window.sessionStorage.getItem(k)) {
            window.sessionStorage.removeItem(k)
          } else {
            window.sessionStorage.setItem (k,'1');
          }
        }
      });
    if(window.sessionStorage) {
      nodes.each(function () {
        var t = $(this);
        var k = thl.id + ':' + t.attr('dim-ref') + '-' + t.attr('node-ref');
        if(window.sessionStorage.getItem(k)) {
          var span = t.find('span').toggleClass('caret-right');
          if (span.length > 0) {
            t.siblings('ul').toggleClass('open');
            t.closest('li').siblings().toggleClass('closed');
          }
        }
      });
    }
    $('.tree>li>span')
      .draggable({
        cursor: 'move',
        helper: 'clone',
        appendTo: 'body',
        start: function (e, ui) {
          $(ui.helper)
            .css('top', '-20px')
            .css('z-index', '1000');
          $('.pivot-content').toggleClass('drop-active', true);
          treeBrowser.removeClass('active');
          var dr = ui.helper.attr('dim-ref');
          if (!thl.pivot.multipleMeasuresShown && dr === 'measure') {
            ft.find('span').text(thl.messages['cube.filter.measure']);
          } else {
            ft.find('span').text(thl.messages['cube.filter']);
          }
        },
        stop: function () {
          $('.pivot-content').toggleClass('drop-active', false);
        }
      });

    $('.measure-target')
        .droppable({
          accept: function (t) {
            return t.attr('dim-ref') === 'measure';
          },
          drop: function (e, ui) {
            if ($('#filter-measure').length === 0) {
              $('#pivot').append('<input type="text" name="filter" id="measure" dim-ref="measure" class="form-control" readonly>');
            }
            changeView($('#filter-measure'), ui.draggable.attr('dim-ref') + thl.separator + ui.draggable.attr('node-ref'));
          }
        });

    var ft = $('.filter-target')
        .droppable({
          hoverClass: 'drop-hover',
          drop: function (e, ui) {
            var dr = ui.draggable.attr('dim-ref');
            var input = $('.filter-' + dr);
            if (input.length === 0) {
              input = $('<input type="text" name="filter" class="form-control" id="filter-' + dr + '" dim-ref="' + dr + '" readonly>');
              $('#pivot').append(input);
            }
            changeView(input, ui.draggable.attr('dim-ref') + thl.separator + ui.draggable.attr('node-ref'));
          }
        });

    $('.row-target')
      .droppable({
        drop: function (e, ui) {
          if ($('.row-selection').length < nthRow($(this))) {
            $('#pivot').append('<input type="text" name="row" class="row-selection form-control" readonly>');
          }
          changeView($('.row-selection').get(nthRow($(this)) - 1), ui.draggable.attr('dim-ref') + thl.separator + ui.draggable.attr('node-ref'));
        },
        over: function (e, ui) {
          $('table th.row-target[data-level=' + ($(this).attr('data-level')) + ']').addClass('drop-hover');
          $(this).addClass('hover');
        },
        out: function () {
          $('table th.row-target[data-level=' + ($(this).attr('data-level')) + ']').removeClass('drop-hover');
          $(this).removeClass('hover');
        }
      });
    $('.column-target')
      .droppable({
        drop: function (e, ui) {
          if ($('.column-selection').length < nthCol($(this))) {
            $('#pivot').append('<input type="text" name="column" class="column-selection form-control" readonly>');
          }
          changeView($('.column-selection').get(nthCol($(this)) - 1), ui.draggable.attr('dim-ref') + thl.separator + ui.draggable.attr('node-ref'));
        },
        over: function () {
          $(this).closest('tr').find('th').toggleClass('drop-hover', true);
        },
        out: function () {
          $(this).closest('tr').find('th').toggleClass('drop-hover', false);
        }
      });

    $('.column.dropdown-menu .remove').click(function (e) {
      e.preventDefault();
      changeViewRemove($('.column-selection'), $(this).attr('data-ref'));
    });
    $('.row.dropdown-menu .remove').click(function (e) {
      e.preventDefault();
      changeViewRemove($('.row-selection'), $(this).attr('data-ref'));
    });

    /**
     * Displays a modal dialog where the user can select which dimension nodes
     * they'd like to display in a column or a row. Selectable nodes are
     * added to a select list in the dimensions order and indented accordingly.
     * The current selection is added to the selected list. User is provided
     * with a simple infix search and buttons for select all and remove all
     * events
     **/
    function populateModal (level, dim, selectable, selected, options, input) {
      $('#subset-selector').modal('show');
      $('#subset-selector h4 span').text(dim.label);

      /* Clear previous dialog values */
      selectable.find('div').remove();
      selected.find('div').remove();

      var sort = 0; // Determines the current sort order in dimension
      var nodeOptions = {}; // hash index for options for quicker handling
        /**
         * Recursively go through the dimension and add each node to
         * the selectable list
         */
      var traverse = function (nodes, level) {
        $.each(nodes, function (i, v) {
          var option = $('<div></div>')
            .text(v.label)
            .attr('data-val', v.sid)
            .attr('data-sort', ++sort)
            .addClass('l' + level);

          if (level > 0) {
            option.addClass('l');
          }

          selectable.append(option);
          nodeOptions[v.sid] = option;
          traverse(v.children, level + 1);
        });
      };

      traverse(dim.children, 0);

      /* Add each selected node using a clone from the selectable list. We use a clone
       * so we can get the sort order from the selectable-list
       */
      $.each(options, function (v) {
        selected.append(nodeOptions[options[v]].clone());
      });

      /* wire remove all button */
      selected.closest('.form-group').siblings('.btn').click(function () {
        selected.find('div').remove();
      });

      /* wire select all button */
      selectable
        .closest('.form-group')
        .siblings('.btn')
        .click(function () {
          /* clear selected list so that we don't have to check for duplicates */
          selected.find('div').remove();
          /* clone all selectable nodes and add them to the selected list */
          selectable
            .find('div:not(.disabled)')
            .clone()
            .click(function () { $(this).remove(); })
            .appendTo(selected);
        });

      /* wire removal of single selected node to click event */
      selected.find('div').click(function () { $(this).remove(); });

      /* Add option to selected list on click */
      selectable.find('div').click(
        function () {
          /* Only add option once */

          var option = $(this).clone();
          if (selected.find('div[data-val=' + option.attr('data-val') + ']').length > 0) {
            return;
          }

          var sort = parseInt(option.attr('data-sort'), 10);
          var isAdded = false;

          /* Remove option onclick */
          option.click(function () { $(this).remove(); });

          /* add option to selected list in it's place */
          selected.find('div').each(function () {
            if (parseInt($(this).attr('data-sort'), 10) > sort) {
              $(this).before(option);
              isAdded = true;
              return false;
            }
            return true;
          })
          .removeClass('just-added');
          if (!isAdded) {
            selected.append(option);
          }
          /* Highlight last added elements - position before animating */
          option.addClass('just-added');
          if (selected.length) {
            selected.scrollTop(selected.scrollTop() - selected.offset().top + option.offset().top - selected.height() / 2);
          }
        });

      /* create row or column subset http parameter value and reload cube */
      $('#subset-selector .save').click(function () {
        var values = [];
        selected.find('div').each(function () {
          values.push($(this).attr('data-val'));
        });
        // We have to add the extra separator in case only one item has been selected
        changeView(input, dim.id + thl.separator + values.join(thl.subsetSeparator) + thl.subsetSeparator);
      });
    }

    $('#subset-selector .selectable, #subset-selector .selected').each(function () {
      var self = $(this);
      var filter = self.find('input');
      var previousValue = '';
      filter.keyup(function () {
        var re = new RegExp(this.value, 'i');
        var options = self.find('.options').children();
        if (this.value.length < previousValue.length) {
          options.show();
          options.toggleClass('disabled', false);
        }
        previousValue = this.value;
        options
          .filter(function () {
            return !re.test($(this).text());
          })
          .toggleClass('disabled', true)
          .hide();
      });
    });

    $('.column.dropdown-menu .select-subset').click(function (e) {
      var level = $(this).closest('.dropdown').attr('data-level');
      var dim = 0;
      var selectable = $('#subset-selector .selectable .options');
      var selected = $('#subset-selector .selected .options');
      var options = thl.columns[level].nodes;
      var input = $('.column-selection').get(level);

      $.each(thl.pivot.dim, function (i, v) {
        if (v.id === thl.columns[level].dimension) {
          dim = v;
        }
      });
      populateModal(level, dim, selectable, selected, options, input);
    });
    $('.row.dropdown-menu .select-subset').click(function (e) {
      var level = $(this).closest('.dropdown').attr('data-level');
      var dim = 0;
      var selectable = $('#subset-selector .selectable .options');
      var selected = $('#subset-selector .selected .options');
      var options = thl.rows[level].nodes;
      var input = $('.row-selection').get(level);

      $.each(thl.pivot.dim, function (i, v) {
        if (v.id === thl.rows[level].dimension) {
          dim = v;
        }
      });
      populateModal(level, dim, selectable, selected, options, input);
    });
    $('.column.dropdown-menu .drill-up').click(function (e) {
      e.preventDefault();
      changeView($('.column-selection').get($(this).attr('data-ref')), $(this).attr('value-ref'));
    });

    var moveDimension = function (self, selector, modifier) {
      var index = self.attr('data-ref');
      var fields = $(selector);
      var value = fields.get(index).value;

      fields.get(+index).value = fields.get(+index + modifier).value;
      fields.get(+index + modifier).value = value;

      $('#pivot').submit();
    };

    $('.column.dropdown-menu .move-up').click(function (e) {
      e.preventDefault();
      moveDimension($(this), '.column-selection', -1);
    });
    $('.column.dropdown-menu .move-down').click(function (e) {
      e.preventDefault();
      moveDimension($(this), '.column-selection', +1);
    });

    $('.row.dropdown-menu .move-up').click(function (e) {
      e.preventDefault();
      moveDimension($(this), '.row-selection', -1);
    });
    $('.row.dropdown-menu .move-down').click(function (e) {
      e.preventDefault();
      moveDimension($(this), '.row-selection', +1);
    });

    $('.column.dropdown-menu .expand').click(function (e) {
      e.preventDefault();
      var self = $(this);
      var ref = self.data('ref');
      var index = self.data('index');
      changeView($('.column-selection').get(index), ref + 'L');
    });

    $('.row.dropdown-menu .expand').click(function (e) {
      e.preventDefault();
      var self = $(this);
      var ref = self.data('ref');
      var index = self.data('index');
      changeView($('.row-selection').get(index), ref + 'L');
    });

    $('.column.dropdown-menu .add-level').click(function (e) {
      var l = $('<input type="text" name="column" class="column-selection form-control" readonly>');
      $('#pivot').append(l);
      l.val($(this).data('ref') + 'L');
      $('#pivot').submit();
    });

    $('.row.dropdown-menu .add-level').click(function (e) {
      var l = $('<input type="text" name="row" class="row-selection form-control" readonly>');
      $('#pivot').append(l);
      l.val($(this).data('ref') + 'L');
      $('#pivot').submit();
    });

    $('.row.dropdown-menu .drill-up').click(function (e) {
      e.preventDefault();
      changeView($('.row-selection').get($(this).attr('data-ref')), $(this).attr('value-ref'));
    });
    $('.row-target a')
      .click(function (e) {
        e.preventDefault();
        changeView($('.row-selection').get($(this).closest('.row-target').attr('data-level')), $(this).attr('data-ref'));
      });
    $('.column-target a')
      .click(function (e) {
        e.preventDefault();
        changeView($('.column-selection').get($(this).closest('.column-target').attr('data-level')), $(this).attr('data-ref'));
      });
    $('.reset-action')
      .click(function () {
        $('#pivot input').remove();
        $('#pivot').submit();
      });
    $('.transpose-action')
      .click(function () {
        // FIXME
        $('.column-selection').attr('name', 'row');
        $('.row-selection').attr('name', 'column');
        $('#pivot').submit();
      });
    $('.hide-empty-action')
      .click(function () {
        var fo = $('#pivot input[name=fo]');
        thl.toggleField(fo);
        $('#pivot').submit();
      });
    $('.hide-zero-action')
      .click(function () {
        var fz = $('#pivot input[name=fz]');
        thl.toggleField(fz);
        $('#pivot').submit();
      });
    $('.show-codes-action')
      .click(function () {
        var sc = $('#pivot input[name=sc]');
        thl.toggleField(sc);
        $('#pivot').submit();
      });

    $('dl dd > span').click(function () {
      $($('.filter-' + $(this).attr('data-ref')).get($(this).attr('data-index'))).remove();
      $('#pivot').submit();
    }).hover(function () {
      $(this).toggleClass('hover');
    });

    var dropdown = $('<div class="dropdown">');
    var dropdownToggle = $('<button type="button" class="btn btn-secondary btn-sm drowdown-toggle" data-bs-toggle="dropdown"><span class="caret"></span></button>');
    var dropdownMenu = $('<ul class="dropdown-menu">');
    var dropdownMenuSortAsc = $('<li><a role="menuitem" class="asc dropdown-item">' + thl.messages['cube.dimension.sort.asc'] + '</a></li>');
    var dropdownMenuSortDesc = $('<li><a role="menuitem" class="desc dropdown-item">' + thl.messages['cube.dimension.sort.desc'] + '</a></li>');
    var dropdownMenuHide = $('<li><a role="menuitem" class="rowhide dropdown-item">' + thl.messages['cube.dimension.hide'] + '</a></li>');
    var dropdownMenuMeta = $('<li><a role="menuitem" class="info dropdown-item">' + thl.messages['cube.dimension.info'] + '</a></li>');

    dropdownMenu
      .append(dropdownMenuSortAsc)
      .append(dropdownMenuSortDesc)
      .append(dropdownMenuHide)
      .append(dropdownMenuMeta);

    dropdown
      .append(dropdownToggle)
      .append(dropdownMenu);

    $('.row-target, .column-target')
      .not('.accept-all')
      .append(dropdown)
      .find('.asc,.desc')
      .click(function () {
        var sort = $('#pivot input[name=sort]');
        var sortMode = $('#pivot input[name=mode]');

        thl.toggleField(sort);
        thl.toggleField(sortMode);

		var sortModeVal = $(this).hasClass('asc') ? 'asc' : 'desc';
        sortMode.val(sortModeVal);

        if ($(this).closest('.row-target').length > 0) {
          sort.val('r' + $(this).closest('th').attr('data-ref'));
        } else {
          sort.val('c' + $(this).closest('th').attr('data-ref'));
        }

        $('#pivot').submit();
      });

    var metadataCallback = function (href) {
      var w = window.open(href, 'tikumetadata', 'scrollbars=1,menubar=0,resizable=1,status=0,location=0,width=650,height=450,screenX=250,screenY=350');
      if (window.focus) {
        w.focus();
      }
    };
    $('th .info').click(function (e) {
      e.preventDefault();
      var target = $(this).closest('.row-target, .column-target');
      var ref = target.find('a').first().attr('data-ref');
      var i = ref.lastIndexOf('-');
      metadataCallback(window.location.protocol + '//' + window.location.host + thl.url + ref.substring(i + 1));
    });

    $('#measure-meta').click(function (e) {
      e.preventDefault();
      metadataCallback(this.href);
    });

    $('th .rowhide').click(function () {
      var target = $(this).closest('.row-target, .column-target');
      var level = target.attr('data-level');
      var ref = target.find('a').first().attr('data-ref');
      var dimension = 0;
      var nodes = 0;
      var input = 0;
      if (target.is('.row-target')) {
        dimension = thl.rows[level].dimension;
        nodes = thl.rows[level].nodes;
        input = $('.row-selection').get(level);
      } else {
        dimension = thl.columns[level].dimension;
        nodes = thl.columns[level].nodes;
        input = $('.column-selection').get(level);
      }
      nodes.splice(
        nodes.indexOf(
          ref.substring(ref.indexOf(thl.separator) + 1)),
        1);
      changeView(input, dimension + thl.separator + nodes.join(thl.subsetSeparator) + thl.subsetSeparator);
    });

    if (!document.cookie.match(/cube_info/)) {
      $('.quick-info').removeClass('d-none');
      $('#close-quick-info').click(function() {
        var date = new Date();
        var expireTime = date.getTime() + 10000 * 24 * 60 * 60 * 1000;
        date.setTime(expireTime);
        document.cookie = "cube_info=shown;expires=" + date.toGMTString() + "; path=/";
        $('.quick-info').removeClass('show');
      });
    }

  });
})(window.jQuery, window.thl);
