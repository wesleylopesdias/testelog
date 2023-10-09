package com.logicalis.serviceinsight.service;

import com.logicalis.serviceinsight.security.UserDetailsImpl;
import static com.logicalis.serviceinsight.security.UserDetailsImpl.toGrantedAuthorities;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 *
 * @author poneil
 */
@Service
public class SecurityUserDetailsService extends BaseServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String query = "select u.id, u.username, u.password, u.token, u.token_expires, au.authority"
                + " from users u"
                + " left join authorities au on au.user_id = u.id"
                + " where u.username = ? and u.enabled = true";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query, username);
        if (results != null && results.size() > 0) {
            String[] roles = new String[results.size()];
            Map<String, Object> userdata = null;
            for (int counter = 0; counter < results.size(); counter++) {
                userdata = results.get(counter);
                roles[counter] = (String) userdata.get("authority");
            }
            return new UserDetailsImpl(
                    (String) userdata.get("username"),
                    (String) userdata.get("password"),
                    (String) userdata.get("token"),
                    (Date) userdata.get("token_expires"),
                    (Collection<GrantedAuthority>) toGrantedAuthorities(roles));
        }
        throw new UsernameNotFoundException("User not found, enabled or has no roles");
    }
}
