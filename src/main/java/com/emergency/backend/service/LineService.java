package com.emergency.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.*;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

@Service
public class LineService {

    @Value("${line.client-id}")
    private String clientId;

    @Value("${line.client-secret}")
    private String clientSecret;

    @Value("${line.redirect-uri}")
    private String redirectUri;
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getToken(String code) {
    RestTemplate rest = new RestTemplate();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("code", code);
    body.add("redirect_uri", redirectUri);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);

    HttpEntity<MultiValueMap<String, String>> req =
            new HttpEntity<>(body, headers);

    ResponseEntity<Map> res = rest.postForEntity(
        "https://api.line.me/oauth2/v2.1/token",
        req,
        Map.class
    );

    return (Map<String, Object>) res.getBody();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> decodeIdToken(String idToken) {
        try {
            String payload = idToken.split("\\.")[1];
            String decoded = new String(Base64.getUrlDecoder().decode(payload));
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(decoded, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Invalid LINE token");
        }
    }
}