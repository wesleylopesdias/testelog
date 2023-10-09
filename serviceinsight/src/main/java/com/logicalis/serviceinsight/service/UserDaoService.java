package com.logicalis.serviceinsight.service;

import java.util.List;

import com.logicalis.serviceinsight.data.User;

public interface UserDaoService extends BaseService {
    public void saveUser(User user) throws ServiceException;
    public void updateUser(User user) throws ServiceException;
    public void deleteUser(Long id) throws ServiceException;
    public void disableUser(Long id) throws ServiceException;
    public void enableUser(Long id) throws ServiceException;
    public void generateNamesFromEmails();
    public User user(Long id);
    public List<User> users(Boolean enabled);    
}
