import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EncryptionDecryptionTest {

    @Test
    void encryptionDecryption() {
        Polynomial f = new Polynomial(new int[]{1, 0, -1, 1, 0}), g = new Polynomial(new int[]{0, 0, 1, 0, -1});
        Integers p = new Integers(13), q = new Integers(147);
        PolynomialInverter F = new PolynomialInverter(f);
        try {
            for (int i = 0; i < 10000; i++) {
                Polynomial Fp = F.findInverse(p), Fq = F.findInverse(q), h = Fq.times(g, q);
                Polynomial m = new Polynomial(new int[]{-5, 2, 4, -1, 3}),
                        phi = Polynomial.tau(m.coefficients.length, 1, 1),
                        e = phi.scale(p.modulus, q).times(h, q).plus(m, q),
                        recovered = f.times(e, q).lift(q).times(Fp, p).lift(p);
                Assertions.assertTrue(recovered.congruent(m, p));
            }
        } catch (Integers.NoInverseExists e) {
            e.printStackTrace();
        }
    }

}
