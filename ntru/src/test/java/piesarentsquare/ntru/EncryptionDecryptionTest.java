package piesarentsquare.ntru;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;

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
//                var ciphertext = ntru.encryptToBytes(msg);
                var poly_e = ntru.encryptToPolynomial(msg);
                var bytes_e = ntru.encodeToBytes(poly_e);
                var base64 = ntru.encodeBase64(bytes_e);
                var bytes_d = ntru.decodeBase64(base64);
                var poly_d = ntru.decodeFromBytes(bytes_d);
                var plaintext = ntru.decryptFromPolynomial(poly_d);
                try {
                    Assertions.assertEquals(msg, plaintext);
                } catch (AssertionFailedError e) {
                    System.out.println(poly_e);
                    System.out.println(poly_d);
                    System.out.println(Arrays.toString(bytes_e));
                    System.out.println(Arrays.toString(bytes_d));
                    System.out.println(base64);
                    throw e;
                }
            }
        } catch (NTRU.MessageTooLargeException e) {
            Assertions.fail();
        }
    }

}
