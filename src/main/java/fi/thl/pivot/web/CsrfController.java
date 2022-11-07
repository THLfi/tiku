package fi.thl.pivot.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class CsrfController extends AbstractController {

    /**
     * End point to get csrf token bound to current session. Adds support for api-login from internet.
     * @return csrf token bound to current session.
     */
    @RequestMapping(value = "/csrf", produces = "text/plain", method = RequestMethod.GET)
    @ResponseBody
    public String getCsrf(HttpSession session, HttpServletRequest request) {
        return getCsrf();
    }

}
