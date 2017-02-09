(function ($, thl) {
  $(document).ready(function () {

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

    var treeBrowser = $('.tree-browser'),
      traverseDimensionTree = function (dimension, node) {
        var children = $('<ul>').addClass('tree');
        for (var n = 0; n < node.children.length; ++n) {
          var li = $('<li>')
            .append(
              $('<span>')
                .attr('dim-ref', dimension)
                .attr('node-ref', node.children[n].sid)
                .append(node.children[n].label)
            );
          if (node.children[n].children.length > 0) {
            li.find('span').append($('<span>').addClass('caret caret-right'));
            li.append(traverseDimensionTree(dimension, node.children[n]));
          }
          children.append(li);
        }
        return children;
      },
      nthRow = function (e) {
        return e.index() + 1;
      },
      nthCol = function (e) {
        return e.parent().index() + 1;
      };

    $('.label').hover(function () {
      $(this).removeClass('hover');
    }, function () {
      $(this).addClass('hover');
    });

    $.each(thl.pivot.dim, function (i, v) {
      var labelSpan = $('<span>').html(v.label);
      var subtree = $('<li>').append(labelSpan);
      if (v.children.length > 0) {
        labelSpan.append($('<span>').addClass('caret caret-right'));
        subtree.append(traverseDimensionTree(v.id, v));
      }
      treeBrowser.append(subtree);
    });

    $('.tree').hide();
    $('.tree-browser>li>span,.tree>li>span')
      .click(function () {
        var t = $(this);
        t.find('span').toggleClass('caret-right');
        t.siblings('ul').toggle();
      });
    $('.tree>li>span')
      .draggable({
        cursor: 'move',
        helper: 'clone',
        appendTo: '.pivot-content',
        start: function (e, ui) { $(ui.helper).css('top', '-20px'); }

      });
    $('.row-target')
      .droppable({
        activeClass: 'drop-active',
        drop: function (e, ui) {
          if ($('.row-selection').size() < nthRow($(this))) {
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
        activeClass: 'drop-active',
        drop: function (e, ui) {
          if ($('.column-selection').size() < nthCol($(this))) {
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
    $('.measure-target')
      .droppable({
        activeClass: 'drop-active',
        accept: function (t) {
          return 'measure' === t.attr('dim-ref');
        },
        drop: function (e, ui) {
          if ($('#filter-measure').size() === 0) {
            $('#pivot').append('<input type="text" name="filter" id="measure" dim-ref="measure" class="form-control" readonly>');
          }
          changeView($('#filter-measure'), ui.draggable.attr('dim-ref') + thl.separator + ui.draggable.attr('node-ref'));
        }
      });

    $('.filter-target')
      .droppable({
        activeClass: 'drop-active',
        hoverClass: 'drop-hover',
        drop: function (e, ui) {
          var dr = ui.draggable.attr('dim-ref');
          var input = $('.filter-' + dr);
          if (input.size() === 0) {
            input = $('<input type="text" name="filter" class="form-control" id="filter-' + dr + '" dim-ref="' + dr + '" readonly>');
            $('#pivot').append(input);
          }
          changeView(input, ui.draggable.attr('dim-ref') + thl.separator + ui.draggable.attr('node-ref'));
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

      var sort = 0, // Determines the current sort order in dimension
        nodeOptions = {}, // hash index for options for quicker handling
        /**
         * Recursively go through the dimension and add each node to
         * the selectable list
         */
        traverse = function (nodes, level) {
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
          });
          if (!isAdded) {
            selected.append(option);
            selected.scrollTop(option.scrollTop());
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
      var self = $(this),
        filter = self.find('input');
      var previousValue = '';
      filter.keydown(function () {
        var re = new RegExp(this.value, 'i'),
          options = self.find('div');
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
      var level = $(this).closest('.dropdown').attr('data-level'),
        dim = 0,
        selectable = $('#subset-selector .selectable .options'),
        selected = $('#subset-selector .selected .options'),
        options = thl.columns[level].nodes,
        input = $('.column-selection').get(level);

      $.each(thl.pivot.dim, function (i, v) {
        if (v.id === thl.columns[level].dimension) {
          dim = v;
        }
      });
      populateModal(level, dim, selectable, selected, options, input);
    });
    $('.row.dropdown-menu .select-subset').click(function (e) {
      var level = $(this).closest('.dropdown').attr('data-level'),
        dim = 0,
        selectable = $('#subset-selector .selectable .options'),
        selected = $('#subset-selector .selected .options'),
        options = thl.rows[level].nodes,
        input = $('.row-selection').get(level);

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
      var index = self.attr('data-ref'),
        fields = $(selector),
        value = fields.get(index).value;

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

    var dropdown = $('<div class="dropdown">'),
      dropdownToggle = $('<button type="button" class="btn btn-xs btn-default drowdown-toggle" data-toggle="dropdown"><span class="caret"></span></button>'),
      dropdownMenu = $('<ul class="dropdown-menu">'),
      dropdownMenuSortAsc = $('<li><a role="menuitem" class="asc">' + thl.messages['cube.dimension.sort.asc'] + '</a></li>'),
      dropdownMenuSortDesc = $('<li><a role="menuitem" class="desc">' + thl.messages['cube.dimension.sort.desc'] + '</a></li>'),
      dropdownMenuHide = $('<li><a role="menuitem" class="rowhide">' + thl.messages['cube.dimension.hide'] + '</a></li>'),
      dropdownMenuMeta = $('<li><a role="menuitem" class="info">' + thl.messages['cube.dimension.info'] + '</a></li>');

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
        var sort = $('#pivot input[name=sort]'),
          sortMode = $('#pivot input[name=mode]');

        thl.toggleField(sort);
        thl.toggleField(sortMode);

        sortMode.val($(this).attr('class'));

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
  });
})(window.jQuery, window.thl);
