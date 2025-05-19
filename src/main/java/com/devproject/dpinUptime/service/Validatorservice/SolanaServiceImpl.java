package com.devproject.dpinUptime.service.Validatorservice;

import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bitcoinj.core.Base58;

import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

// @Service
// public class SolanaServiceImpl implements SolanaService {

//     @Override
//     public boolean verifySignature(String message, String publicKey, String signature) {
//         try {
//             Ed25519Signer verifier = new Ed25519Signer();
//             verifier.init(false, new Ed25519PublicKeyParameters(
//                     Base64.getDecoder().decode(publicKey), 0));
//             verifier.update(message.getBytes(), 0, message.length());
//             return verifier.verifySignature(Base64.getDecoder().decode(signature));
//         } catch (Exception e) {
//             return false;
//         }
//     }
// }

@Service
public class SolanaServiceImpl implements SolanaService {

    @Override
    public boolean verifySignature(String message, String publicKeyBase58, String signatureBase64) {
        try {
            byte[] publicKeyBytes = Base58.decode(publicKeyBase58);

            // Validate public key length (Ed25519 requires 32 bytes)
            if (publicKeyBytes.length != 32) {
                throw new IllegalArgumentException("Invalid public key length");
            }

            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
            if (signatureBytes.length != 64) {
                throw new IllegalArgumentException("Invalid signature length");
            }

            Ed25519PublicKeyParameters publicKeyParams = new Ed25519PublicKeyParameters(publicKeyBytes, 0);

            Ed25519Signer verifier = new Ed25519Signer();
            verifier.init(false, publicKeyParams);

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            verifier.update(messageBytes, 0, messageBytes.length);

            return verifier.verifySignature(signatureBytes);

        } catch (Exception e) {
            return false;
        }
    }
}
// package com.devproject.dpinUptime.service.Validatorservice;
// import org.bouncycastle.crypto.signers.Ed25519Signer;
// import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
// import org.springframework.stereotype.Service;
// import java.nio.charset.StandardCharsets;
// import java.util.Base64;

// @Service
// public class SolanaServiceImpl implements SolanaService {
// @Override
// public boolean verifySignature(String message, String publicKey, String
// signature) {
// final var verifier = new Ed25519Signer();
// verifier.init(false, new Ed25519PublicKeyParameters(
// Base64.getDecoder().decode(publicKey)
// ));
// verifier.update(message.getBytes(StandardCharsets.UTF_8), 0,
// message.length());
// return verifier.verifySignature(Base64.getDecoder().decode(signature));
// }
// }