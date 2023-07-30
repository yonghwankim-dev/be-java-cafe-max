package kr.codesqaud.cafe.app.user.controller.dto;

import java.util.Set;

import kr.codesqaud.cafe.app.user.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserVerifyResponseDto {
	private boolean isValid;
	private Set<Role> userRole;

	@Builder
	public UserVerifyResponseDto(boolean isValid, Set<Role> userRole) {
		this.isValid = isValid;
		this.userRole = userRole;
	}
}
