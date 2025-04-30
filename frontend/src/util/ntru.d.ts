export declare class NTRU {
    constructor()
    getPublicKey():string
    setForeignKey(foreignKey: string): void
    encrypt(plaintext: string): string
    decrypt(ciphertext: string): string
}