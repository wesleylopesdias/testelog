package com.logicalis.serviceinsight.web.controller;

import static org.springframework.http.HttpStatus.OK;

import com.logicalis.serviceinsight.service.SecurityService;
import com.logicalis.serviceinsight.service.ServiceException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author poneil
 */
@Controller
@RequestMapping("/myaccount")
public class UserManagementController extends BaseController {

    @Autowired
    SecurityService securityService;

    /**
     * Base method for reaching a User's account information
     *
     * @param uiModel
     * @return
     */
    @Secured({"ROLE_USER", "ROLE_BILLING", "ROLE_ADMIN"})
    @RequestMapping(method = RequestMethod.GET)
    public String myaccount(Model uiModel) {
        // put something into model, as needed...
        return "myaccount/show";
    }

    /**
     * Link sent in email to a user needing a password reset.
     *
     * The token is validated. If it is INVALID, the forward to
     * myaccount/newpassword includes a model object "error" that is an
     * APIResponse. Otherwise, the token can be re-used / submitted from the new
     * password form.
     *
     * @param base64EncodedToken
     * @return
     */
    @RequestMapping(value = "/resetpassword", method = RequestMethod.GET)
    public String resetPassword(Model uiModel, @RequestParam(value = "token", required = true) String encodedToken) {
        try {
            securityService.validateToken(encodedToken);
        } catch (ServiceException se) {
            APIResponse error = new APIResponse(APIResponse.Status.ERROR, se.getMessage());
            uiModel.addAttribute("error", error);
        } catch (Exception ex) {
            APIResponse error = new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_general_error_password_reset_validation", new Object[]{ex.getMessage()}, LocaleContextHolder.getLocale()));
            uiModel.addAttribute("error", error);
        }
        return "myaccount/newpassword";
    }

    /**
     * app posts to this method with just the username to start the reset
     * password process
     *
     * @param passwordReset
     * @return
     * @throws ServiceException
     */
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/resetpassword", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse resetPassword(@RequestBody UserPassword passwordReset) {
        try {
            securityService.sendPasswordReset(passwordReset.getUsername());
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_password_reset_sent", new Object[]{passwordReset.getUsername()}, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            Throwable thrown = se.getCause();
            if (thrown != null && thrown instanceof UsernameNotFoundException) {
                return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("security_ui_username_not_found", null, LocaleContextHolder.getLocale())); // a message more suitable for the UI...
            } else {
                return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_service_error_password_reset", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
            }
            //return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_service_error_password_reset", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception ex) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_general_error_password_reset", new Object[]{ex.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }

    /**
     * Base method to display page for an authenticated user to change their
     * password
     *
     * @param uiModel
     * @return
     */
    @Secured({"ROLE_USER", "ROLE_BILLING", "ROLE_ADMIN"})
    @RequestMapping(value = "/updatepassword", method = RequestMethod.GET)
    public String updatePassword(Model uiModel) {
        return "myaccount/updatepassword";
    }

    /**
     * Handles 2 scenarios: 1) An authenticated user wants to simply change his
     * password and submits form with oldPassword, newPassword, confirmPassword
     * 2) A password reset user passes in the token, which is validated and
     * username for token is used to setup the password update
     *
     * @param updatePassword object represents form data
     * @return
     * @throws ServiceException
     */
    @ResponseBody
    @ResponseStatus(OK)
    @RequestMapping(value = "/updatepassword", method = RequestMethod.PUT, consumes = APPLICATION_JSON_VALUE, produces = {APPLICATION_JSON_VALUE, "application/javascript"})
    public APIResponse updatePassword(@RequestBody UserPassword updatePassword) {
        String authenticated = authenticatedUser();
        if (authenticated == null) {
            try {
                String username = securityService.validateToken(updatePassword.getToken());
                securityService.updatePassword(username, updatePassword.getNewPassword());
                String loginLink = securityService.getApplicationLoginLink();
                return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_password_reset", new Object[]{loginLink}, LocaleContextHolder.getLocale()));
            } catch (ServiceException se) {
                return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_service_error_password_change", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
            } catch (Exception ex) {
                return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_general_error_password_change", new Object[]{ex.getMessage()}, LocaleContextHolder.getLocale()));
            }
        }
        try {
            securityService.checkUserAuthentication(authenticated, updatePassword.getOldPassword());
            securityService.updatePassword(authenticated, updatePassword.getNewPassword());
            return new APIResponse(APIResponse.Status.OK, messageSource.getMessage("api_ok_password_changed", null, LocaleContextHolder.getLocale()));
        } catch (ServiceException se) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_service_error_password_change", new Object[]{se.getMessage()}, LocaleContextHolder.getLocale()));
        } catch (Exception ex) {
            return new APIResponse(APIResponse.Status.ERROR, messageSource.getMessage("api_general_error_password_change", new Object[]{ex.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }

    public static class UserPassword {

        private String username;
        private String oldPassword;
        private String newPassword;
        private String confirmPassword;
        private String token;

        public UserPassword() {
        }

        public UserPassword(String username) {
            this.username = username;
        }

        public UserPassword(String oldPassword, String newPassword, String confirmPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            this.confirmPassword = confirmPassword;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
