package com.example.DATN.services;

import com.example.DATN.dtos.request.AuthenticationRequest;
import com.example.DATN.dtos.request.IntrospectRequest;
import com.example.DATN.dtos.request.LogoutRequest;
import com.example.DATN.dtos.request.RefreshRequest;
import com.example.DATN.dtos.respone.AuthenticationResponse;
import com.example.DATN.dtos.respone.IntrospectResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.InvalidateToken;
import com.example.DATN.models.User;
import com.example.DATN.repositories.CategoryRepository;
import com.example.DATN.repositories.InvalidateTokenRepository;
import com.example.DATN.repositories.UserRepository;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults  (level = lombok.AccessLevel.PRIVATE)
public class AuthenticationService {
    private final CategoryRepository categoryRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.valid-duration}")
    @NonFinal
    private long VALID_DURATION; // 1 hour in seconds

     @Value("${jwt.refreshable-duration}")
    private long REFRESHABLE_DURATION; // 7 days in seconds

    private final UserRepository userRepository;
    final InvalidateTokenRepository invalidateTokenRepository;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).
                orElseThrow(()-> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder=new BCryptPasswordEncoder(10);
        boolean isPasswordMatch= passwordEncoder.matches(request.getPassword(),user.getPassword());

        if(!isPasswordMatch) {
            throw new ApplicationException(ErrorCode.INVALID_PASSWORD);
        }
        String jwt = GenerateJWT(user);
        return AuthenticationResponse.builder()
                .token(jwt)
                .success(true)
                .message("Đăng nhập thành công")
                .build();
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
                .claim("scope",buildScope(user))
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(jwtSecret.getBytes()));
            return jwsObject.serialize();
        }
        catch (JOSEException e) {
            log.error("Error when sign jwt", e);
            throw new RuntimeException(e);
        }
    }
    private String buildScope(User user){
        StringJoiner stringJoiner=new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_"+role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
                }
            });
        }
        return stringJoiner.toString();
    }

    public void logout(LogoutRequest request)
            throws JOSEException, ParseException
    {
        try {
            var signToken = verifiedToken(request.getToken(), false);
            String jit=signToken.getJWTClaimsSet().getJWTID();
            Date expirationtime= signToken.getJWTClaimsSet().getExpirationTime();
            InvalidateToken invalidateToken=InvalidateToken.builder()
                    .id(jit)
                    .expiryTime(expirationtime)
                    .build();
            invalidateTokenRepository.save(invalidateToken);
        }
        catch (ApplicationException e){
            log.info("Token is already expired");
        }

    }
    private SignedJWT verifiedToken(String token,boolean isRefresh)
            throws JOSEException, ParseException{
        JWSVerifier verifier = new MACVerifier(jwtSecret.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);
        String id = signedJWT.getJWTClaimsSet().getJWTID();
        Date expirationTime = (isRefresh)
                ?new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                :signedJWT.getJWTClaimsSet().getExpirationTime();
        var verified = signedJWT.verify(verifier);
        if(!(verified && expirationTime.after(new Date()))){
            throw new ApplicationException(ErrorCode.EXPIRED_TOKEN);
        }
        if(invalidateTokenRepository.existsById(id)){
            throw new ApplicationException(ErrorCode.USER_NOT_EXISTED);
        }
        return signedJWT;
    }

    public AuthenticationResponse RefreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        //Kiểm tra token còn hợp lệ không
       var signedJWT = verifiedToken(request.getToken(),true);
       var jit = signedJWT.getJWTClaimsSet().getJWTID();
       var expiration =signedJWT.getJWTClaimsSet().getExpirationTime();
       InvalidateToken invalidateToken = InvalidateToken
               .builder()
               .id(jit)
               .expiryTime(expiration)
               .build();
       //lưu token vào invalid token
       invalidateTokenRepository.save(invalidateToken);
       //tìm username dựa vào claimset getsubject để lấy ra usernam để tìm user
       var username =signedJWT.getJWTClaimsSet().getSubject();
       User user =userRepository.findByUsername(username).
               orElseThrow(()-> new ApplicationException(ErrorCode.USER_NOT_EXISTED));
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
        boolean isValid=true;
        try {
            verifiedToken(token,false);
        }
        catch (ApplicationException e)
        {
            isValid=false;
        }
        return IntrospectResponse.builder()
                .active(isValid)
                .build();

    }
}
