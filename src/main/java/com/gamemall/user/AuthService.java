package com.gamemall.user;

import com.gamemall.common.BizException;
import com.gamemall.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userMapper.findByUsername(request.username) != null) {
            throw new BizException("username already exists");
        }
        User user = new User();
        user.setUsername(request.username);
        user.setPasswordHash(passwordEncoder.encode(request.password));
        user.setNickname(request.nickname == null ? request.username : request.nickname);
        user.setRole("USER");
        user.setStatus(1);
        userMapper.insert(user);
        return token(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userMapper.findByUsername(request.username);
        if (user == null || user.getStatus() == 0 || !passwordEncoder.matches(request.password, user.getPasswordHash())) {
            throw new BizException(401, "invalid username or password");
        }
        return token(user);
    }

    private AuthResponse token(User user) {
        String token = jwtTokenProvider.createToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
