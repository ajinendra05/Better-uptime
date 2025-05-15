package com.devproject.dpinUptime.model;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator;
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import java.security.SecureRandom;
import java.util.Base64;

public class SolanaKeyPair {
    private final byte[] publicKey;
    private final byte[] privateKey;

    public SolanaKeyPair(byte[] privateKey) {
        Ed25519PrivateKeyParameters privateKeyParams = new Ed25519PrivateKeyParameters(privateKey, 0);
        this.privateKey = privateKeyParams.getEncoded();
        this.publicKey = privateKeyParams.generatePublicKey().getEncoded();
    }

    // Generate from seed phrase
    public static SolanaKeyPair fromSeed(byte[] seed) {
        Ed25519KeyPairGenerator generator = new Ed25519KeyPairGenerator();
        generator.init(new Ed25519KeyGenerationParameters(new SecureRandom()));
        AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();

        return new SolanaKeyPair(
                ((Ed25519PrivateKeyParameters) keyPair.getPrivate()).getEncoded());
    }

    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(publicKey);
    }

    public String sign(String message) {
        Ed25519Signer signer = new Ed25519Signer();
        signer.init(true, new Ed25519PrivateKeyParameters(privateKey, 0));
        signer.update(message.getBytes(), 0, message.length());
        return Base64.getEncoder().encodeToString(signer.generateSignature());
    }

    public static boolean verify(String message, String publicKey, String signature) {
        try {
            Ed25519Signer verifier = new Ed25519Signer();
            verifier.init(false, new Ed25519PublicKeyParameters(
                    Base64.getDecoder().decode(publicKey), 0));
            verifier.update(message.getBytes(), 0, message.length());
            return verifier.verifySignature(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            return false;
        }
    }
}