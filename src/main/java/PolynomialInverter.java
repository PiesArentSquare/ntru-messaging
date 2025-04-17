import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

class PrimeInverter {
    final int[][] startingValues;
    int[][] values;
    final int N;
    Integers z;

    PrimeInverter(final int[][] startingValues, Integers z) {
        N = startingValues.length;
        this.startingValues = startingValues;
        values = new int[N][N + 1];
        copyValuesToWorking();
        this.z = z;
    }

    private void copyValuesToWorking() {
        for (int i = 0; i < N; i++)
            values[i] = Arrays.copyOf(startingValues[i], N + 1);
    }

    Polynomial findInverse(int exponent) throws Integers.NoInverseExists {
        Polynomial result = findInverse();
        int pk, pkPlus1 = pk = z.modulus;
        Polynomial one = Polynomial.one(N);
        // lift from mod p^k to mod p^k+1 using hensel's lemma
        // I have no idea what's happening here and don't yet have the time to find out, so this is adapted from chatgpt
        for (int i = 1; i < exponent; i++) {
            pkPlus1 *= z.modulus;
            Integers m = new Integers(pkPlus1);

            // find residual = (b - Ax) / p^k mod p and put it in the augment column
            copyValuesToWorking();
            Polynomial Ax = multiplyMatrixVec(result, m);
            for (int j = 0; j < N; j++) {
                int diff = one.coefficients[j] - Ax.coefficients[j];
                values[j][N] = z.mod(diff / pk);
            }
            // solve A * delta = residual
            Polynomial delta = findInverse();

            // x = x + p^k * delta mod p^k+1
            result = result.plus(delta.scale(pk, m), m);

            pk = pkPlus1;
        }
        return result;
    }

    private Polynomial multiplyMatrixVec(Polynomial x, Integers m) {
        int[] result = new int[N];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                result[i] = m.mod(result[i] + values[i][j] * x.coefficients[j]);
        return new Polynomial(result);
    }

    private Polynomial findInverse() throws Integers.NoInverseExists {
        // echelon form
        for (int i = 0; i < N; i++) {
            int j;
            for (j = i; j < N; j++)
                if (values[j][i] != 0)
                    break;
            // if the whole column is zeroes then there are not enough pivots => by IMT, matrix is not invertible
            if (j == N)
                throw new Integers.NoInverseExists("no inverse exists in R");

            // swap the pivot row into position
            int[] rowi = values[i];
            values[i] = values[j];
            values[j] = rowi;

            // normalize pivot
            scaleRow(i, z.inverse(values[i][i]), z);

            // zero under the pivot
            for (j = i + 1; j < N; j++)
                zeroByPivot(i, j, z);
        }

        // diagonalize
        for (int i = N - 1; i >= 0; i--)
            // zero above the pivot
            for (int j = i - 1; j >= 0; j--)
                zeroByPivot(i, j, z);

        // bring into reduced echelon
        int[] inverted = new int[N];
        for (int i = 0; i < N; i++) {
            int pivot = values[i][i], adjunct = values[i][N];
            inverted[i] = z.mod(adjunct * z.inverse(pivot));
        }
        return new Polynomial(inverted);
    }

    void scaleRow(int rowIndex, int scalar, Integers z) {
        // rows have N+1 columns because of the augment
        for (int i = 0; i < N + 1; i++)
            values[rowIndex][i] = z.lift(values[rowIndex][i] * scalar);
    }

    void zeroByPivot(int pivotIndex, int otherRowIndex, Integers z) {
        int belowPivot = values[otherRowIndex][pivotIndex];
        if (belowPivot == 0)
            return;
        for (int i = 0; i < N + 1; i++)
            values[otherRowIndex][i] = z.lift(values[otherRowIndex][i] - values[pivotIndex][i] * belowPivot);
    }
}

public class PolynomialInverter {
    final int[][] startingValues;
    int[][] values;
    final int N;

    PolynomialInverter(Polynomial f) {
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

    public Polynomial findInverse(int modulus) throws Integers.NoInverseExists {
        var factors = factorize(modulus);

        Integers z = new Integers(modulus);
        int[] coeffs = new int[N];

        for (var kv : factors.entrySet()) {
            int p = kv.getKey();
            int pk = (int) Math.pow(p, kv.getValue().intValue());
            Integers field = new Integers(p);
            Polynomial fieldInverse = new PrimeInverter(startingValues, field).findInverse(kv.getValue().intValue());

            int moduliExceptPK = modulus / pk;
            Integers fieldK = new Integers(pk);
            int moduliExceptPKInverse = fieldK.inverse(moduliExceptPK);

            // chinese remainder theory
            for (int i = 0; i < N; i++) {
                coeffs[i] = z.mod(coeffs[i] + fieldInverse.coefficients[i] * moduliExceptPK * moduliExceptPKInverse);
            }
        }
        return new Polynomial(coeffs);
    }

    static Map<Integer, LongAdder> factorize(int modulus) {
        int i = 2;
        int m = modulus;
        Map<Integer, LongAdder> frequencies = new HashMap<>();
        while (i <= m && modulus != 1) {
            if (modulus % i == 0) {
                modulus /= i;
                frequencies.computeIfAbsent(i, k -> new LongAdder()).increment();
            } else {
                i++;
            }
        }
        return frequencies;
    }

    public static void main(String[] args) throws Integers.NoInverseExists {
        var f = new Polynomial(new int[]{1, 0, -1, 1, 0});
        Integers z = new Integers(9*16*25);
        var F = new PolynomialInverter(f).findInverse(z.modulus);
        System.out.println(F);
        System.out.println(F.times(f, z));
    }
}