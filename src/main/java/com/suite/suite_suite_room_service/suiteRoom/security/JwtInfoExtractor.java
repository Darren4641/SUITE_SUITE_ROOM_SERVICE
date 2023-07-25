package com.suite.suite_suite_room_service.suiteRoom.security;

import com.suite.suite_suite_room_service.suiteRoom.config.ConfigUtil;
import com.suite.suite_suite_room_service.suiteRoom.security.dto.AuthorizerDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class JwtInfoExtractor {

    private final ConfigUtil configUtil;

    public Claims extractToken(String token) {
        return Jwts.parser().setSigningKey(configUtil.getProperty("jwt.access.key").getBytes()).parseClaimsJws(token).getBody();
    }

    public static AuthorizerDto getSuiteAuthorizer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            Claims claims = ((JwtAuthenticationToken) authentication).getClaims();



            return AuthorizerDto.builder()
                    .memberId(Long.parseLong(claims.get(AuthorizerDto.ClaimName.ID.getValue()).toString()))
                    .email(authentication.getName())
                    .name(claims.get(AuthorizerDto.ClaimName.NAME.getValue()).toString())
                    .nickName(claims.get(AuthorizerDto.ClaimName.NICKNAME.getValue()).toString())
                    .accountStatus(claims.get(AuthorizerDto.ClaimName.ACCOUNTSTATUS.getValue()).toString())
                    .role(AuthorizerDto.ClaimName.ROLE.getValue())
                    .build();

        }
        return null;
    }
}