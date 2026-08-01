package com.haui.lms.service;

import com.haui.lms.constant.ErrorMessage;
import com.haui.lms.dto.response.FacebookUserInfoResponse;
import com.haui.lms.exception.extended.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacebookOAuth2Service {

    @Value("${app.oauth2.facebook.client-id}")
    private String clientId;

    @Value("${app.oauth2.facebook.client-secret}")
    private String clientSecret;

    @Value("${app.oauth2.facebook.redirect-uri}")
    private String redirectUri;

    @Value("${app.oauth2.facebook.token-uri}")
    private String tokenUri;

    @Value("${app.oauth2.facebook.user-info-uri}")
    private String userInfoUri;

    private final RestTemplate restTemplate;

    public FacebookUserInfoResponse getUserInfo(String code) {
        // Exchange code to accessToken
        String accessToken = exchangeCodeForAccessToken(code);

        // Get information
        String url = UriComponentsBuilder.fromUriString(userInfoUri).queryParam("fields", "id,name,email,picture")
                .queryParam("access_token", accessToken).toUriString();

        try {
            FacebookUserInfoResponse userInfo = restTemplate.getForObject(url, FacebookUserInfoResponse.class);
            if (userInfo != null && userInfo.id() != null) {
                return userInfo;
            }
            log.error("Failed to fetch Facebook User Info: Response is null or missing ID.");
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);

        } catch (HttpClientErrorException e) {
            log.error("Facebook User Info HTTP Error: Status {}, Response: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        } catch (RestClientException e) {
            log.error("Error occurred while fetching Facebook User Info: ", e);
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        }
    }

    private String exchangeCodeForAccessToken(String code) {
        String url = UriComponentsBuilder.fromUriString(tokenUri).queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret).queryParam("redirect_uri", redirectUri)
                .queryParam("code", code).toUriString();

        try {
            var responseEntity = restTemplate.exchange(url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.get("access_token") instanceof String accessToken) {
                return accessToken;
            }

            log.error("Failed to exchange Facebook code for token. Response: {}", response);
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);

        } catch (HttpClientErrorException e) {
            log.error("Facebook OAuth Token Error Response: {}", e.getResponseBodyAsString());
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        } catch (RestClientException e) {
            log.error("Error occurred while exchanging Facebook code: ", e);
            throw new AppException(400, ErrorMessage.Auth.CHANGE_AUTHORIZATION_CODE_TO_TOKEN_FAIL);
        }
    }
}