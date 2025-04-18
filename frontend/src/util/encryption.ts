export const encrypt = (plaintext: string): string => {
    return plaintext.split('').reverse().join('')
}

export const decrypt = (ciphertext: string): string => {
    return ciphertext.split('').reverse().join('')
}