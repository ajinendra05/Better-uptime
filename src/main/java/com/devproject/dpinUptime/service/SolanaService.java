package com.devproject.dpinUptime.service;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class SolanaService {

    public boolean verifySignature(String message, String publicKey, String signature) {
        final var verifier = new Ed25519Signer();
        verifier.init(false, new Ed25519PublicKeyParameters(
            Base64.getDecoder().decode(publicKey)
        ));
        verifier.update(message.getBytes(StandardCharsets.UTF_8), 0, message.length());
        return verifier.verifySignature(Base64.getDecoder().decode(signature));
    }
}