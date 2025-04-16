import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PolynomialInverterTest {

    @Test
    void noBadInversesTest() {
        Polynomial f = new Polynomial(new int[]{1, 0, -1, 1, 0, 1, 0, -1});
        Polynomial one = Polynomial.one(f.coefficients.length);
        PolynomialInverter F = new PolynomialInverter(f);
        for (int i = 3; i < 256; i++) {
            Integers z = new Integers(i);
            try {
                Assertions.assertEquals(F.findInverse(z).times(f, z), one);
            } catch (Integers.NoInverseExists ignored) {}
        }
    }
}
