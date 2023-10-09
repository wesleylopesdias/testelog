package com.logicalis.serviceinsight.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;

import com.logicalis.serviceinsight.data.User;
import com.logicalis.serviceinsight.web.config.CloudBillingSecurityConfig.Role;

@org.springframework.stereotype.Service
public class UserDaoServiceImpl extends BaseServiceImpl implements UserDaoService {

    @Autowired
    private Md5PasswordEncoder passwordEncoder;

    private Integer verifyUserExists(String username) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from users where username = ?", Integer.class, username);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("user_not_found_for_username", new Object[]{username}, LocaleContextHolder.getLocale()));
        }
        return count;
    }
    
    private Integer verifyUserExistsById(Long id) throws ServiceException {
        Integer count = jdbcTemplate.queryForObject("select count(*) from users where id = ?", Integer.class, id);
        if (!count.equals(1)) {
            throw new ServiceException(messageSource.getMessage("user_not_found_for_userid", new Object[]{id}, LocaleContextHolder.getLocale()));
        }
        return count;
    }

    @Override
    public void saveUser(User user) throws ServiceException {
        if (StringUtils.isBlank(user.getUsername())) {
            throw new ServiceException(messageSource.getMessage("validation_error_username", null, LocaleContextHolder.getLocale()));
        }
        if (StringUtils.isBlank(user.getName())) {
            throw new ServiceException(messageSource.getMessage("validation_error_user_name", null, LocaleContextHolder.getLocale()));
        }
        if (StringUtils.isBlank(user.getPassword())) {
            throw new ServiceException(messageSource.getMessage("validation_error_user_password", null, LocaleContextHolder.getLocale()));
        }
        Integer count = jdbcTemplate.queryForObject("select count(*) from users where username = ?", Integer.class, user.getUsername());
        if (count > 0) {
            throw new ServiceException(messageSource.getMessage("user_already_exists_with_username", new Object[]{user.getUsername()}, LocaleContextHolder.getLocale()));
        }
        try {
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("users").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("username", user.getUsername());
            params.put("name", user.getName());
            params.put("title", user.getTitle());
            params.put("password", passwordEncoder.encodePassword(user.getPassword(), null));
            params.put("enabled", Boolean.TRUE);
            //int rows = jdbcInsert.execute(new MapSqlParameterSource(params));
            Long userId = (Long)jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));
            if (userId != null) {
                // add new authority roles
                jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
                jdbcInsert.withTableName("authorities").usingGeneratedKeyColumns("id");
                for (Role role : user.getProfile()) {
                    params.clear();
                    params.put("user_id", userId);
                    //params.put("username", user.getUsername());
                    params.put("authority", role);
                    int rows = jdbcInsert.execute(new MapSqlParameterSource(params));
                    if (rows != 1) {
                        throw new ServiceException(messageSource.getMessage("jdbc_error_authority_insert", new Object[]{role,user.getUsername()}, LocaleContextHolder.getLocale()));                        
                    }
                }
            }
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_user_insert", new Object[]{user.getUsername(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @Override
    public void updateUser(User user) throws ServiceException {
        verifyUserExistsById(user.getId());
        
        if (StringUtils.isBlank(user.getName())) {
            throw new ServiceException(messageSource.getMessage("validation_error_user_name", null, LocaleContextHolder.getLocale()));
        }
        
        User dbUser = user(user.getId());
        try {        	
            // add new authority roles
            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
            jdbcInsert.withTableName("authorities").usingGeneratedKeyColumns("id");
            Map<String, Object> params = new HashMap<String, Object>();
            int rows = 0;
            for (Role role : user.getProfile()) {
                if (!dbUser.getProfile().contains(role)) {
                    params.clear();
                    params.put("user_id", user.getId());
                    //params.put("username", user.getUsername());
                    params.put("authority", role);
                    rows = jdbcInsert.execute(new MapSqlParameterSource(params));
                    if (rows != 1) {
                        throw new ServiceException(messageSource.getMessage("jdbc_error_authority_insert", new Object[]{role, user.getUsername()}, LocaleContextHolder.getLocale()));                        
                    }
                }
            }
            // remove unassigned authority roles
            String jdbcDelete = "delete from authorities where user_id = ? and authority = ?";
            for (Role dbRole : dbUser.getProfile()) {
                if (!user.getProfile().contains(dbRole)) {
                    rows = jdbcTemplate.update(jdbcDelete, new Object[]{user.getId(),dbRole.toString()});
                    if (rows != 1) {
                        throw new ServiceException(messageSource.getMessage("jdbc_error_authority_insert", new Object[]{dbRole, dbUser.getUsername()}, LocaleContextHolder.getLocale()));
                    }
                }
            }
            
            int updated = jdbcTemplate.update("update users set name = ?, title = ? where id = ?",
                    new Object[]{user.getName(), user.getTitle(), user.getId()});
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_user_update", new Object[]{user.getUsername(), any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    public void generateNamesFromEmails() {
    	List<User> users = users(Boolean.TRUE);
    	for(User user: users) {
    		if(StringUtils.isEmpty(user.getName())) {
    			generateNameFromEmail(user);
    		}
    	}
    	
    	users = users(Boolean.FALSE);
    	for(User user: users) {
    		if(StringUtils.isEmpty(user.getName())) {
    			generateNameFromEmail(user);
    		}
    	}
    }
    
    private void generateNameFromEmail(User user) {
    	try {
    		
			String name = "";
			String[] initialPart = user.getUsername().split("@");
			if(initialPart[0] != null) {
				String[] nameParts = initialPart[0].split("\\.");
				if(nameParts != null && nameParts.length > 0) {
					String firstName = nameParts[0];
					name = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
					if(nameParts.length > 1) {
						String lastName = nameParts[1];
						name += " " + lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
					}
				}
			}
			
			if(!StringUtils.isEmpty(name)) {
				user.setName(name);
				updateUser(user);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
    }
    
    @Override
    public void deleteUser(Long id) throws ServiceException {
        try {
            int aRows = jdbcTemplate.update("delete from authorities where user_id = ?", new Object[]{id});
            int uRow = jdbcTemplate.update("delete from users where id = ?", new Object[]{id});
            log.debug("Deleted User ID : " + id + " [" + uRow + "] with " + aRows + " authorities.");
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_user_delete", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);            
        }
    }

    @Override
    public void disableUser(Long id) throws ServiceException {
        verifyUserExistsById(id);
        try {
            int row = jdbcTemplate.update("update users set enabled = false where id = ?",new Object[]{id});
            if (row != 1) {
                throw new ServiceException(messageSource.getMessage("jdbc_error_user_disable", new Object[]{id, "unexpected result"}, LocaleContextHolder.getLocale()));                
            }
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_user_disable", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }

    @Override
    public void enableUser(Long id) throws ServiceException {
        verifyUserExistsById(id);
        try {
            int row = jdbcTemplate.update("update users set enabled = true where id = ?", new Object[]{id});
            if (row != 1) {
                throw new ServiceException(messageSource.getMessage("jdbc_error_user_enable", new Object[]{id, "unexpected result"}, LocaleContextHolder.getLocale()));                
            }
        } catch (Exception any) {
            throw new ServiceException(messageSource.getMessage("jdbc_error_user_enable", new Object[]{id, any.getMessage()}, LocaleContextHolder.getLocale()), any);
        }
    }
    
    @Override
    public User user(Long id) {
        String query = "select u.id user_id, u.username, u.name, u.password, u.title, u.token, u.token_expires, au.authority"
                + " from users u"
                + " left join authorities au on au.user_id = u.id"
                + " where u.id = ? and u.enabled = true";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query, id);
        User user = null;
        if (results != null && results.size() > 0) {
            Map<String, Object> userdata = null;
            for (int counter = 0; counter < results.size(); counter++) {
                userdata = results.get(counter);
                if (user == null) {
                    user = new User(
                    		(Long) userdata.get("user_id"),
                    		(String) userdata.get("name"),
                            (String) userdata.get("username"),
                            (String) userdata.get("password"),
                            (String) userdata.get("title"),
                            new ArrayList<Role>());
                }
                if (!StringUtils.isBlank((String)userdata.get("authority"))) {
                    user.getProfile().add(Role.valueOf((String)userdata.get("authority")));
                }
            }
        }
        return user;
    }

    @Override
    public List<User> users(Boolean enabled) {
        Map<String,User> userMap = new HashMap<String,User>();
        List<User> users;
        String query = "select u.id user_id, u.username, u.name, u.password, u.title, u.token, u.token_expires, au.authority"
                + " from users u"
                + " left join authorities au on au.user_id = u.id"
                + " where u.enabled = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query, new Object[]{enabled});
        if (results != null && results.size() > 0) {
            Map<String, Object> userdata = null;
            User user;
            String key;
            for (int counter = 0; counter < results.size(); counter++) {
                userdata = results.get(counter);
                key = (String) userdata.get("username");
                if (userMap.containsKey(key)) {
                    user = userMap.get(key);
                } else {
                    user = new User(
                    		(Long) userdata.get("user_id"),
                    		(String) userdata.get("name"),
                    		key,
                            (String) userdata.get("password"),
                            (String) userdata.get("title"),
                            new ArrayList<Role>());
                    userMap.put(key, user);
                }
                if (!StringUtils.isBlank((String)userdata.get("authority"))) {
                    user.getProfile().add(Role.valueOf((String)userdata.get("authority")));
                    userMap.put(key, user);
                }
            }
        }
        users = (new ArrayList<User>(userMap.values()));
        Collections.sort(users);
        return users;
    }

}
