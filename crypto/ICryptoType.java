package crypto;

import crypto.exceptions.BadKeyException;
import crypto.exceptions.KeyNotLoadedException;

import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.security.InvalidKeyException;
import java.security.Key;

/**
 * A wrapper for basic cryptographic functionality found in Java APIs, e.g.
 * javax.crypto, as well as in third party APIs, such as org.bouncycastle.crypto
 *
 * Created by Matthew Kindy II and Matt Bernhard on 11/3/2014.
 */
public interface ICryptoType {

    /**
     * Decrypts a cipherText generated by this ICryptoType.
     *
     * @param cipherText    a byte array to be decrypted
     * @return              the plaintext decrypted from the ciphertext
     *
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.BadPaddingException
     */
    public byte[] decrypt(byte[] cipherText) throws InvalidKeyException, KeyNotLoadedException, CipherException;

    /**
     * Encrypts a plainText according to the ICryptoType's protocol.
     *
     * @param plainText     a byte array to be encrypted
     * @return              the ciphertext of the plaintext according to the protocol
     *
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws java.security.InvalidKeyException
     * @throws javax.crypto.BadPaddingException
     */
    public byte[] encrypt(byte[] plainText) throws InvalidKeyException, KeyNotLoadedException, CipherException;

    /**
     * Given a filePath, extracts the Keys from the file and checks if they are proper keys for the
     * protocol. If they are, calls the proper submethod, else throws a BadKeyException.
     *
     * @param filePaths      a String array specifying the locations of the IKeys to be loaded
     *
     * @throws BadKeyException if the ICryptoType does not support the type of Key that was loaded
     * @throws NoSuchFileException if the file could not be found
     */
    public void loadKeys(String[] filePaths) throws BadKeyException, FileNotFoundException;

    /**
     * Given an array of Keys, checks if they are proper keys for the protocol. If they are, calls the
     * proper submethod, else throws a BadKeyException.
     *
     * @param keys      a Key or Key array
     *
     * @throws BadKeyException if the ICryptoType does not support the type of Key that was loaded
     */
    public void loadKeys(Key... keys) throws BadKeyException;

    public String toString();

}
