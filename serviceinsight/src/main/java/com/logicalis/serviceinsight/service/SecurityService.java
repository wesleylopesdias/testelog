package com.logicalis.serviceinsight.service;

/**
 *
 * @author poneil
 */
public interface SecurityService {
    public void sendPasswordReset(String email) throws ServiceException;
    public void updatePassword(String username, String newPassword) throws ServiceException;
    public String validateToken(String token) throws ServiceException;
    public String getApplicationLoginLink();
    public void checkUserAuthentication(String username, String oldPassword) throws ServiceException;
}
