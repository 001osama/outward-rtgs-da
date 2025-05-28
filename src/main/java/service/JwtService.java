package service;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;

import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;

import java.util.*;

import org.eclipse.microprofile.jwt.Claims;

import dto.UserInfoDto;



@Singleton
public class JwtService {
    

    public String generateToken(UserInfoDto userInfo) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("loginId", userInfo.getLoginId());
        claims.put("branchCode", userInfo.getBranchCode());
        claims.put("lastLoggedInDate", userInfo.getLastLoggedInDate());
        claims.put("currentLoggedInDate", userInfo.getCurrentLoggedInDate());
        claims.put("role", userInfo.getRole());

        String token = Jwt.claims(claims)
                .issuer("https://bahl.com/issuer")
                .upn("osamanaseem.io")
                .groups(new HashSet<>(Arrays.asList(userInfo.getRole())))
                .expiresIn(Duration.ofHours(2))
                .sign();
        return token;
    }

    private static PrivateKey getPrivateKey() {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("rsaPrivateKey.pem")) {
            if (is == null) {
                throw new FileNotFoundException("rsaPrivateKey.pem not found in classpath");
            }

            String key = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }
}
