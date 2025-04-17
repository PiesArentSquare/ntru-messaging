package piesarentsquare.ntru;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EncryptionDecryptionTest {

    @Test
    void encryptionDecryption() {
        try {
            String msg = "hello there \uD83D\uDE00";
            for (int i = 0; i < 1000; i++) {
                NTRU ntru;
                try {
                    ntru = NTRU.create(107, 14, 12, 5, 3, 64);
                } catch (Integers.NoInverseExists e) {
                    continue;
                }
                var ciphertext = ntru.encryptToPolynomial(msg);
                var plaintext = ntru.decryptFromPolynomial(ciphertext);
                Assertions.assertEquals(msg, plaintext);
            }
        } catch (NTRU.MessageTooLargeException e) {
            Assertions.fail();
        }
    }

}
