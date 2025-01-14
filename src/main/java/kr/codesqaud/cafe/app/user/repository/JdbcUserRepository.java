package kr.codesqaud.cafe.app.user.repository;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.errors.errorcode.UserErrorCode;
import kr.codesqaud.cafe.errors.exception.RestApiException;

@Profile(value = {"local", "dev", "prod"})
@Primary
@Repository
public class JdbcUserRepository implements UserRepository {

	private static final Logger logger = LoggerFactory.getLogger(JdbcUserRepository.class);

	private final JdbcTemplate template;

	@Autowired
	public JdbcUserRepository(JdbcTemplate template) {
		this.template = template;
	}

	@Override
	public List<User> findAll() {
		return template.query("SELECT * FROM users", userRowMapper());
	}

	@Override
	public Optional<User> findById(Long id) {
		List<User> users = template.query("SELECT * FROM users WHERE id = ?", userRowMapper(), id);
		return users.stream().findAny();
	}

	@Override
	public Optional<User> findByUserId(String userId) {
		List<User> users =
			template.query("SELECT * FROM users WHERE userId = ?", userRowMapper(), userId);
		return users.stream().findAny();
	}

	@Override
	public Optional<User> findByEmail(String email) {
		List<User> users =
			template.query("SELECT * FROM users WHERE email = ?", userRowMapper(), email);
		return users.stream().findAny();
	}

	@Override
	public User save(User user) {
		template.update("INSERT INTO users(userId, password, name, email) VALUES(?, ?, ?, ?)",
			user.getUserId(), user.getPassword(), user.getName(), user.getEmail());
		return findByUserId(user.getUserId()).orElseThrow();
	}

	@Override
	public User modify(User user) {
		template.update("UPDATE users SET name = ?, email = ? WHERE id = ?",
			user.getName(), user.getEmail(), user.getId());
		return user;
	}

	@Override
	public int deleteAll() {
		return template.update("DELETE FROM users");
	}

	private RowMapper<User> userRowMapper() {
		return (rs, rowNum) ->
			User.builder()
				.id(rs.getLong("id"))
				.userId(rs.getString("userId"))
				.password(rs.getString("password"))
				.name(rs.getString("name"))
				.email(rs.getString("email"))
				.build();
	}

	@Override
	public User findByRefreshToken(String refreshToken) {
		return template.query("SELECT * FROM users WHERE refreshToken = ?", userRowMapper(), refreshToken)
			.stream()
			.findAny()
			.orElseThrow(() -> new RestApiException(UserErrorCode.NOT_FOUND_USER));
	}

	@Override
	public void updateRefreshToken(Long id, String refreshToken) {
		int update = template.update("UPDATE users SET refreshToken = ? WHERE id = ?", refreshToken, id);
		logger.info("updateRefreshToken update query row : {}", update);
	}
}
