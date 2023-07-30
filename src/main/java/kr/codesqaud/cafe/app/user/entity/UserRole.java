package kr.codesqaud.cafe.app.user.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRole {
	private Long id;
	private User user;
	private Role role;

	@Builder
	public UserRole(User user, Role role) {
		this.user = user;
		this.role = role;
	}
}
