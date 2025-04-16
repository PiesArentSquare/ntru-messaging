import java.math.BigInteger;
import java.util.Arrays;

class PolynomialInverter {
    BigInteger[][] values;
    final Polynomial f;
    final int N;

    PolynomialInverter(Polynomial f) throws Integers.NoInverseExists {
        this.f = new Polynomial(f.coefficients.clone());
        N = f.coefficients.length;
        values = new BigInteger[N][N + 1];
        // convolution matrix
        for (int i = 0; i < N; i++)
            for (int j = 0; j < N; j++)
                values[i][j] = BigInteger.valueOf(f.coefficients[Integers.mod(N - j + i, N)]);
        // augment with 1 to solve fg = 1 for g
        values[0][N] = BigInteger.ONE;
        for (int i = 1; i < N; i++)
            values[i][N] = BigInteger.ZERO;
        System.out.println("created matrix");
        rref();
        System.out.println("rref done");
    }

    void scaleRow(int rowIndex, BigInteger scalar) {
        // rows have N+1 columns because of the augment
        for (int i = 0; i < N + 1; i++)
            values[rowIndex][i] = values[rowIndex][i].multiply(scalar);
    }
    void subtractFromRow(int rowIndex, BigInteger[] row) {
        for (int i = 0; i < N + 1; i++)
            values[rowIndex][i] = values[rowIndex][i].subtract(row[i]);
    }
    void zeroByPivot(int pivotIndex, int otherRowIndex) {
        BigInteger pivot = values[pivotIndex][pivotIndex], belowPivot = values[otherRowIndex][pivotIndex];
        // if this row is already zeroed, skip it
        if (belowPivot.equals(BigInteger.ZERO))
            return;
        // get the pivot and other row to have the same leading term
        if (!pivot.equals(belowPivot)) {
            BigInteger gcd = belowPivot.gcd(pivot);
            scaleRow(pivotIndex, belowPivot.divide(gcd));
            scaleRow(otherRowIndex, pivot.divide(gcd));
        }
        subtractFromRow(otherRowIndex, values[pivotIndex]);
    }

    void rref() throws Integers.NoInverseExists {
        // echelon form
        for (int i = 0; i < N; i++) {
            // select the pivot row
            int j;
            for (j = i; j < N; j++)
                if (!values[j][i].equals(BigInteger.ZERO))
                    break;
            // if the whole column is zeroes then there are not enough pivots => by IMT, matrix is not invertible
            if (j == N)
                throw new Integers.NoInverseExists("no inverse exists in R");

            // swap the pivot row into position
            BigInteger[] rowi = values[i];
            values[i] = values[j];
            values[j] = rowi;

            // zero under the pivot
            for (j = i + 1; j < N; j++)
                zeroByPivot(i, j);
            System.out.println(i);
            System.out.println(Arrays.toString(values[i]));
        }

        // diagonalize
        for (int i = N - 1; i >= 0; i--)
            // zero above the pivot
            for (int j = i - 1; j >= 0; j--)
                zeroByPivot(i, j);
    }

    Polynomial findInverse(Integers z) throws Integers.NoInverseExists {
        // bring into reduced echelon and Z/pZ
        int[] inverted = new int[N];
        for (int i = 0; i < N; i++) {
            BigInteger pivot = values[i][i], adjunct = values[i][N], g = pivot.gcd(adjunct), modulus = BigInteger.valueOf(z.modulus);
            // reduce the fraction and bring into Z/pZ
            pivot = pivot.divide(g).mod(modulus); adjunct = adjunct.divide(g).mod(modulus);
            int p = pivot.intValueExact(), a = adjunct.intValueExact();

            inverted[i] = z.mod(a * z.inverse(p));
        }
        return new Polynomial(inverted);
    }
}