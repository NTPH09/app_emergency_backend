// package com.emergency.backend.config;

// import java.util.Collections;

// import org.springframework.stereotype.Service;

// import com.google.api.client.googleapis.auth.oauth2.*;
// import com.google.api.client.http.javanet.NetHttpTransport;
// import com.google.api.client.json.gson.GsonFactory; // ✅ ใช้ Gson

// @Service
// public class GoogleService {

//     private static final String CLIENT_ID =
//         "1091903595849-71o40d7dg850i9a3nugg5g2jqbu9irem.apps.googleusercontent.com";

//     public GoogleIdToken.Payload verifyIdToken(String idTokenString) {
//         if (idTokenString == null || idTokenString.isEmpty()) return null;

//         try {
//             GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
//                     new NetHttpTransport(),
//                     new GsonFactory())
//                     .setAudience(Collections.singletonList(CLIENT_ID))
//                     .build();

//             GoogleIdToken idToken = verifier.verify(idTokenString);
//             if (idToken != null) return idToken.getPayload();
            
//         if (idToken == null) {
//             System.out.println("❌ Invalid Google token");
//         }
//         } catch (Exception e) {
//             e.printStackTrace();
//         }

//         return null;
//     }
    
// }