import org.teavm.jso.JSExport;

public class NTRU {
    private final piesarentsquare.ntru.NTRU ntru;

    @JSExport
    public NTRU() {
        this.ntru = piesarentsquare.ntru.NTRU.create(107, 14, 12, 5, 3, 64);
    }

    @JSExport
    public String getPublicKey() {
        return ntru.getPublicKey();
    }

    @JSExport
    public void setForeignKey(String foreignKey) {
        ntru.setForeignKey(foreignKey);
    }

    @JSExport
    public String encrypt(String plaintext) {
        return ntru.encrypt(plaintext);
    }

    @JSExport
    public String decrypt(String ciphertext) {
        return ntru.decrypt(ciphertext);
    }
}
