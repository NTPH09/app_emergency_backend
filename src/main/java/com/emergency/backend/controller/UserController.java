package com.emergency.backend.controller;

import java.util.Map;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.emergency.backend.entity.User;
import com.emergency.backend.service.UserService;
import com.emergency.backend.repository.UserRepository;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        try {
            User user = userService.getUserFromToken(authHeader);

            if (body.containsKey("firstname")
                    && body.get("firstname") != null
                    && !body.get("firstname").trim().isEmpty()) {
                user.setFirstname(body.get("firstname"));
            }

            if (body.containsKey("lastname")
                    && body.get("lastname") != null
                    && !body.get("lastname").trim().isEmpty()) {
                user.setLastname(body.get("lastname"));
            }

            if (body.containsKey("phone")
                    && body.get("phone") != null
                    && !body.get("phone").trim().isEmpty()) {

                String newPhone = body.get("phone");

                userRepository.findByPhone(newPhone).ifPresent(existing -> {
                    if (!existing.getId().equals(user.getId())) {
                        throw new RuntimeException("เบอร์นี้ถูกใช้แล้ว");
                    }
                });

                user.setPhone(newPhone);
            }

            if (body.containsKey("email")
                    && body.get("email") != null
                    && !body.get("email").trim().isEmpty()) {
                user.setEmail(body.get("email"));
            }

            if (body.containsKey("birthday")
                    && body.get("birthday") != null
                    && !body.get("birthday").trim().isEmpty()) {
                user.setDateOfBirth(java.time.LocalDate.parse(body.get("birthday")));
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "User updated"
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    private final List<String> avatars = List.of(
        "cat","bare","dog","rabbit","cipmuck",
        "raccon","coarala","panqice","unicon","fox"
    );

    @PutMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestHeader("Authorization") String authHeader,
                                        @RequestBody Map<String, String> body) {
        try {
            User user = userService.getUserFromToken(authHeader);
            String newAvatar = body.get("avatar");

            if (!avatars.contains(newAvatar)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status","error",
                    "message","Avatar ไม่ถูกต้อง"
                ));
            }

            user.setAvatar(newAvatar);
            user.setUseAvatar(true);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                "status","success",
                "message","Avatar updated",
                "avatar", newAvatar
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status","error",
                "message", e.getMessage()
            ));
        }
    }
}