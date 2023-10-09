package com.logicalis.serviceinsight.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 *
 * @author poneil
 */
public class UserDetailsImpl implements UserDetails {

    private String username;
    private String password;
    private String token;
    private Date tokenExpires;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(String username, String password) {
        this(username, password, null, null, (Collection<GrantedAuthority>) toGrantedAuthorities("ROLE_USER"));
    }

    public UserDetailsImpl(String username, String password, String token, Date tokenExpires) {
        this(username, password, token, tokenExpires, (Collection<GrantedAuthority>) toGrantedAuthorities("ROLE_USER"));
    }

    public UserDetailsImpl(String username, String password, String token, Date tokenExpires, Collection<? extends GrantedAuthority> authorities) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.tokenExpires = tokenExpires;
        this.authorities = Collections.unmodifiableCollection(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }

    public Date getTokenExpires() {
        return tokenExpires;
    }

    @Override
    public boolean isAccountNonExpired() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isAccountNonLocked() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCredentialsNonExpired() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEnabled() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static Collection<GrantedAuthority> toGrantedAuthorities(String... roles) {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>(roles.length);
        for (String role : roles) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role));
        }
        return grantedAuthorities;
    }
}
