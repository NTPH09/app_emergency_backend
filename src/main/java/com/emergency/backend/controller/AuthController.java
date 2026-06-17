package com.emergency.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.emergency.backend.entity.Provider;
import com.emergency.backend.entity.User;
import com.emergency.backend.service.AuthService;
import java.util.List;
import com.emergency.backend.repository.UserProviderRepository;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    // private final UserProviderRepository userProviderRepository;

    public AuthController(AuthService authService, UserProviderRepository userProviderRepository) {
        this.authService = authService;
        // this.userProviderRepository = userProviderRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            authService.register(user);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Register success"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String token = authService.login(
                body.get("username"),
                body.get("password")
            );
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "token", token
            ));
        } catch (RuntimeException e) {
            // ✅ ใช้ Spring HttpStatus ได้ถูกต้อง
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Invalid token");
            }

            String token = authHeader.substring(7);
            User user = authService.getUserFromToken(token);

            Map<String, Object> res = new HashMap<>();
            res.put("id", user.getId());
            res.put("phone", user.getPhone());
            res.put("email", user.getEmail());
            res.put("firstname", user.getFirstname());
            res.put("lastname", user.getLastname());
            res.put("dateOfBirth", user.getDateOfBirth());
            res.put("hasPassword", user.getPassword() != null);
            res.put("avatar", user.getAvatar());
            res.put("photoUrl", user.getPhotoUrl());
            res.put("useAvatar", user.getUseAvatar());

            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/firebase-login")
    public ResponseEntity<?> firebaseLogin(@RequestBody Map<String, String> body) {
        try {
            String firebaseToken = body.get("token");
            String photoUrl = body.get("photoUrl"); 

            if (firebaseToken == null || firebaseToken.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Token is missing"
                ));
            }

            String jwt = authService.loginWithFirebase(firebaseToken, photoUrl);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "token", jwt
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/dev-login")
    public ResponseEntity<?> devLogin(@RequestBody Map<String, String> body) {
        try {
            String phone = body.get("phone");

            if (phone == null || phone.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Phone is missing"
                ));
            }

            String jwt = authService.loginWithPhone(phone);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "token", jwt
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        try {
            authService.changePassword(authHeader, body.get("oldPassword"), body.get("newPassword"));
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Password changed"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/set-password")
    public ResponseEntity<?> setPassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        try {
            authService.setPassword(authHeader, body.get("newPassword"));
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "ตั้งรหัสผ่านสำเร็จ"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/avatar")
    public ResponseEntity<?> updateAvatar(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body) {
        // ✅ authService.updateAvatar เป็น void แล้ว
        authService.updateAvatar(token.replace("Bearer ", ""), body.get("avatar"));
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PutMapping("/use-google-photo")
    public ResponseEntity<?> useGooglePhoto(
            @RequestHeader("Authorization") String authHeader) {
        try {
            authService.useGooglePhoto(authHeader.replace("Bearer ", ""));
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "กลับไปใช้รูป Google แล้ว"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }


    @PostMapping("/line-login")
    public ResponseEntity<?> lineLogin(@RequestBody Map<String, String> body) {
        try {
            String code = body.get("code");

            String jwt = authService.loginWithLine(code);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "token", jwt
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/line-callback")
    public void lineCallback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        
        // ✅ ngrok จะข้าม warning ถ้า request มาจาก custom User-Agent
        // redirect ทันทีโดยไม่รอ browser render
        response.setStatus(302);
        response.setHeader("ngrok-skip-browser-warning", "true");
        response.setHeader("Location", "myapp://callback?code=" + code);
        response.flushBuffer();
    }

    @GetMapping("/providers")
    public ResponseEntity<?> getProviders(
            @RequestHeader("Authorization") String authHeader) {

        User user = authService.getUserFromToken(authHeader.substring(7));

        List<String> connected = authService.getProviders(user);
        List<Map<String, String>> providers = authService.getProvidersWithPhoto(user);


        return ResponseEntity.ok(Map.of("providers", connected, "providerDetails", providers));
    }


    // Link Google/Facebook
    @PostMapping("/link/firebase")
    public ResponseEntity<?> linkFirebase(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            User user = authService.getUserFromToken(authHeader.substring(7));
            authService.linkFirebaseToUser(user, body.get("token"), body.get("photoUrl"));
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "message", e.getMessage()
            ));
        }
    }

    // Link LINE
    @PostMapping("/link/line")
    public ResponseEntity<?> linkLine(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            User user = authService.getUserFromToken(authHeader.substring(7));
            String photoUrl = authService.linkLineToUser(user, body.get("code"));
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "photoUrl", photoUrl != null ? photoUrl : "" // ✅
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "message", e.getMessage()
            ));
        }
    }

    // Unlink provider
    @DeleteMapping("/unlink/{provider}")
    public ResponseEntity<?> unlink(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("provider") String provider) {
        try {
            User user = authService.getUserFromToken(authHeader.substring(7));
            Provider p = Provider.valueOf(provider.toUpperCase());
            authService.unlinkProvider(user, p);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "message", e.getMessage()
            ));
        }
    }

    // ส่ง OTP
    @PostMapping("/change-phone/request")
    public ResponseEntity<?> requestChangePhone(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            User user = authService.getUserFromToken(authHeader.substring(7));
            String newPhone = body.get("phone");
            String devOtp = body.get("otp"); // ✅ รับ devOtp ด้วย (null ถ้า prod)
            authService.requestChangePhone(user, newPhone, devOtp);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "message", e.getMessage()
            ));
        }
    }

    // ยืนยัน OTP แล้วเปลี่ยนเบอร์
    @PostMapping("/change-phone/verify")
    public ResponseEntity<?> verifyChangePhone(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            User user = authService.getUserFromToken(authHeader.substring(7));
            String newPhone = body.get("phone");
            String otp = body.get("otp");
            authService.verifyChangePhone(user, newPhone, otp);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error", "message", e.getMessage()
            ));
        }
    }

    @PutMapping("/use-provider-photo")
    public ResponseEntity<?> useProviderPhoto(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        try {
            String token = authHeader.replace("Bearer ", "");
            User user = authService.getUserFromToken(token);
            user.setPhotoUrl(body.get("photoUrl"));
            user.setUseAvatar(false);
            // save ผ่าน authService
            authService.updatePhotoUrl(user);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
}
}