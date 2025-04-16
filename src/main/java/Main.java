public class Main {

    public static void main(String[] args) {
        try {
            var ntru = NTRU.create(107, 107 / 3, 3, 64);
            var ciphertext = ntru.encode("hello");
            System.out.println(ciphertext);
            var plaintext = ntru.decode(ciphertext);
            System.out.println(plaintext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
