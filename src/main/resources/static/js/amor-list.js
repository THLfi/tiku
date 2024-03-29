jQuery(document).ready(function($){
    $('.subject-button').click(function () {
        var target =
            $(this)
                .closest('tr')
                .siblings('.subject-' + $(this).attr('subject-ref'));
        if ($(this)
            .find('.caret')
            .toggleClass('caret-right')
            .is('.caret-right')) {
            target.hide();
        } else {
            target.show();
        }
    });
    $('.hydra-button').click(function () {
        var p = $(this).closest('tr');
        p.siblings('.' + p.attr('class').replace(/\s+(fh)?$/, '').replace(/\s+/g, '.')).toggle();
        $(this).find('.caret').toggleClass('caret-right');
    }).click();
});
