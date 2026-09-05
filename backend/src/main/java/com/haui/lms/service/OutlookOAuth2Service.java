package com.haui.lms.service;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.response.OutlookTokenResponse;
import com.haui.lms.dto.response.OutlookUserResponse;
import com.haui.lms.exception.extended.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutlookOAuth2Service {
    private final RestTemplate restTemplate;

    @Value("${app.oauth2.outlook.client-id}")
    private String clientId;

    @Value("${app.oauth2.outlook.client-secret}")
    private String clientSecret;

    @Value("${app.oauth2.outlook.redirect-uri}")
    private String redirectUri;

    @Value("${app.oauth2.outlook.token-uri}")
    private String tokenUrl;

    @Value("${app.oauth2.outlook.user-info-uri}")
    private String userInfoUrl;

    public OutlookUserResponse getUserProfile(String code) {
        String accessToken = exchangeCodeForToken(code);

        // get profile
        return fetchUserProfile(accessToken);
    }

    private String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", code);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");
        params.add("scope", "openid profile email User.Read");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<OutlookTokenResponse> response = restTemplate.postForEntity(tokenUrl, request,
                    OutlookTokenResponse.class);

            if (response.getBody() == null || response.getBody().accessToken() == null) {
                throw new AppException(400, ErrorMessage.Auth.CANNOT_CONVERT_OAUTH_CODE);
            }
            return response.getBody().accessToken();
        } catch (RestClientResponseException e) {
            log.error("Microsoft API error status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(400, ErrorMessage.Auth.CANNOT_CONVERT_OAUTH_CODE);
        } catch (Exception e) {
            log.error("Error exchanging Microsoft OAuth token: {}", e.getMessage(), e);
            throw new AppException(400, ErrorMessage.Auth.CANNOT_CONVERT_OAUTH_CODE);
        }
    }

    private OutlookUserResponse fetchUserProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<OutlookUserResponse> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request,
                    OutlookUserResponse.class);

            OutlookUserResponse user = response.getBody();
            if (user == null || user.getEmail() == null) {
                throw new AppException(400, ErrorMessage.Auth.OUTLOOK_EMAIL_MISSING);
            }
            return user;
        } catch (RestClientResponseException e) {
            log.error("Microsoft Graph API error status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AppException(400, ErrorMessage.Auth.CANNOT_FETCH_OAUTH_USER_INFO);
        } catch (Exception e) {
            log.error("Error fetching Microsoft user profile: {}", e.getMessage(), e);
            throw new AppException(400, ErrorMessage.Auth.CANNOT_FETCH_OAUTH_USER_INFO);
        }
    }
}
