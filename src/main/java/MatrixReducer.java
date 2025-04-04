import java.math.BigInteger;
import java.util.*;

class Polynomial {
    int[] coefficients;

    Polynomial(int[] coeffs) {
        coefficients = coeffs;
    }

    Polynomial copy() {
        return new Polynomial(Arrays.copyOf(coefficients, coefficients.length));
    }

    Polynomial add(Polynomial other, Integers z) {
        assert coefficients.length == other.coefficients.length;
        for (int i = 0; i < coefficients.length; i++)
            coefficients[i] = z.mod(coefficients[i] + other.coefficients[i]);
        return this;
    }

    Polynomial plus(Polynomial other, Integers z) {
        return copy().add(other, z);
    }

    Polynomial mul(Polynomial other, Integers z) {
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

    Polynomial times(Polynomial other, Integers z) {
        return copy().mul(other, z);
    }

    Polynomial lift(Integers z) {
        var N = coefficients.length;
        var coeffs = Arrays.copyOf(coefficients, coefficients.length);
        for (int i = 0; i < N; i++)
            coeffs[i] = z.lift(coeffs[i]);
        return new Polynomial(coeffs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = coefficients.length - 1; i >= 0; i--) {
            if (coefficients[i] != 0) {
                if (coefficients[i] != 1 || i == 0)
                    sb.append(coefficients[i]);
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
}

class Integers {
    final int modulus;
    final int shift;
    Map<Integer, Integer> inverseCache = new HashMap<>();
    Integers(int m) {
        modulus = m;
        shift = (modulus / 2);
    }

    public static class NoInverseExists extends Exception {
        final String why;
        NoInverseExists(String w) {why = w;}
    }

    int mod(int num) {
        return mod(num, modulus);
    }
    static int mod(int num, int modulus) {
        int m = num % modulus;
        return m < 0 ? m + modulus : m;
    }

    int lift(int num) {
        return mod((num + shift)) - shift;
    }

    int inverse(int num) throws NoInverseExists {
        num = mod(num);
        if (num == 1)
            return num;
        if (num == 0)
            throw new NoInverseExists("0 never has an inverse");
        int inverse = inverseCache.computeIfAbsent(num, n -> {
            ArrayList<Integer> qs = new ArrayList<>();
            int a = modulus, b = n;
            do {
                qs.add(a / b);
                int r = a % b;
                a = b;
                b = r;
            } while (b != 1 && b != 0);
            // inverse does not exist
            if (b == 0)
                return 0;
            int f_i = 0, f_i_1 = 1, f_i_2 = 0;
            for (int q : qs) {
                f_i = q * f_i_1 + f_i_2;
                f_i_2 = f_i_1;
                f_i_1 = f_i;
            }
            return qs.size() % 2 == 1 ? mod(-f_i) : f_i;
        });
        if (inverse == 0)
            throw new NoInverseExists(num + " has no inverse mod " + modulus);
        return inverse;
    }
}

class PolynomialInverter {

    static class Fraction {
        BigInteger numerator, denominator = BigInteger.valueOf(1);
        Fraction (int a) {
            numerator = BigInteger.valueOf(a);
        }
        Fraction (Fraction other) {
            numerator = other.numerator;
            denominator = other.denominator;
        }
        Fraction mul(int other) {
            numerator = numerator.multiply(BigInteger.valueOf(other));
            return this;
        }
        Fraction mul(Fraction other) {
            numerator = numerator.multiply(other.numerator);
            denominator = denominator.multiply(other.denominator);
            return this;
        }
        Fraction add(int other) {
            numerator = numerator.add(denominator.multiply(BigInteger.valueOf(other)));
            return this;
        }
        Fraction add(Fraction other) {
            numerator = numerator.multiply(other.denominator).add(other.numerator.multiply(denominator));
            denominator = denominator.multiply(other.denominator);
            return this;
        }
        void reduce() {
            var g = numerator.gcd(denominator);
            numerator = numerator.divide(g);
            denominator = denominator.divide(g);
        }
    }

    int[][] values;
    final int[][] startingValues;
    final int N;
    final Polynomial f;

    PolynomialInverter(Polynomial f) {
        this.f = f;
        N = f.coefficients.length;
        startingValues = new int[N][N + 1];
        values = new int[N][N + 1];
        // convolution matrix
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                startingValues[i][j] = f.coefficients[Integers.mod(N - j + i, N)];
        // augment with 1 to solve fg = 1 for g
        startingValues[0][N] = 1;
    }

    void scaleRow(int rowIndex, int scalar, Integers z) {
        // rows have N+1 columns
        for (int i = 0; i < N + 1; i++)
            values[rowIndex][i] = z.mod(values[rowIndex][i] * scalar);
    }

    int[] getScaledRow(int rowIndex, int scalar, Integers z) {
        int[] scaled = Arrays.copyOf(values[rowIndex], N + 1);
        for (int i = 0; i < N + 1; i++)
            scaled[i] = z.mod(scaled[i] * scalar);
        return scaled;
    }

    void subtractFromRow(int rowIndex, int[] row, Integers z) {
        for (int i = 0; i < N + 1; i++)
            values[rowIndex][i] = z.mod(values[rowIndex][i] - row[i]);
    }

    Polynomial findInverse(Integers z) throws Integers.NoInverseExists {
        for (int i = 0; i < N; i++) {
            values[i] = Arrays.copyOf(startingValues[i], N + 1);
        }
        // echelon form
        for (int i = 0; i < N; i++) {
            // select a pivot row
            int j = i;
            for (; j < N; j++) {
                if (values[j][i] != 0)
                    break;
            }
            // if the whole column is 0's, then we don't have enough pivots => by IMT, matrix is not invertible
            if (j == N)
                throw new Integers.NoInverseExists(f + " has no inverse in R" + z.modulus);

            // swap the pivot row into position
            int[] rowi = values[i];
            values[i] = values[j];
            values[j] = rowi;

            // get a leading 1 in the pivot row
            scaleRow(i, z.inverse(values[i][i]), z);

            // get 0's under the pivot
            for (j = i + 1; j < N; j++) {
                // if this row is already zeroed, skip it
                if (values[j][i] == 0)
                    continue;
                // get a leading 1 in the jth row
                scaleRow(j, z.inverse(values[j][i]), z);
                subtractFromRow(j, values[i], z);
            }
        }

        // reduced echelon form
        for (int i = N - 1; i >= 0; i--) {
            // zero above the pivot
            for (int j = i - 1; j >= 0; j--) {
                // scale the pivot row by the value above the pivot
                int[] scaledRow = getScaledRow(i, values[j][i], z);
                subtractFromRow(j, scaledRow, z);
            }
        }
        int[] inverted = new int[N];
        for (int i = 0; i < N; i++)
            inverted[i] = values[i][N];
        return new Polynomial(inverted);
    }
}

public class MatrixReducer {
    public static void main(String[] args) {
        var f = new Polynomial(new int[]{1, 0, -1, 1, 0});
        var F = new PolynomialInverter(f);
        var p = new Integers(147);
        try {
            var Fp = F.findInverse(p);
            System.out.println("(" + Fp + ") * (" + f + ") = " + Fp.mul(f, p));
        } catch (Integers.NoInverseExists e) {
            System.out.println(e.why);
        }
    }
}
