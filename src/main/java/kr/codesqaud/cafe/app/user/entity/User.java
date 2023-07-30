package kr.codesqaud.cafe.app.user.entity;

import java.util.HashSet;
import java.util.Set;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(of = "id")
@ToString
public class User {

	private final Long id;
	private final String userId;
	private final String password;
	private String name;
	private String email;
	private String refreshToken;
	private Set<UserRole> userRoles = new HashSet<>();

	@Builder
	public User(Long id, String userId, String password, String name, String email, String refreshToken) {
		this.id = id;
		this.userId = userId;
		this.password = password;
		this.name = name;
		this.email = email;
		this.refreshToken = refreshToken;
	}

	public void modify(User user) {
		this.name = user.name;
		this.email = user.email;
	}

	public void addRole(UserRole userRole) {
		userRoles.add(userRole);
	}

	public void updateRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
