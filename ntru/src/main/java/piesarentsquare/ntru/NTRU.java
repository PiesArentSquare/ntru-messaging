package piesarentsquare.ntru;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class NTRU {
    Polynomial f, g, Fp, Fq, h, foreignKey;
    Integers p, q;
    int N, d;
    final int encryptedByteLength;

    public int[] enc, dec;

    NTRU(Polynomial f, Polynomial g, int d, int p, int q) throws Integers.NoInverseExists {
        this.f = f;
        this.g = g;
        this.p = new Integers(p);
        this.q = new Integers(q);
        this.N = g.coefficients.length;
        this.d = d;
        // how many bytes an encrypted polynomial will take up. important for making sure block lengths are consistent
        this.encryptedByteLength = (int) Math.ceil(N * Math.ceil(Math.log(q) / Math.log(2)) / 8);
        var F = new PolynomialInverter(f);
        Fp = F.findInverse(p);
        Fq = F.findInverse(q);
        this.h = Fq.times(g, this.q);
    }

    public static NTRU create(int N, int df, int dg, int dr, int p, int q) {
        for (int i = 0; i < 100; i++) {
            try {
                return new NTRU(Polynomial.tau(N, df + 1, df), Polynomial.tau(N, dg, dg), dr, p, q);
            } catch (Integers.NoInverseExists ignored) {}
        }
        throw new RuntimeException("damn");
    }

    public String getPublicKey() {
        return encodeBase64(encodeToBytes(h));
    }

    public void setForeignKey(String foreignKey) {
        try {
            this.foreignKey = decodeFromBytes(decodeBase64(foreignKey));
        } catch (MessageTooLargeException ignored) {
            throw new RuntimeException("could not decode foreign key");
        }
    }

    public String encrypt(String message) {
        int[] fullCoeffs = bytesToCoeffs(message.getBytes(StandardCharsets.UTF_8), p.modulus, true);
        // ensure coeffs dont end on a zero
        fullCoeffs = Arrays.copyOf(fullCoeffs, fullCoeffs.length + 1);
        fullCoeffs[fullCoeffs.length - 1] = 1;
        int numBlocks = (fullCoeffs.length) / N + (fullCoeffs.length % N != 0 ? 1 : 0);
        int[] encryptedCoeffs = new int[numBlocks * N];
        for (int i = 0; i < fullCoeffs.length; i += N) {
            int blockSize = Math.min(fullCoeffs.length - i, N);
            int[] coeffs = new int[blockSize];
            System.arraycopy(fullCoeffs, i, coeffs, 0, blockSize);
            try {
                var poly = encryptPolynomial(new Polynomial(coeffs, N, true));
                System.arraycopy(poly.coefficients, 0, encryptedCoeffs, i, N);
            } catch (MessageTooLargeException e) {
                throw new RuntimeException("This should be impossible", e);
            }
        }
        enc = encryptedCoeffs;
        int[] paddedCoeffs = new int[encryptedCoeffs.length + 1];
        System.arraycopy(encryptedCoeffs, 0, paddedCoeffs, 1, encryptedCoeffs.length);
        paddedCoeffs[0] = 1;
        return encodeBase64(coeffsToBytes(paddedCoeffs, q.modulus, false, false, false));
    }

    public String decrypt(String encrypted) {
        int[] paddedCoeffs = bytesToCoeffs(decodeBase64(encrypted), q.modulus, false);
        int[] fullCoeffs = new int[paddedCoeffs.length - 1];
        System.arraycopy(paddedCoeffs, 1, fullCoeffs, 0, fullCoeffs.length);
        dec = fullCoeffs;
        List<Integer> decrypted = new ArrayList<>();
        for (int i = 0; i < fullCoeffs.length; i += N) {
            int blockSize = Math.min(fullCoeffs.length - i, N);
            int[] coeffs = new int[blockSize];
            System.arraycopy(fullCoeffs, i, coeffs, 0, blockSize);
            try {
                int[] decryptedCoeffs = decryptPolynomial(new Polynomial(coeffs, N, false)).coefficients;
                for (int c : decryptedCoeffs)
                    decrypted.add(c);
            } catch (MessageTooLargeException e) {
                throw new RuntimeException("This should be impossible", e);
            }
        }
        int[] coefficients = decrypted.stream().mapToInt(Integer::intValue).toArray();
        return new String(coeffsToBytes(coefficients, p.modulus, true, false, true), StandardCharsets.UTF_8);
    }

    private Polynomial encryptPolynomial(Polynomial encoded) {
        var phi = Polynomial.tau(N, d, d);
        return phi.scale(p.modulus, q).times(foreignKey, q).plus(encoded, q);
    }

    byte[] encodeToBytes(Polynomial encrypted) {
        return coeffsToBytes(encrypted.coefficients, q.modulus, false, true,false);
    }

    String encodeBase64(byte[] message) {
        return Base64.getEncoder().encodeToString(message);
    }

    Polynomial decryptPolynomial(Polynomial encrypted) {
        Polynomial encoded = f.times(encrypted, q).lift(q).times(Fp, p).lift(p);
        return encoded.mod(p);
    }

    Polynomial decodeFromBytes(byte[] encrypted) throws MessageTooLargeException {
        return new Polynomial(bytesToCoeffs(encrypted, q.modulus, false), N, false);
    }

    byte[] decodeBase64(String encrypted) {
        return Base64.getDecoder().decode(encrypted);
    }

    private int[] bytesToCoeffs(byte[] bytes, int modulus, boolean trim) {
        int[] ints = new int[bytes.length];
        for (int i = 0; i < ints.length; i++)
            ints[i] = bytes[i] & 0xFF;
        var coeffs = baseConvert(ints, 256, modulus, trim, false);
        return coeffs;
    }

    private byte[] coeffsToBytes(int[] coefficients, int modulus, boolean trim, boolean pad, boolean trimLastCharacter) {
        int[] ints = baseConvert(coefficients, modulus, 256, trim, trimLastCharacter);
        if (pad && ints.length < encryptedByteLength) {
            int[] newInts = new int[encryptedByteLength];
            System.arraycopy(ints, 0, newInts, encryptedByteLength - ints.length, ints.length);
            ints = newInts;
        }
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = (byte) ints[i];
        return Arrays.copyOf(bytes, ints.length);
    }

    public int[] baseConvert(int[] digits, int sourceBase, int destinationBase, boolean trim, boolean trimLastCharacter) {
        // adapted from Kevin Kwok (antimatter15@gmail.com)
        // https://gist.github.com/antimatter15/2bf0fcf5b924b4387174d5f84f35277c
        // trim off trailing zeros, since this expects most significant digit first, and we're supplying the least
        int lastNonZero;
        for (lastNonZero = digits.length - 1; trim && lastNonZero >= 0; lastNonZero--)
            if (digits[lastNonZero] != 0) break;
        digits = Arrays.copyOf(digits, lastNonZero + 1 - (trimLastCharacter ? 1 : 0));
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
                return result.stream().mapToInt(Integer::intValue).toArray();
            }
        }
    }

    public static class MessageTooLargeException extends Exception {
        MessageTooLargeException(String message) {super(message);}
    }
}
