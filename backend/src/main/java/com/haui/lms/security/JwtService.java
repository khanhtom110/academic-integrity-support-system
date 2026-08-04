package com.haui.lms.security;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.entity.InvalidatedToken;
import com.haui.lms.entity.User;
import com.haui.lms.exception.extended.AppException;
import com.haui.lms.repository.InvalidatedRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private long jwtAccessExpiration;

    @Value("${jwt.refresh.expiration}")
    private long jwtRefreshExpiration;

    private final InvalidatedRepository invalidatedRepository;

    public String generateToken(User user, boolean isRefresh) {
        try {
            long expirationTime = isRefresh ? jwtRefreshExpiration : jwtAccessExpiration;

            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder().subject(user.getEmail())
                    .issueTime(new Date()).expirationTime(new Date(System.currentTimeMillis() + expirationTime))
                    .jwtID(UUID.randomUUID().toString()).claim("isRefresh", isRefresh);

            if (!isRefresh) {
                claimsBuilder.claim("authorities", user.getRole()).claim("userId", user.getId());
            }

            // Ky
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsBuilder.build());
            signedJWT.sign(new MACSigner(secretKey.getBytes(StandardCharsets.UTF_8)));
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public String extractEmail(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new AppException(400, ErrorMessage.Auth.MALFORMED_TOKEN);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            // if (!isAccessToken(token)) {
            // return false;
            // }

            SignedJWT signedJWT = SignedJWT.parse(token);

            String jti = signedJWT.getJWTClaimsSet().getJWTID();

            if (invalidatedRepository.existsById(jti)) {
                return false;
            }

            // Kiem tra chu ky
            JWSVerifier verifier = new MACVerifier(secretKey.getBytes(StandardCharsets.UTF_8));
            boolean isSignatureValid = signedJWT.verify(verifier);

            // Kiem tra thoi gian
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            boolean isTokenExpired = expirationTime.before(new Date());

            // Kiem tra email
            String email = signedJWT.getJWTClaimsSet().getSubject();
            boolean isUsernameMatch = email != null && email.equals(userDetails.getUsername());

            return isSignatureValid && !isTokenExpired && isUsernameMatch;

        } catch (JOSEException | ParseException e) {
            return false;
        }
    }

    // Dua token invalid vao table db
    public void invalidateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            InvalidatedToken invalidatedToken = InvalidatedToken.builder().id(signedJWT.getJWTClaimsSet().getJWTID())
                    .expiryTime(signedJWT.getJWTClaimsSet().getExpirationTime()).build();

            invalidatedRepository.save(invalidatedToken);
        } catch (ParseException e) {
            throw new AppException(400, ErrorMessage.Auth.MALFORMED_TOKEN);
        }
    }

    // Check param la access token
    public boolean isAccessToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Boolean isRefresh = signedJWT.getJWTClaimsSet().getBooleanClaim("isRefresh");
            return isRefresh == null || !isRefresh;
        } catch (ParseException e) {
            return false;
        }
    }
}