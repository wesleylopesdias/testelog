package com.logicalis.serviceinsight.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.MimeMessage;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author poneil
 */
@Transactional(readOnly = false, rollbackFor = ServiceException.class)
@org.springframework.stereotype.Service
public class SecurityServiceImpl extends BaseServiceImpl implements SecurityService {

    @Autowired
    UserDetailsService securityUserDetailsService;
    @Value("${lost.password.key}")
    private String key;
    @Value("${lost.password.expires.hours}")
    private Integer expireHours;
    @Autowired
    private Md5PasswordEncoder passwordEncoder;
    @Autowired
    private StandardPasswordEncoder standardPasswordEncoder;

    @Override
    public void sendPasswordReset(final String email) throws ServiceException {
        try {
            UserDetails userDetails = securityUserDetailsService.loadUserByUsername(email);
            final String token = makeTokenSignature(userDetails.getUsername(), userDetails.getPassword());
            saveUserToken(userDetails.getUsername(), token);
            final String templateLocation = getTemplateLocation("password_reset", LocaleContextHolder.getLocale());
            MimeMessagePreparator preparator = new MimeMessagePreparator() {
                public void prepare(MimeMessage mimeMessage) throws Exception {
                    MimeMessageHelper message = new MimeMessageHelper(mimeMessage, Boolean.TRUE);
                    message.setTo(email);
                    message.setFrom(noReplyEmail, "Service Insight");
                    message.setReplyTo(noReplyEmail, "Service Insight");
                    message.setSubject(messageSource.getMessage("mail_password_reset_subject", null, LocaleContextHolder.getLocale()));
                    Map model = new HashMap();
                    String resetlink = UriComponentsBuilder.fromHttpUrl(httpHost)
                            .path("/myaccount/resetpassword")
                            .queryParam("token", token)
                            .build().toUriString();
                    model.put("resetlink", resetlink);
                    String text = VelocityEngineUtils.mergeTemplateIntoString(
                            velocityEngine, templateLocation, mailEncoding, model);
                    message.setText(text, true);
                    message.addInline("logo", new ClassPathResource(String.format("%s/%s", baseTemplateLocation, emailLogo)));
                }
            };
            mailSender.send(preparator);
        } catch (UsernameNotFoundException unfe) {
            throw new ServiceException(messageSource.getMessage("security_username_not_found", new Object[]{email}, LocaleContextHolder.getLocale()), unfe);
        } catch (Exception ex) {
            log.error("whoops... general error", ex);
            throw new ServiceException(messageSource.getMessage("password_reset_exception", new Object[]{email}, LocaleContextHolder.getLocale()), ex);
        }
    }

    private Integer saveUserToken(String username, String token) throws ServiceException {
        try {
            return jdbcTemplate.update("update users set token = ?, token_expires = ? where username = ?",
                    new Object[]{token, new DateTime().withZone(DateTimeZone.forID(TZID)).plusHours(expireHours).toDate(), username});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_user_update", new Object[]{username, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void updatePassword(String username, String newPassword) throws ServiceException {
        try {
            Integer updated = jdbcTemplate.update("update users set password = ?, token = null, token_expires = null where username = ?",
                    new Object[]{passwordEncoder.encodePassword(newPassword, null), username});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_user_update_password", new Object[]{username, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public String validateToken(String token) throws ServiceException {
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap("select username, password, token_expires"
                    + " from users where token = ?", token);
            DateTime tokenExpires = new DateTime((Date) result.get("token_expires"));
            DateTime now = new DateTime().withZone(DateTimeZone.forID(TZID));
            if (now.isAfter(tokenExpires)) {
                throw new ServiceException(messageSource.getMessage("password_reset_token_expired", null, LocaleContextHolder.getLocale()));
            }
            String username = (String) result.get("username");
            String password = (String) result.get("password");
            if (!standardPasswordEncoder.matches(username + ":" + password + ":" + key, token)) {
                throw new ServiceException(messageSource.getMessage("password_reset_token_invalid", null, LocaleContextHolder.getLocale()));
            }
            log.info("validated token and looked up user {}", username);
            return username;
        } catch (IncorrectResultSizeDataAccessException irs) {
            throw new ServiceException(messageSource.getMessage("password_reset_validation_error_irs", null, LocaleContextHolder.getLocale()));
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("password_reset_validation_error", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }

    private String makeTokenSignature(String username, String password) {
        String data = username + ":" + password + ":" + key;
        return standardPasswordEncoder.encode(data);
    }

    @Override
    public String getApplicationLoginLink() {
        return UriComponentsBuilder.fromHttpUrl(httpHost).path("/login").build().toUriString();
    }

    @Override
    public void checkUserAuthentication(String username, String oldPassword) throws ServiceException {
        try {
            String encodedPassword = passwordEncoder.encodePassword(oldPassword, null);
            Map<String, Object> result = jdbcTemplate.queryForMap("select username, password"
                    + " from users where username = ? and password = ?", username, encodedPassword);
        } catch (IncorrectResultSizeDataAccessException irs) {
            throw new ServiceException(messageSource.getMessage("user_authentication_wrong_password", null, LocaleContextHolder.getLocale()));
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("user_authentication_general_error", new Object[]{any.getMessage()}, LocaleContextHolder.getLocale()));
        }
    }
}
