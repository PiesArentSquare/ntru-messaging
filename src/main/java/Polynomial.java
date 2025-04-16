import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Polynomial {
    int[] coefficients;

    Polynomial(int[] coeffs) {
        coefficients = coeffs;
    }

    Polynomial(int[] coeffs, int N) throws Exception{
        if (coeffs.length == N)
            coefficients = coeffs;
        if (coeffs.length > N)
            throw new Exception("message too long");
        else {
            coefficients = new int[N];
            System.arraycopy(coeffs, 0, coefficients, 0, coeffs.length);
        }
    }

    public static Polynomial one(int N) {
        int[] coeffs = new int[N];
        coeffs[0] = 1;
        return new Polynomial(coeffs);
    }

    public static Polynomial tau(int N, int d1, int d2) {
        if (d1 + d2 > N)
            throw new IllegalArgumentException("d1 + d2 must not be greater than N");
        int[] coeffs = new int[N];
        for (int i = 0; i < d1; i++)
            coeffs[i] = 1;
        for (int i = d1; i < d1 + d2; i++)
            coeffs[i] = -1;

        for (int i = N - 1; i >= 0; i--) {
            int j = ThreadLocalRandom.current().nextInt(i + 1);
            int t = coeffs[i];
            coeffs[i] = coeffs[j];
            coeffs[j] = t;
        }
        return new Polynomial(coeffs);
    }

    public Polynomial copy() {
        return new Polynomial(Arrays.copyOf(coefficients, coefficients.length));
    }

    private Polynomial add(Polynomial other, Integers z) {
        assert coefficients.length == other.coefficients.length;
        for (int i = 0; i < coefficients.length; i++)
            coefficients[i] = z.mod(coefficients[i] + other.coefficients[i]);
        return this;
    }

    public Polynomial plus(Polynomial other, Integers z) {
        return copy().add(other, z);
    }

    private Polynomial mul(Polynomial other, Integers z) {
        assert coefficients.length == other.coefficients.length;
        var N = coefficients.length;
        var coeffs = new int[N];
        for (int k = 0; k < N; k++) {
            for (int i = 0; i <= k; i++)
                coeffs[k] = z.mod(coeffs[k] + coefficients[i] * other.coefficients[k - i]);
            for (int i = k + 1; i < coeffs.length; i++)
                coeffs[k] = z.mod(coeffs[k] + coefficients[i] * other.coefficients[N + k - i]);
        }
        coefficients = coeffs;
        return this;
    }

    public Polynomial times(Polynomial other, Integers z) {
        return copy().mul(other, z);
    }

    public Polynomial scale(int scalar, Integers z) {
        var coeffs = Arrays.copyOf(coefficients, coefficients.length);
        for (int i = 0; i < coeffs.length; i++)
            coeffs[i] = z.mod(coeffs[i] * scalar);
        return new Polynomial(coeffs);
    }

    public Polynomial lift(Integers z) {
        var N = coefficients.length;
        var coeffs = Arrays.copyOf(coefficients, coefficients.length);
        for (int i = 0; i < N; i++)
            coeffs[i] = z.lift(coeffs[i]);
        return new Polynomial(coeffs);
    }

    public Polynomial mod(Integers z) {
        var N = coefficients.length;
        var coeffs = Arrays.copyOf(coefficients, coefficients.length);
        for (int i = 0; i < N; i++)
            coeffs[i] = z.mod(coeffs[i]);
        return new Polynomial(coeffs);
    }

    public boolean congruent(Polynomial other, Integers z) {
        return lift(z).equals(other.lift(z));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = coefficients.length - 1; i >= 0; i--) {
            if ((coefficients[i] != 1 && coefficients[i] != 0) || i == 0)
                sb.append(coefficients[i]);
            if (coefficients[i] != 0) {
                if (i != 0) {
                    sb.append("x");
                    if (i != 1)
                        sb.append("^").append(i);
                    sb.append(" + ");
                }
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Polynomial other))
            return false;
        return Arrays.equals(coefficients, other.coefficients);
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(coefficients);
    }
}
