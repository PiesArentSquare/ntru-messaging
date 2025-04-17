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
        Fp = F.findInverse(p);
        Fq = F.findInverse(q);
        this.h = Fq.times(g, this.q);
    }

    public static NTRU create(int N, int df, int dg, int dr, int p, int q) throws Exception {
        for (int i = 0; i < 100; i++) {
            try {
                return new NTRU(Polynomial.tau(N, df + 1, df), Polynomial.tau(N, dg, dg), dr, p, q);
            } catch (Integers.NoInverseExists ignored) {}
        }
        throw new Exception("couldn't find a good choice for f and g");
    }

    public Polynomial encrypt(String message) throws Exception {
        Polynomial encoded = bytesToPolynomial(message.getBytes(StandardCharsets.UTF_8), p.modulus);
        var phi = Polynomial.tau(N, d, d);
        return phi.scale(p.modulus, q).times(h, q).plus(encoded, q);
    }

    public String decrypt(Polynomial encrypted) {
        Polynomial encoded = f.times(encrypted, q).lift(q).times(Fp, p).lift(p);
        encoded = encoded.mod(p);
        return new String(polynomialToBytes(encoded, p.modulus), StandardCharsets.UTF_8);
    }

    private Polynomial bytesToPolynomial(byte[] bytes, int modulus) throws Exception {
        int[] ints = new int[bytes.length];
        for (int i = 0; i < ints.length; i++)
            ints[i] = bytes[i] & 0xFF;
        int[] coeffs = baseConvert(ints, 256, modulus);
        return new Polynomial(coeffs, N);
    }

    private byte[] polynomialToBytes(Polynomial message, int modulus) {
        int[] ints = baseConvert(message.coefficients, modulus, 256);
        byte[] bytes = new byte[ints.length];
        int i;
        for (i = 0; i < bytes.length && ints[i] != 0; i++)
            bytes[i] = (byte) ints[i];
        return Arrays.copyOf(bytes, i);
    }

    public static int[] baseConvert(int[] digits, int sourceBase, int destinationBase) {
        // adapted from Kevin Kwok (antimatter15@gmail.com)
        // https://gist.github.com/antimatter15/2bf0fcf5b924b4387174d5f84f35277c
        // reverse digits to not have to deal with the trailing 0s
        digits = reverse(digits);
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
                return reverse(result.stream().mapToInt(Integer::intValue).toArray());
        }
    }

    public static int[] reverse(int[] arr) {
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[arr.length - i - 1] = arr[i];
        return result;
    }
}
