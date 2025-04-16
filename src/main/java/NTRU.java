import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NTRU {
    Polynomial f, g, Fp, Fq, h;
    Integers p, q;
    int N, d;


    NTRU(Polynomial f, Polynomial g, int d, int p, int q) throws Integers.NoInverseExists {
        this.f = f;
        this.g = g;
        this.p = new Integers(p);
        this.q = new Integers(q);
        this.N = g.coefficients.length;
        this.d = d;
        var F = new PolynomialInverter(f);
        System.out.println("created polynomial inverter");
        Fp = F.findInverse(this.p);
        System.out.println("found inverse mod p" + Fp);
        Fq = F.findInverse(this.q);
        System.out.println("found inverse mod q " + Fq);
        this.h = Fq.times(g, this.q);
        System.out.println("found pubkey " + h);
    }

    public static NTRU create(int N, int d, int p, int q) throws Exception {
//        for (int i = 0; i < 100; i++) {
            try {
                return new NTRU(Polynomial.tau(N, d + 1, d), Polynomial.tau(N, d, d), d, p, q);
            } catch (Integers.NoInverseExists ignored) {}
//        }
        throw new Exception("couldn't find a good choice for f and g");
    }

    public Polynomial encode(String message) throws Exception {
        var phi = Polynomial.tau(N, d, d);
        return phi.scale(p.modulus, q).times(h, q).plus(stringToPolynomial(message), q);
    }

    public String decode(Polynomial encoded) {
        return polynomialToString(f.times(encoded, q).lift(q).times(Fp, p).lift(p));
    }

    private Polynomial stringToPolynomial(String message) throws Exception {
        byte[] utf8 = message.getBytes(StandardCharsets.UTF_8);
        int[] utf8Ints = new int[utf8.length];
        for (int i = 0; i < utf8Ints.length; i++)
            utf8Ints[i] = utf8[i] & 0xFF;
        int[] coeffs = baseConvert(utf8Ints, 256, p.modulus);
        return new Polynomial(coeffs, N);
    }

    private String polynomialToString(Polynomial message) {
        int[] utf8Ints = baseConvert(message.coefficients, p.modulus, 256);
        byte[] utf8 = new byte[utf8Ints.length];
        int i;
        for (i = 0; i < utf8.length && utf8Ints[i] != 0; i++)
            utf8[i] = (byte) utf8Ints[i];
        return new String(Arrays.copyOf(utf8, i), StandardCharsets.UTF_8);
    }

    public static int[] baseConvert(int[] digits, int sourceBase, int destinationBase) {
        // adapted from Kevin Kwok (antimatter15@gmail.com)
        // https://gist.github.com/antimatter15/2bf0fcf5b924b4387174d5f84f35277c
        digits = Arrays.copyOf(digits, digits.length);
        int start = 0;
        List<Integer> result = new ArrayList<>();
        for (;;) {
            int carry = 0;
            boolean done = true;
            for (int i = start; i < digits.length; i++) {
                int p = sourceBase * carry + digits[i];
                digits[i] = p / destinationBase;
                carry = p % destinationBase;
                if (done) {
                    if (digits[i] == 0)
                        start = i;
                    else
                        done = false;
                }
            }
            result.add(0, carry);
            if (done)
                return result.stream().mapToInt(Integer::intValue).toArray();
        }
    }
}
