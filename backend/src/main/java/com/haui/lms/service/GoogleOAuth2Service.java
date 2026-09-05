package com.haui.lms.service;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.response.GoogleUserInfoResponse;
import com.haui.lms.exception.extended.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth2Service {

    @Value("${app.oauth2.google.client-id}")
    private String clientId;

    @Value("${app.oauth2.google.client-secret}")
    private String clientSecret;

    @Value("${app.oauth2.google.redirect-uri}")
    private String redirectUri;

    @Value("${app.oauth2.google.token-uri}")
    private String tokenUri;

    @Value("${app.oauth2.google.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate;

    public GoogleUserInfoResponse getUserInfo(String code) {
        String googleAccessToken = exchangeCodeForAccessToken(code);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(googleAccessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GoogleUserInfoResponse> response = restTemplate.exchange(userInfoUri, HttpMethod.GET,
                    request, GoogleUserInfoResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }

            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        } catch (RestClientException e) {
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        }
    }

    private String exchangeCodeForAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUri, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String accessToken = (String) response.getBody().get("access_token");
                if (accessToken != null) {
                    return accessToken;
                }
            }
            log.error("Failed to exchange code for token. Response body: {}", response.getBody());
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        } catch (HttpClientErrorException e) {
            log.error("Google OAuth Token Error Response: {}", e.getResponseBodyAsString());
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        } catch (RestClientException e) {
            log.error("Error occurred while exchanging code for access token: ", e);
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        }
    }
}
