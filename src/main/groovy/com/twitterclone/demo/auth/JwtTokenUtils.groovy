package com.twitterclone.demo.auth

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys

import java.security.Key

class JwtTokenUtils {

    private static final String SECRET_KEY = "suPEr-hjGEFG*&_secret%for*super-keyAaBbFgE&&&%*#"
    private static final long EXPIRATION_TIME = 30 * 60 * 1000

    static String generateToken(String username) {
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.bytes)
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact()
    }

    static String getUsernameFromToken(String token) throws ExpiredJwtException, MalformedJwtException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.bytes)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }


    static boolean isTokenValid(String token) throws ExpiredJwtException, MalformedJwtException {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.bytes)
                .build()
                .parseClaimsJws(token)
                .body
        return claims.getExpiration().after(new Date())
    }
}
