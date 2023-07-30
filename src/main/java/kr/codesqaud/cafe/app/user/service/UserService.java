package kr.codesqaud.cafe.app.user.service;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import kr.codesqaud.cafe.app.user.controller.dto.UserLoginRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserResponse;
import kr.codesqaud.cafe.app.user.controller.dto.UserSavedRequest;
import kr.codesqaud.cafe.app.user.controller.dto.UserVerifyResponseDto;
import kr.codesqaud.cafe.app.user.entity.AuthenticateUser;
import kr.codesqaud.cafe.app.user.entity.Role;
import kr.codesqaud.cafe.app.user.entity.User;
import kr.codesqaud.cafe.app.user.entity.UserRole;
import kr.codesqaud.cafe.app.user.filter.VerifyUserFilter;
import kr.codesqaud.cafe.app.user.repository.UserRepository;
import kr.codesqaud.cafe.app.user.repository.UserRoleRepository;
import kr.codesqaud.cafe.app.user.validator.UserValidator;
import kr.codesqaud.cafe.errors.errorcode.UserErrorCode;
import kr.codesqaud.cafe.errors.exception.ResourceNotFoundException;
import kr.codesqaud.cafe.errors.exception.RestApiException;
import kr.codesqaud.cafe.jwt.Jwt;
import kr.codesqaud.cafe.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final UserValidator validator;
	private final JwtProvider jwtProvider;
	private final ObjectMapper objectMapper;

	// 전체 회원 목록
	public List<UserResponse> getAllUsers() {
		return userRepository.findAll().stream()
			.map(UserResponse::new)
			.collect(Collectors.toUnmodifiableList());
	}

	// 회원가입
	@Transactional
	public UserResponse signUp(UserSavedRequest userRequest) {
		validateDuplicatedUserId(userRequest.getUserId());
		validateDuplicatedUserEmail(userRequest.getEmail());
		User savedUser = userRepository.save(userRequest.toEntity());
		UserRole role = UserRole.builder()
			.role(Role.USER)
			.user(savedUser)
			.build();
		savedUser.addRole(role);
		userRoleRepository.save(role);
		return new UserResponse(savedUser);
	}

	// 회원 아이디 중복 검증
	private void validateDuplicatedUserId(String userId) {
		userRepository.findByUserId(userId).ifPresent((user) -> {
			throw new RestApiException(UserErrorCode.ALREADY_EXIST_USERID);
		});
	}

	// 회원 이메일 중복 검증
	private void validateDuplicatedUserEmail(String email) {
		logger.debug("userRepository : {}", userRepository);
		logger.debug("count : {}", userRepository.findAll().size());
		userRepository.findByEmail(email).ifPresent((user) -> {
			throw new RestApiException(UserErrorCode.ALREADY_EXIST_EMAIL);
		});
	}

	// 특정 회원 조회
	public UserResponse findUser(Long id) {
		User findUser = userRepository.findById(id).orElseThrow(() -> {
			throw new ResourceNotFoundException(UserErrorCode.NOT_FOUND_USER);
		});
		return new UserResponse(findUser);
	}

	// 특정 회원 조회
	public UserResponse findUser(String userId) {
		User findUser = userRepository.findByUserId(userId).orElseThrow(() -> {
			throw new ResourceNotFoundException(UserErrorCode.NOT_FOUND_USER);
		});
		return new UserResponse(findUser);
	}

	// 로그인
	public User login(UserLoginRequest requestDto) {
		User loginUser = requestDto.toEntity();
		User user = userRepository.findByUserId(loginUser.getUserId()).orElseThrow(() -> {
			throw new RestApiException(UserErrorCode.NOT_MATCH_LOGIN);
		});
		validator.validateLoginPassword(loginUser.getPassword(), user.getPassword());
		return user;
	}

	// 회원 정보 수정
	@Transactional
	public UserResponse modifyUser(Long id, UserSavedRequest userRequest) {
		User originalUser = userRepository.findById(id).orElseThrow();
		// 기존 이메일과 수정하고자 하는 이메일이 같지 않다면 수정하고자 하는 이메일이 중복되지 않았는지 검증합니다.
		if (!validator.isEmailUnChanged(originalUser.getEmail(), userRequest.getEmail())) {
			validateDuplicatedUserEmail(userRequest.getEmail());
		}
		validator.validateEqualConfirmPassword(userRequest.getPassword(),
			originalUser.getPassword());
		originalUser.modify(userRequest.toEntity());
		return new UserResponse(userRepository.modify(originalUser));
	}

	public UserVerifyResponseDto verifyUser(UserLoginRequest userLoginRequest) {
		User user = userRepository.findByUserId(userLoginRequest.getUserId()).orElse(null);
		logger.info("findByUserId : {}", user);
		if (user == null) {
			return UserVerifyResponseDto.builder()
				.isValid(false)
				.build();
		}
		return UserVerifyResponseDto.builder()
			.isValid(true)
			.userRole(user.getUserRoles().stream().map(UserRole::getRole).collect(Collectors.toSet()))
			.build();
	}

	@Transactional
	public void updateRefreshToken(String userId, String refreshToken) {
		User user = userRepository.findByUserId(userId).orElse(null);
		if (user == null) {
			return;
		}
		userRepository.updateRefreshToken(user.getId(), refreshToken);
	}

	@Transactional
	public Jwt refreshToken(String refreshToken) {
		try {
			// 유효한 토큰인지 확인
			jwtProvider.getClaims(refreshToken);
			User user = userRepository.findByRefreshToken(refreshToken);
			if (user == null) {
				return null;
			}
			HashMap<String, Object> claims = new HashMap<>();
			AuthenticateUser authenticateUser = new AuthenticateUser(user.getUserId(),
				user.getUserRoles().stream().map(UserRole::getRole).collect(Collectors.toSet()));
			String authenticateUserJson = objectMapper.writeValueAsString(authenticateUser);
			claims.put(VerifyUserFilter.AUTHENTICATE_USER, authenticateUserJson);
			Jwt jwt = jwtProvider.createJwt(claims);
			updateRefreshToken(user.getUserId(), jwt.getRefreshToken());
			return jwt;
		} catch (Exception e) {
			logger.error("error : {}", e.getMessage());
			return null;
		}
	}

	public boolean addUserRole(String userid, Role role) {
		User user = userRepository.findByUserId(userid)
			.orElseThrow(() -> new RestApiException(UserErrorCode.NOT_FOUND_USER));
		if (user.getUserRoles().stream().anyMatch(userRole -> userRole.getRole().equals(role))) {
			return false;
		}
		UserRole userRole = UserRole.builder()
			.user(user)
			.role(role)
			.build();
		user.addRole(userRole);
		userRoleRepository.save(userRole);
		return true;
	}

}
