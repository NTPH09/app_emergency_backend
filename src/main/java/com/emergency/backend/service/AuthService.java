package com.emergency.backend.service;

import java.util.List;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.emergency.backend.entity.Provider;
import com.emergency.backend.entity.User;
import com.emergency.backend.repository.UserProviderRepository;
import com.emergency.backend.repository.UserRepository;
import com.emergency.backend.config.JwtUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import java.util.Map;
import com.emergency.backend.entity.UserProvider;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final LineService lineService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final UserProviderRepository userProviderRepository;
    private final Map<String, String> otpStore = new java.util.concurrent.ConcurrentHashMap<>();

    public List<String> getProviders(User user) {
        return userProviderRepository.findByUser(user)
            .stream()
            .map(p -> p.getProvider().name().toLowerCase())
            .distinct() // กันซ้ำ
            .toList();
    }

    public AuthService(
        UserRepository userRepository, 
        JwtUtil jwtUtil, LineService lineService, 
        UserProviderRepository userProviderRepository) 
        {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.lineService = lineService;
        this.userProviderRepository = userProviderRepository;
    }

    // ✅ แก้: ถ้าเชื่อมอยู่แล้ว → return เลย ไม่ throw
    private void linkProvider(User user, Provider provider, String providerId, String photoUrl) {
        boolean exists = userProviderRepository
            .findByUser(user)
            .stream()
            .anyMatch(p -> p.getProvider() == provider
                        && p.getProviderId() != null
                        && p.getProviderId().equals(providerId));

        if (exists) return;

        UserProvider up = new UserProvider();
        up.setUser(user);
        up.setProvider(provider);
        up.setProviderId(providerId);
        up.setPhotoUrl(photoUrl); // ✅

        userProviderRepository.save(up);
    }

    private String normalizePhone(String phone) {
        if (phone == null) return null;
        if (phone.startsWith("+66")) {
            return "0" + phone.substring(3);
        }
        return phone;
    }

    /// REGISTER
    public User register(User user) {
        if (user.getEmail() != null &&
            userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (user.getPhone() != null &&
            userRepository.findByPhone(user.getPhone()).isPresent()) {
            throw new RuntimeException("Phone already exists");
        }

        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new RuntimeException("Password must be at least 6 characters");
        }

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("USER");

        if (user.getAvatar() == null) {
            user.setAvatar(randomAvatar());
        }

        User saved = userRepository.save(user);
        linkProvider(saved, Provider.LOCAL, null, null);

        return saved;
    }

    /// LOGIN (email หรือ phone)
    public String login(String username, String password) {
        final String userInput = username.trim();
        final String passInput = password.trim();

        User user = userRepository.findByEmail(userInput)
            .orElseGet(() -> userRepository.findByPhone(userInput).orElse(null));

        if (user == null) {
            throw new RuntimeException("อีเมลหรือเบอร์โทรศัพท์ไม่ถูกต้อง");
        }

        if (!encoder.matches(passInput, user.getPassword())) {
            throw new RuntimeException("รหัสผ่านไม่ถูกต้อง");
        }

        return jwtUtil.generateToken(user.getId().toString());
    }

    public String getUserIdFromToken(String token) {
        return jwtUtil.extractUserId(token);
    }

    public User getUserFromToken(String token) {
        String userId = jwtUtil.extractUserId(token);
        return userRepository.findById(Long.parseLong(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /// FIREBASE LOGIN
    public String loginWithFirebase(String firebaseToken, String photoUrlFromClient) {
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);

            String phone = (String) decoded.getClaims().get("phone_number");
            String email = decoded.getEmail();
            String name = decoded.getName();
            String picture = decoded.getPicture();
            String uid = decoded.getUid();

            // ✅ แก้: ดึง provider จาก sign_in_provider ใน claims ไม่ใช่ issuer
            String signInProvider = "";
            Object providerObj = decoded.getClaims().get("firebase");
            if (providerObj instanceof Map<?, ?> firebaseMap) {
                Object signInProviderObj = firebaseMap.get("sign_in_provider");
                if (signInProviderObj != null) {
                    signInProvider = signInProviderObj.toString();
                }
            }

            Provider provider = Provider.GOOGLE; // default
            if (signInProvider.contains("facebook")) {
                provider = Provider.FACEBOOK;
            }

            if (photoUrlFromClient != null && !photoUrlFromClient.isBlank()) {
                picture = photoUrlFromClient;
            }

            User user = userProviderRepository
                .findByProviderAndProviderId(provider, uid)
                .map(UserProvider::getUser)
                .orElse(null);

            if (user == null && phone != null) {
                phone = normalizePhone(phone);
                user = userRepository.findByPhone(phone).orElse(null);
            }

            if (user == null && email != null) {
                user = userRepository.findByEmail(email).orElse(null);
            }

            if (user == null) {
                user = new User();
                user.setPhone(phone);
                user.setEmail(email);
                user.setRole("USER");
                user.setAvatar(randomAvatar());

                if (name != null && !name.isEmpty()) {
                    String[] parts = name.split(" ", 2);
                    user.setFirstname(parts[0]);
                    if (parts.length > 1) user.setLastname(parts[1]);
                }

                if (picture != null && !picture.isEmpty()) {
                    user.setPhotoUrl(picture);
                    user.setUseAvatar(false);
                } else {
                    user.setUseAvatar(true);
                }

                userRepository.save(user);
            }

            linkProvider(user, provider, uid, picture);

            return jwtUtil.generateToken(user.getId().toString());

        } catch (Exception e) {
            // ✅ แก้: log error จริงเพื่อ debug
            e.printStackTrace();
            throw new RuntimeException("Firebase error: " + e.getMessage());
        }
    }

    /// LINE LOGIN
    public String loginWithLine(String code) {
        Map<String, Object> token = lineService.getToken(code);
        String idToken = (String) token.get("id_token");
        Map<String, Object> userInfo = lineService.decodeIdToken(idToken);

        String lineId = (String) userInfo.get("sub");
        String name = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");
        String picture = (String) userInfo.get("picture");

        if (email == null) email = "line_" + lineId + "@noemail.com";

        final String finalEmail = email;
        final String finalPicture = picture;

        User user = userProviderRepository
            .findByProviderAndProviderId(Provider.LINE, lineId)
            .map(UserProvider::getUser)
            .orElseGet(() -> {
                User existing = userRepository.findByEmail(finalEmail).orElse(null);

                if (existing != null) {
                    if (finalPicture != null && !finalPicture.isEmpty()) {
                        existing.setPhotoUrl(finalPicture);
                        existing.setUseAvatar(false);
                        userRepository.save(existing);
                    }
                    return existing;
                }

                User u = new User();
                if (name != null) {
                    String[] parts = name.split(" ", 2);
                    u.setFirstname(parts[0]);
                    if (parts.length > 1) u.setLastname(parts[1]);
                }
                u.setEmail(finalEmail);
                u.setAvatar(randomAvatar());
                u.setRole("USER");
                if (finalPicture != null && !finalPicture.isEmpty()) {
                    u.setPhotoUrl(finalPicture);
                    u.setUseAvatar(false);
                } else {
                    u.setUseAvatar(true);
                }
                return userRepository.save(u);
            });

        if (picture != null && !picture.isEmpty()) {
            user.setPhotoUrl(picture);
            user.setUseAvatar(false);
            userRepository.save(user);
        }

        linkProvider(user, Provider.LINE, lineId, null);

        return jwtUtil.generateToken(user.getId().toString());
    }

    // ✅ แก้: set role และ avatar ก่อน save ครั้งแรก (avatar เป็น NOT NULL)
    public String loginWithPhone(String rawPhone) {
        String phone = normalizePhone(rawPhone);

        User user = userRepository.findByPhone(phone).orElse(null);

        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setRole("USER");
            user.setAvatar(randomAvatar()); // ✅ set ก่อน save
            userRepository.save(user);
            linkProvider(user, Provider.PHONE, phone, null);
        }

        return jwtUtil.generateToken(user.getId().toString());
    }

    public void changePassword(String authHeader, String oldPassword, String newPassword) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(Long.parseLong(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPassword() == null) {
            throw new RuntimeException("บัญชีนี้ไม่ได้ตั้งรหัสผ่าน");
        }

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("รหัสผ่านเก่าไม่ถูกต้อง");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("รหัสผ่านใหม่ต้องอย่างน้อย 6 ตัว");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    /// SET PASSWORD (สำหรับ user ที่ยังไม่มี password)
    public void setPassword(String authHeader, String newPassword) {
        String token = authHeader.replace("Bearer ", "");
        String userId = jwtUtil.extractUserId(token);

        User user = userRepository.findById(Long.parseLong(userId))
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getPassword() != null) {
            throw new RuntimeException("บัญชีนี้มีรหัสผ่านอยู่แล้ว");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("รหัสผ่านต้องอย่างน้อย 6 ตัว");
        }

        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
    }

    private final List<String> avatars = List.of(
        "cat", "bare", "dog", "rabbit", "cipmuck",
        "raccon", "coarala", "panqice", "unicon", "fox"
    );

    private String randomAvatar() {
        return avatars.get(new Random().nextInt(avatars.size()));
    }

    public boolean updateAvatar(String token, String avatar) {
        User user = getUserFromToken(token);
        user.setAvatar(avatar);
        user.setUseAvatar(true);
        userRepository.save(user);
        return true;
    }

    public boolean useGooglePhoto(String token) {
        User user = getUserFromToken(token);
        user.setUseAvatar(false);
        userRepository.save(user);
        return true;
    }

    public void linkFirebaseToUser(User user, String firebaseToken, String photoUrl) {
        try {
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);

            String uid = decoded.getUid();

            // ✅ แก้: ดึง provider จาก sign_in_provider ใน claims
            String signInProvider = "";
            Object providerObj = decoded.getClaims().get("firebase");
            if (providerObj instanceof Map<?, ?> firebaseMap) {
                Object signInProviderObj = firebaseMap.get("sign_in_provider");
                if (signInProviderObj != null) {
                    signInProvider = signInProviderObj.toString();
                }
            }

            Provider provider = Provider.GOOGLE;
            if (signInProvider.contains("facebook")) {
                provider = Provider.FACEBOOK;
            }

            final Provider finalProvider = provider;
            userProviderRepository.findByProviderAndProviderId(provider, uid)
                .ifPresent(p -> {
                    if (!p.getUser().getId().equals(user.getId())) {
                        throw new RuntimeException("บัญชีนี้เชื่อมกับ user อื่นอยู่แล้ว");
                    }
                });

            linkProvider(user, finalProvider, uid, photoUrl);

            if (user.getPhotoUrl() == null && photoUrl != null && !photoUrl.isEmpty()) {
                user.setPhotoUrl(photoUrl);
                user.setUseAvatar(false);
                userRepository.save(user);
            }

        } catch (RuntimeException e) {
            throw e; // re-throw RuntimeException เช่น "เชื่อมกับ user อื่น"
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Firebase error: " + e.getMessage());
        }
    }

    public String linkLineToUser(User user, String code) {
        Map<String, Object> token = lineService.getToken(code);
        String idToken = (String) token.get("id_token");
        Map<String, Object> userInfo = lineService.decodeIdToken(idToken);

        String lineId = (String) userInfo.get("sub");
        String picture = (String) userInfo.get("picture"); // ✅

        userProviderRepository.findByProviderAndProviderId(Provider.LINE, lineId)
            .ifPresent(p -> {
                if (!p.getUser().getId().equals(user.getId())) {
                    throw new RuntimeException("LINE นี้เชื่อมกับ user อื่นอยู่แล้ว");
                }
            });

        linkProvider(user, Provider.LINE, lineId, picture);

        // ✅ ไม่ set photoUrl อัตโนมัติ — ให้ Flutter ถามก่อน
        // เอาออก: user.setPhotoUrl(picture) ที่เคยมี

        return picture; // ✅ return กลับไปให้ Flutter
    }

    @Transactional
    public void unlinkProvider(User user, Provider provider) {
        List<UserProvider> providers = userProviderRepository.findByUser(user);

        boolean hasPassword = user.getPassword() != null;
        boolean hasPhone = user.getPhone() != null;

        long remaining = providers.stream()
            .filter(p -> p.getProvider() != provider)
            .count();

        if (remaining == 0 && !hasPassword && !hasPhone) {
            throw new RuntimeException("กรุณาตั้งรหัสผ่านหรือเพิ่มเบอร์ก่อน");
        }

        List<UserProvider> targets = providers.stream()
            .filter(p -> p.getProvider() == provider)
            .toList();

        if (targets.isEmpty()) {
            throw new RuntimeException("ไม่พบ provider นี้");
        }

        // ✅ เปลี่ยนจาก deleteAll + flush → deleteAllInBatch
        userProviderRepository.deleteAllInBatch(targets);

        String photoUrl = user.getPhotoUrl();
        if (photoUrl != null) {
            boolean shouldClear = false;

            if (provider == Provider.GOOGLE &&
                (photoUrl.contains("google") || photoUrl.contains("googleapis") || 
                photoUrl.contains("googleusercontent"))) {
                shouldClear = true;
            }

            if (provider == Provider.FACEBOOK &&
                (photoUrl.contains("facebook") || photoUrl.contains("fbsbx"))) {
                shouldClear = true;
            }

            if (provider == Provider.LINE &&
                photoUrl.contains("profile.line-scdn.net")) {
                shouldClear = true;
            }

            if (shouldClear) {
                user.setPhotoUrl(null);
                user.setUseAvatar(true); // ✅ fallback กลับไปใช้ avatar
                userRepository.save(user);
            }
        }
    }

    public void requestChangePhone(User user, String newPhone, String devOtp) {
        if (newPhone == null || newPhone.isBlank()) {
            throw new RuntimeException("กรุณากรอกเบอร์โทรศัพท์");
        }

        String normalized = normalizePhone(newPhone);

        if (userRepository.findByPhone(normalized).isPresent()) {
            throw new RuntimeException("เบอร์นี้ถูกใช้งานแล้ว");
        }

        // ✅ dev ส่ง OTP มาเอง / prod generate เอง
        String otp = (devOtp != null && !devOtp.isBlank())
            ? devOtp
            : String.format("%06d", new Random().nextInt(1000000));

        otpStore.put(normalized, otp);

        System.out.println("OTP for " + normalized + " : " + otp);
    }

    public void verifyChangePhone(User user, String newPhone, String otp) {
        String normalized = normalizePhone(newPhone);
        String stored = otpStore.get(normalized);

        if (stored == null) {
            throw new RuntimeException("ไม่พบ OTP กรุณาขอใหม่");
        }

        if (!stored.equals(otp)) {
            throw new RuntimeException("OTP ไม่ถูกต้อง");
        }

        // ลบ OTP ออกหลังใช้แล้ว
        otpStore.remove(normalized);

        // อัปเดตเบอร์ใหม่
        user.setPhone(normalized);
        userRepository.save(user);

        // อัปเดต provider PHONE ด้วย
        List<UserProvider> phoneProviders = userProviderRepository.findByUser(user)
            .stream()
            .filter(p -> p.getProvider() == Provider.PHONE)
            .toList();

        if (!phoneProviders.isEmpty()) {
            UserProvider pp = phoneProviders.get(0);
            pp.setProviderId(normalized);
            userProviderRepository.save(pp);
        }
    }

    public List<Map<String, String>> getProvidersWithPhoto(User user) {
        return userProviderRepository.findByUser(user)
            .stream()
            .map(p -> {
                Map<String, String> map = new java.util.HashMap<>();
                map.put("provider", p.getProvider().name().toLowerCase());
                map.put("photoUrl", p.getPhotoUrl() != null ? p.getPhotoUrl() : "");
                return map;
            })
            .toList();
    }

    public void updatePhotoUrl(User user) {
        userRepository.save(user);
    }
}