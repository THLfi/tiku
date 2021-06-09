$(document).ready(function() {
    $('#password').keyup(function() {
            if (!isPasswordValid($('#password').val())) {
                $('#password-validation-error').prop('hidden', false);
                $('#submitbutton').prop('disabled', true);
            } else {
                $('#password-validation-error').prop('hidden', true);
                $('#submitbutton').prop('disabled', false);
            }
        }
    )

    function isPasswordValid(password) {
        var passwordValid = false;
        if (password.length <= 100) passwordValid = true;

        return passwordValid;
    }
});
