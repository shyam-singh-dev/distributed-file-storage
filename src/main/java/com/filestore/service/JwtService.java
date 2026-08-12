package com.filestore.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.security.Keys;


import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j

public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    // Generate token

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(),userDetails);

    }

    public String generateToken(
            Map<String,Object> extraClaims,UserDetails userDetails
    ) {
        log.info("Generating JWT token for : {}", userDetails.getUsername());

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

        // Validate token

        public boolean isTokenValid(String token,UserDetails userDetails) {
        final String username = extractUsername(token);
        boolean isValid = username.equals(userDetails.getUsername())
                &&  !isTokenExpired(token);

        log.info("Token valid for {} : {} ",username,isValid);
        return isValid;
        }
        private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
        }

        // extract data from token

    public String extractUsername(String token) {
        return extractClaim(token,Claims::getSubject);
    }
    private Date  extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token , Function<Claims,T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);

    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // SIGNING KEY

    private SecretKey getSigningKey() {
        byte [] keyBytes = hexStringToByteArray(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte [] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i<len; i+=2) {
            data[i/2] = (byte) (
                    ( Character.digit(hex.charAt(i),16) << 4
                    ) + Character.digit(hex.charAt(i+1),16));
        }
        return data;
    }
}
