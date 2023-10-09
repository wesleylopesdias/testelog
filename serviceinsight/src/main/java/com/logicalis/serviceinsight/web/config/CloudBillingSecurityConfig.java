package com.logicalis.serviceinsight.web.config;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 *
 * @author poneil
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class CloudBillingSecurityConfig extends WebSecurityConfigurerAdapter {

    public enum Role {
        ROLE_USER("User"),
        ROLE_SDM("SDM"),
        ROLE_BSC("Business Solution Consultant"),
        ROLE_EPE("Enterprise Program Executive"),
        ROLE_AE("Account Executive"),
        ROLE_BILLING("Billing"),
        ROLE_MANAGER("Manager"),
        ROLE_ADMIN("Admin"),
        ROLE_API("API"),
        ROLE_ACT_USER("Activation User"),
        ROLE_ACT_ADMIN("Activation Admin"),
        ROLE_ACT_SIGNER("Activation Document Signer"),
        ROLE_ACT_LC_ENGINEER("Activation Launch Center Engineer"),
        ROLE_MC_ADMIN("Mission Control Admin");
        
        private String description;
        
        Role(String description){
            this.description = description;
        }
        
        public String getDescription() {
            return this.description;
        }
    }
    
    @Autowired
    DataSource dataSource;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .jdbcAuthentication()
                .dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery("select username, password, enabled from users where username = ?")
                .authoritiesByUsernameQuery("select users.username as username, authorities.authority as authority" + 
                		" from users inner join authorities on users.id = authorities.user_id where users.username = ?");
    }

    @Bean
    public Md5PasswordEncoder passwordEncoder() {
        return new Md5PasswordEncoder();
    }
    
    @Bean StandardPasswordEncoder standardPasswordEncoder() {
        return new StandardPasswordEncoder();
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {    	
        http
                .csrf().disable()
                .authorizeRequests()
                .accessDecisionManager(accessDecisionManager())
                .expressionHandler(expressionHandler())
                .antMatchers("/resources/**").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/settings/**").hasRole("ADMIN")
                .antMatchers("/myaccount/**").permitAll()
                .antMatchers("/data/**").hasRole("USER")
                .antMatchers("/contracts/**").hasRole("USER")
                .antMatchers("/contractservices/**").hasRole("USER")
                .antMatchers("/contractinvoices/**").hasRole("USER")
                .antMatchers("/contractupdates/**").hasRole("USER")
                .antMatchers("/customers/**").hasRole("USER")
                .antMatchers("/services/**").hasRole("USER")
                .antMatchers("/reports/**").hasRole("USER")
                .antMatchers("/api/**").hasRole("ADMIN")
                .antMatchers("/awsapis/**").hasRole("USER")
                //.antMatchers("/api/**").hasRole("USER")
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login").permitAll()
                .and()
                .logout().permitAll()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .and()
                .exceptionHandling().accessDeniedHandler(unauthorized())
                .and()
                .httpBasic()
                .and()
                .headers()
                .frameOptions()
                .sameOrigin();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_MANAGER and ROLE_MANAGER > ROLE_BILLING and ROLE_BILLING > ROLE_USER and ROLE_SDM > ROLE_USER and ROLE_AE > ROLE_USER");
        return roleHierarchy;
    }
    
    @Bean
    public RoleHierarchyVoter roleVoter() {
        return new RoleHierarchyVoter(roleHierarchy());
    }
    
    @Bean 
    public DefaultWebSecurityExpressionHandler expressionHandler(){
        DefaultWebSecurityExpressionHandler expressionHandler = new DefaultWebSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy());
        return expressionHandler;
    }

    @Bean
    public AffirmativeBased accessDecisionManager() {       
        List<AccessDecisionVoter<? extends Object>> decisionVoters = new ArrayList();
        WebExpressionVoter webExpressionVoter = new WebExpressionVoter();
        webExpressionVoter.setExpressionHandler(expressionHandler());
        decisionVoters.add(webExpressionVoter);
        decisionVoters.add(roleVoter());
        return new AffirmativeBased(decisionVoters);
    }
    
    @Override
    public void init(WebSecurity web) throws Exception {
        web.expressionHandler(expressionHandler());
        super.init(web);
    }

    private AccessDeniedHandler unauthorized() {
        return new ApplicationAccessDeniedHandler("/unauthorized");
    }
}
