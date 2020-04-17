/**
 * Original source from: https://github.com/armaaar/JQuery-Sticky-Table
 *
 * IE related code was removed due performance issues with IE11.
 * Added support for header columns with rowspan attribute. 
 *
 */

 jQuery(document).on('stickyTable', function() {
    var positionStickySupport = (function() {
        var el = document.createElement('a'), mStyle = el.style;
        mStyle.cssText = "position:sticky;position:-webkit-sticky;position:-ms-sticky;";
        return mStyle.position.indexOf('sticky')!==-1;
    })();

    if (positionStickySupport) {
        var offset = 0;
        $(".sticky-table").each(function() {
            offset = 0;
            $(this).find("table tr.sticky-header").each(function() {
                $(this).find("th").css('top', offset);
                $(this).find("td").css('top', offset);
                offset += $(this).outerHeight();
            });

            offset = 0;
            $($(this).find("table tr.sticky-footer").get().reverse()).each(function() {
                $(this).find("th").css('bottom', offset);
                $(this).find("td").css('bottom', offset);
                offset += $(this).outerHeight();
            });
        });

		var columnOffsets = [0];
		$('.sticky-ltr-cells table tr').has('.row-target').first().find('th.sticky-cell').each(function(i, cell) {
			columnOffsets[i + 1] = $(cell).outerWidth();
			if (i > 0) {
			    columnOffsets[i + 1] += columnOffsets[i];
			}
		});
        $('.sticky-ltr-cells table tr').each(function() {
            $(this).find('.sticky-cell').each(function() {
       		    $(this).css('left', columnOffsets[$(this).data('level')]);
	        });
        });
    }
    $(window).resize(function() {
        $(".sticky-table").scroll();
    });

});