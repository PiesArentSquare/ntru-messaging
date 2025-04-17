public class Main {

    public static void main(String[] args) {
        try {
            var ntru = NTRU.create(107, 14, 12, 5, 3, 64);
            var ciphertext = ntru.encrypt("hello there \uD83D\uDE00");
            System.out.println(ciphertext);
            var plaintext = ntru.decrypt(ciphertext);
            System.out.println(plaintext);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
