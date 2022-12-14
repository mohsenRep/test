package it.unisa.dia.gas.crypto.engines.kem;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.InvalidCipherTextException;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.cert.Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

/**
 * @author Angelo De Caro (angelo.decaro@gmail.com)
 */
public class KEMCipher {
    protected Cipher cipher;
    protected KeyEncapsulationMechanism kem;

    public KEMCipher(Cipher cipher, KeyEncapsulationMechanism kem) {
        this.cipher = cipher;
        this.kem = kem;
    }


    public byte[] init(boolean forEncryption, CipherParameters cipherParameters) throws GeneralSecurityException, CryptoException {
        byte[][] kemOutput = initKEM(forEncryption, cipherParameters);
        initCipher(forEncryption, cipherParameters, kemOutput[0]);

        return kemOutput[1];
    }


    public byte[] update(byte[] bytes) {
        return cipher.update(bytes);
    }


    public byte[] update(byte[] bytes, int i, int i1) {
        return cipher.update(bytes, i, i1);
    }


    public int update(byte[] bytes, int i, int i1, byte[] bytes1) throws ShortBufferException {
        return cipher.update(bytes, i, i1, bytes1);
    }


    public int update(byte[] bytes, int i, int i1, byte[] bytes1, int i2) throws ShortBufferException {
        return cipher.update(bytes, i, i1, bytes1, i2);
    }


    public int update(ByteBuffer byteBuffer, ByteBuffer byteBuffer1) throws ShortBufferException {
        return cipher.update(byteBuffer, byteBuffer1);
    }


    public byte[] doFinal() throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal();
    }

    public int doFinal(byte[] bytes, int i) throws IllegalBlockSizeException, ShortBufferException, BadPaddingException {
        return cipher.doFinal(bytes, i);
    }


    public byte[] doFinal(byte[] bytes) throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(bytes);
    }

    public byte[] doFinal(byte[] bytes, int i, int i1) throws IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(bytes, i, i1);
    }

    public int doFinal(byte[] bytes, int i, int i1, byte[] bytes1) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(bytes, i, i1, bytes1);
    }

    public int doFinal(byte[] bytes, int i, int i1, byte[] bytes1, int i2) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(bytes, i, i1, bytes1, i2);
    }

    public int doFinal(ByteBuffer byteBuffer, ByteBuffer byteBuffer1) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        return cipher.doFinal(byteBuffer, byteBuffer1);
    }

    public Provider getProvider() {
        return cipher.getProvider();
    }

    public String getAlgorithm() {
        return cipher.getAlgorithm();
    }

    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    public int getOutputSize(int i) {
        return cipher.getOutputSize(i);
    }

    public byte[] getIV() {
        return cipher.getIV();
    }

    public AlgorithmParameters getParameters() {
        return cipher.getParameters();
    }

    public ExemptionMechanism getExemptionMechanism() {
        return cipher.getExemptionMechanism();
    }

    public void init(int i, Key key) throws InvalidKeyException {
        cipher.init(i, key);
    }

    public void init(int i, Key key, SecureRandom random) throws InvalidKeyException {
        cipher.init(i, key, random);
    }

    public void init(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec) throws InvalidKeyException, InvalidAlgorithmParameterException {
        cipher.init(i, key, algorithmParameterSpec);
    }

    public void init(int i, Key key, AlgorithmParameterSpec algorithmParameterSpec, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        cipher.init(i, key, algorithmParameterSpec, random);
    }

    public void init(int i, Key key, AlgorithmParameters algorithmParameters) throws InvalidKeyException, InvalidAlgorithmParameterException {
        cipher.init(i, key, algorithmParameters);
    }

    public void init(int i, Key key, AlgorithmParameters algorithmParameters, SecureRandom random) throws InvalidKeyException, InvalidAlgorithmParameterException {
        cipher.init(i, key, algorithmParameters, random);
    }

    public void init(int i, Certificate certificate) throws InvalidKeyException {
        cipher.init(i, certificate);
    }

    public void init(int i, Certificate certificate, SecureRandom random) throws InvalidKeyException {
        cipher.init(i, certificate, random);
    }


    protected byte[][] initKEM(boolean forEncryption, CipherParameters cipherParameters) throws InvalidCipherTextException{
        byte[] key, encapsulation;

        if (forEncryption) {
            KEMCipherEncryptionParameters parameters = (KEMCipherEncryptionParameters) cipherParameters;

            // encapsulate
            kem.init(forEncryption, parameters.getKemCipherParameters());
            byte[] output = kem.processBlock(new byte[0], 0, 0);

            int strength = (parameters.getCipherKeyStrength() + 7) / 8;
            if (kem.getKeyBlockSize() < strength)
                throw new InvalidCipherTextException("Cipher strength too high for the passed KEM.");

            key = Arrays.copyOfRange(output, 0, strength);
            encapsulation = Arrays.copyOfRange(output, kem.getKeyBlockSize(), output.length);
        } else {
            // get the encapsulation
            KEMCipherDecryptionParameters parameters = (KEMCipherDecryptionParameters) cipherParameters;
            encapsulation = parameters.getEncapsulation();

            // extract the key
            kem.init(forEncryption, parameters.getKemCipherParameters());

            int strength = (parameters.getCipherKeyStrength() + 7) / 8;
            if (kem.getKeyBlockSize() < strength)
                throw new InvalidCipherTextException("Cipher strength too high for the passed KEM.");

            key = Arrays.copyOfRange(kem.processBlock(encapsulation, 0, encapsulation.length), 0, strength);
            encapsulation = null;
        }

        return new byte[][]{key, encapsulation};
    }


    protected void initCipher(boolean forEncryption, CipherParameters cipherParameters, byte[] key) throws InvalidKeyException {
        cipher.init(
                forEncryption ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                new SecretKeySpec(key, cipher.getAlgorithm())
        );
    }

}
