package kr.codesqaud.cafe.app.user.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import kr.codesqaud.cafe.app.user.entity.UserRole;

@Repository
public class JdbcUserRoleRepository implements UserRoleRepository {
	private final NamedParameterJdbcTemplate template;

	public JdbcUserRoleRepository(NamedParameterJdbcTemplate template) {
		this.template = template;
	}

	@Override
	public void save(UserRole role) {
		MapSqlParameterSource source = new MapSqlParameterSource();
		source.addValue("userid", role.getUser().getId());
		source.addValue("role", role.getRole().name());
		template.update("INSERT INTO user_role (userid, role) VALUES(:userid, :role)", source);
	}
}
