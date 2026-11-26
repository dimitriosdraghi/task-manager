package com.alexandros.taskmanager.security;

import com.alexandros.taskmanager.entity.Role;
import com.alexandros.taskmanager.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class JwtServiceTest {

    private static final String SECRET = "test-secret-key-that-is-long-enough-1234567890";

    private final User alex = new User("alex", "hash", Role.USER);

    @Test
    void tokenContainsUsernameAndRole() {
        JwtService jwtService = new JwtService(SECRET, 3_600_000);

        String token = jwtService.generateToken(alex);
        Claims claims = jwtService.parseToken(token);

        assertThat(token.split("\\.")).hasSize(3);
        assertThat(claims.getSubject()).isEqualTo("alex");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtService ourService = new JwtService(SECRET, 3_600_000);
        JwtService otherService = new JwtService("a-completely-different-secret-key-0987654321", 3_600_000);

        String fakeToken = otherService.generateToken(alex);

        assertThatThrownBy(() -> ourService.parseToken(fakeToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwtService = new JwtService(SECRET, -1_000);

        String token = jwtService.generateToken(alex);

        assertThatThrownBy(() -> jwtService.parseToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
