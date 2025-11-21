package com.example.DATN.services;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import com.example.DATN.constant.AuthProvider;
import com.example.DATN.constant.PredefinedRole;
import com.example.DATN.dtos.request.jwt.*;
import com.example.DATN.dtos.respone.jwt.AuthenticationResponse;
import com.example.DATN.dtos.respone.jwt.IntrospectResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.helper.GetJwtIdForGuest;
import com.example.DATN.models.ForgotToken;
import com.example.DATN.models.InvalidateToken;
import com.example.DATN.models.Role;
import com.example.DATN.models.User;
import com.example.DATN.repositories.*;
import com.example.DATN.structure.MailStructure;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthenticationService {
    private final ForgotTokenRepository forgotTokenRepository;
    private final CategoryRepository categoryRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${jwt.valid-duration}")
    @NonFinal
    private long VALID_DURATION; // 1 hour in seconds

    @Value("${jwt.refreshable-duration}")
    private long REFRESHABLE_DURATION; // 7 days in seconds

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final SnowflakeIdGenerator snowflakeIdGenerator;
    private final CartService cartService;
    private final GetJwtIdForGuest getJwtIdForGuest;
    private RedisTemplate redisTemplate;
    final InvalidateTokenRepository invalidateTokenRepository;
    private final MailService mailService;

    @Transactional
    public void sendResetPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));

        String token = GenerateJwtForForgotPassword(request.getEmail());
        ForgotToken forgotToken = ForgotToken.builder()
                .email(request.getEmail())
                .token(token)
                .build();
        forgotTokenRepository.save(forgotToken);
        String url = "http://localhost:4200/reset-password?token=" + token;

        String htmlContent = """
                <p>Khôi phục mật khẩu của bạn</p>
                <p>Vui lòng nhấn vào link bên dưới để đặt lại mật khẩu:</p>
                
                <p>
                    <a href="%s" target="_blank" 
                       style="color:#1a73e8; font-weight:bold; text-decoration:none;">
                        Nhấn vào đây để reset mật khẩu
                    </a>
                </p>
                
                <br>
                <p>Trân trọng!</p>
                """.formatted(url);
        MailStructure mailStructure =
                MailStructure.builder()
                        .to(user.getEmail())
                        .subject("Khôi phục mật khẩu")
                        .content(htmlContent)
                        .build();
        // Gửi mail
        mailService.sendMail(mailStructure);

    }
