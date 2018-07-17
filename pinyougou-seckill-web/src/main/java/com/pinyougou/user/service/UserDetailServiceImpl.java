package com.pinyougou.user.service;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailServiceImpl implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("through:"+username);
		//构建角色集合
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		//ROLE_USER如果有用户角色表的话,需要从用户角色表中查询出username相对应的角色
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		
		return new User(username, "", authorities);
	}

}
