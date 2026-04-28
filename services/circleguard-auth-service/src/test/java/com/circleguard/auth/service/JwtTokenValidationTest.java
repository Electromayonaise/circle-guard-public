package com.circleguard.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Key;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenValidationTest {

    private static final String SECRET = "my-super-secret-dev-key-32-chars-long-12345678";

    @Test
    void shouldSetAnonymousIdAsTokenSubject() {
        JwtTokenService service = new JwtTokenService(SECRET, 3600000L);
        UUID anonymousId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken("user", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = service.generateToken(anonymousId, auth);

        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

        assertEquals(anonymousId.toString(), claims.getSubject());
    }

    @Test
    void shouldIncludeGrantedAuthoritiesInPermissionsClaim() {
        JwtTokenService service = new JwtTokenService(SECRET, 3600000L);
        UUID anonymousId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken("admin", null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("alert:receive_priority")));

        String token = service.generateToken(anonymousId, auth);

        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();

        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) claims.get("permissions");
        assertTrue(permissions.contains("ROLE_ADMIN"));
        assertTrue(permissions.contains("alert:receive_priority"));
    }

    @Test
    void shouldProduceTokenRejectedByParserWhenExpired() {
        JwtTokenService service = new JwtTokenService(SECRET, -1000L);
        UUID anonymousId = UUID.randomUUID();
        var auth = new UsernamePasswordAuthenticationToken("user", null, List.of());

        String token = service.generateToken(anonymousId, auth);

        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        assertThrows(ExpiredJwtException.class, () ->
                Jwts.parserBuilder().setSigningKey(key).build()
                        .parseClaimsJws(token).getBody());
    }
}
