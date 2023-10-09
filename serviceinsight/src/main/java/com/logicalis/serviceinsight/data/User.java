package com.logicalis.serviceinsight.data;

import java.util.List;

import com.logicalis.serviceinsight.web.config.CloudBillingSecurityConfig.Role;

public class User implements Comparable<User> {

	private Long id;
	private String name;
    private String username;
    private String password;
    private String title;
    private List<Role> profile;
    
    public User() {}
    
    public User(Long id, String name, String username, String password, String title, List<Role> profile) {
    	this.id = id;
    	this.name = name;
        this.username = username;
        this.password = password;
        this.title = title;
        this.profile = profile;
    }

    public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Role> getProfile() {
        return profile;
    }

    public void setProfile(List<Role> profile) {
        this.profile = profile;
    }

    @Override
    public int compareTo(User u) {
        if (getUsername() != null && (u != null)) {
            return getUsername().toLowerCase().compareTo(u.getUsername().toLowerCase());
        } else if (getUsername() != null) {
            return 1;
        }        
        if (u!=null) {
            return -1;
        }
        return 0;
    }

    @Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", username=" + username + ", password=" + password + ", title="
				+ title + ", profile=" + profile + "]";
	}

}
