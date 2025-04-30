package piesarentsquare.backend.web.security;

import org.springframework.stereotype.Component;
import piesarentsquare.ntru.NTRU;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class EncryptionManager {
    public final ConcurrentHashMap<String, NTRU> sessionCryptoMap = new ConcurrentHashMap<>();

    public void init(String id, String foreignKey) {
        NTRU ntru = NTRU.create(107, 14, 12, 5, 3, 64);
        ntru.setForeignKey(foreignKey);
        sessionCryptoMap.put(id, ntru);
        System.out.println("got foreign key " + foreignKey + " for user " + id);
    }

    public void dispose(String id) {
        sessionCryptoMap.remove(id);
    }

    public String getPublicKey(String id) {
        NTRU ntru = sessionCryptoMap.get(id);
        return ntru != null ? ntru.getPublicKey() : null;
    }

    public String encrypt(String id, String plaintext) {
        NTRU ntru = sessionCryptoMap.get(id);
        System.out.println(ntru);
        return ntru != null ? ntru.encrypt(plaintext) : null;
    }

    public String decrypt(String id, String ciphertext) {
        NTRU ntru = sessionCryptoMap.get(id);
        System.out.println(ntru);
        return ntru != null ? ntru.decrypt(ciphertext) : null;
    }
}
