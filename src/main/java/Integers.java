import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Integers {
    final int modulus;
    final int shift;
    Map<Integer, Integer> inverseCache = new HashMap<>();
    Integers(int m) {
        modulus = m;
        shift = (modulus / 2);
    }

    public static class NoInverseExists extends Exception {
        NoInverseExists(String message) {super(message);}
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
            throw new NoInverseExists("0 has no inverse mod " + modulus);
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
