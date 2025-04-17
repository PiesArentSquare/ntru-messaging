import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
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

    public static NTRU create(int N, int df, int dg, int dr, int p, int q) throws Integers.NoInverseExists {
        for (int i = 0; i < 100; i++) {
            try {
                return new NTRU(Polynomial.tau(N, df + 1, df), Polynomial.tau(N, dg, dg), dr, p, q);
            } catch (Integers.NoInverseExists ignored) {}
        }
        throw new Integers.NoInverseExists("couldn't find a good choice for f and g");
    }

    public Polynomial encryptToPolynomial(String message) throws MessageTooLargeException {
        Polynomial encoded = bytesToPolynomial(message.getBytes(StandardCharsets.UTF_8), p.modulus, true);
        var phi = Polynomial.tau(N, d, d);
//        System.out.println(phi);
        return phi.scale(p.modulus, q).times(h, q).plus(encoded, q);
    }

    public byte[] encryptToBytes(String message) throws MessageTooLargeException {
        Polynomial encrypted = encryptToPolynomial(message);
//        System.out.println(encrypted);
        byte[] encoded = polynomialToBytes(encrypted, q.modulus, false);
        return encoded;
    }

    public String encrypt(String message) throws MessageTooLargeException {
        return Base64.getEncoder().encodeToString(encryptToBytes(message));
    }

    public String decryptFromPolynomial(Polynomial encrypted) {
        Polynomial encoded = f.times(encrypted, q).lift(q).times(Fp, p).lift(p);
        encoded = encoded.mod(p);
        return new String(polynomialToBytes(encoded, p.modulus, true), StandardCharsets.UTF_8);
    }

    public String decryptFromBytes(byte[] encrypted) throws MessageTooLargeException {
        Polynomial polynomial = bytesToPolynomial(encrypted, q.modulus, false);
        return decryptFromPolynomial(polynomial);
    }

    public String decrypt(String encrypted) throws MessageTooLargeException {
        return decryptFromBytes(Base64.getDecoder().decode(encrypted));
    }

    private Polynomial bytesToPolynomial(byte[] bytes, int modulus, boolean trim) throws MessageTooLargeException {
        int[] ints = new int[bytes.length];
        for (int i = 0; i < ints.length; i++)
            ints[i] = bytes[i] & 0xFF;
        int[] coeffs = baseConvert(ints, 256, modulus, trim);
        return new Polynomial(coeffs, N);
    }

    private byte[] polynomialToBytes(Polynomial message, int modulus, boolean trim) {
        int[] ints = baseConvert(message.coefficients, modulus, 256, trim);
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) ints[i];
        return Arrays.copyOf(bytes, ints.length);
    }

    public static int[] baseConvert(int[] digits, int sourceBase, int destinationBase, boolean trim) {
        // adapted from Kevin Kwok (antimatter15@gmail.com)
        // https://gist.github.com/antimatter15/2bf0fcf5b924b4387174d5f84f35277c
        // reverse digits to not have to deal with the trailing 0s
        if (trim)
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
            if (done) {
                var res = result.stream().mapToInt(Integer::intValue).toArray();
                if (trim)
                    res = reverse(res);
                return res;
            }
        }
    }

    private static int[] reverse(int[] arr) {
        int[] result = new int[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[arr.length - i - 1] = arr[i];
        return result;
    }

    public static class MessageTooLargeException extends Exception {
        MessageTooLargeException(String message) {super(message);}
    }
}