//
//    public void sendOTP(ForgotPasswordRequest request)  {
//        ForgotToken forgotToken = forgotTokenRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
//        String otp = String.valueOf((int) ((Math.random() * 9 + 1) * 100000));
//        // Lưu OTP vào Redis với thời gian hết hạn là 5 phút
//        redisTemplate.opsForValue().set(forgotToken.getEmail(), otp, 5, java.util.concurrent.TimeUnit.MINUTES);
//        String htmlContent =
//                "Mã OTP của bạn là: " + otp + "\n Mã có hiệu lực trong 5 phút.";
//        MailStructure mailStructure =
//                MailStructure.builder()
//                        .to(forgotToken.getEmail())
//                        .subject("Mã OTP khôi phục mật khẩu")
//                        .content(htmlContent)
//                        .build();
//        mailService.sendMail(mailStructure);
//    }


    public void resetPassword(ResetPasswordRequest request) throws ParseException, JOSEException {
        ForgotToken forgotToken = forgotTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ApplicationException(ErrorCode.EXPIRED_TOKEN));

        verifiedToken(request.getToken(), false);
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new ApplicationException(ErrorCode.PASSWORD_CONFIRM_NOT_MATCH);
        }
        User user = userRepository.findByEmail(forgotToken.getEmail())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public AuthenticationResponse loginWithGoogle(String token) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(token);
        } catch (GeneralSecurityException | IOException e) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED, "Token verification failed.");
        }

        if (idToken == null) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED, "Invalid ID token.");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setId(snowflakeIdGenerator.nextId());
            newUser.setEmail(email);
            newUser.setUsername(email);
            newUser.setFirstName((String) payload.get("given_name"));
            newUser.setLastName((String) payload.get("family_name"));
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Generate a random password
            newUser.setProvider(AuthProvider.GOOGLE);
            Role userRole = roleRepository.findByName(PredefinedRole.USER.name())
                    .orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_FOUND));
            newUser.setRoles(Collections.singleton(userRole));

            return userRepository.save(newUser);
        });

        String jwt = GenerateJWT(user);
        return AuthenticationResponse.builder()
                .token(jwt)
                .success(true)
                .message("Đăng nhập bằng Google thành công")
                .build();
    }

    public AuthenticationResponse createGuestAndAuthenticate() {
        String jwt = GenerateJwtForGuest();
        return AuthenticationResponse.builder()
                .token(jwt)
                .success(true)
                .message("Guest account created successfully.")
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        //kiểm tra username có tồn tại không
        User user = userRepository.findByUsername(request.getUsername()).
                orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        //kiểm tra password có đúng không
        if (!isPasswordMatch) {
            throw new ApplicationException(ErrorCode.INVALID_PASSWORD);
        }
        //generate jwt
        String jwt = GenerateJWT(user);
        //trả lại jwt cho client
        return AuthenticationResponse.builder()
                .token(jwt)
                .success(true)
                .message("Đăng nhập thành công")
                .build();
    }

    public String GenerateJwtForGuest() {
        Role userRole = roleRepository.findByName(PredefinedRole.GUEST.name())
                .orElseThrow(() -> new ApplicationException(ErrorCode.ROLE_NOT_FOUND));
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + VALID_DURATION * 1000L);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject("guest_" + UUID.randomUUID().toString())
                .issuer("DATN.com")
                .issueTime(new Date())
                .expirationTime(expiryDate)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", GetPermissionForRole(userRole))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(jwtSecret.getBytes()));

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error when sign jwt", e);
            throw new RuntimeException(e);
        }
    }

    public String GenerateJwtForForgotPassword(String email) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + VALID_DURATION * 1000L);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject("email_" + email)
                .issuer("DATN.com")
                .issueTime(new Date())
                .expirationTime(expiryDate)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", "forgot_password")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(jwtSecret.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error when sign jwt", e);
            throw new RuntimeException(e);
        }
    }

    public String GetPermissionForRole(Role role) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        stringJoiner.add("ROLE_" + role.getName());
        role.getPermissions()
                .forEach(permission ->
                        stringJoiner.add(permission.getName()));
        return stringJoiner.toString();
    }

    public String GenerateJWT(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + VALID_DURATION * 1000L);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("DATN.com")
                .issueTime(new Date())
                .expirationTime(expiryDate)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(jwtSecret.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Error when sign jwt", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }

    public void logout(LogoutRequest request)
            throws JOSEException, ParseException {
        try {
            var signToken = verifiedToken(request.getToken(), false);
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expirationtime = signToken.getJWTClaimsSet().getExpirationTime();
            InvalidateToken invalidateToken = InvalidateToken.builder()
                    .id(jit)
                    .expiryTime(expirationtime)
                    .build();
            invalidateTokenRepository.save(invalidateToken);
        } catch (ApplicationException e) {
            log.info("Token is already expired");
        }

    }

    private SignedJWT verifiedToken(String token, boolean isRefresh)
            throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        String id = signedJWT.getJWTClaimsSet().getJWTID();
        Date expirationTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if (!(verified && expirationTime.after(new Date()))) {
            throw new ApplicationException(ErrorCode.EXPIRED_TOKEN);
        }
        if (invalidateTokenRepository.existsById(id)) {
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        return signedJWT;
    }

    public AuthenticationResponse RefreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        var signedJWT = verifiedToken(request.getToken(), true);
        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
        InvalidateToken invalidateToken = InvalidateToken
                .builder()
                .id(jit)
                .expiryTime(expiration)
                .build();
        //lưu token vào invalid token
        invalidateTokenRepository.save(invalidateToken);
        //tìm username dựa vào claimset getsubject để lấy ra usernam để tìm user
        var username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        //tạo token mới từ user moi làm ra
        var token = GenerateJWT(user);
        return AuthenticationResponse
                .builder()
                .token(token)
                .success(true)
                .message("Refresh token thành công")
                .build();
    }

    public IntrospectResponse Introspect
            (IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        try {
            verifiedToken(token, false);
        } catch (ApplicationException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .active(isValid)
                .build();

    }
}