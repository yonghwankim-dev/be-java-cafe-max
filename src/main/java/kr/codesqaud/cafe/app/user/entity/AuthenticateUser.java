package kr.codesqaud.cafe.app.user.entity;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateUser {
	private String userId;
	private Set<Role> roles;
}
