package crypto;

/**
 * An interface for different Ciphertext classes so that basic functionality of
 * ciphertexts is maintained.
 *
 * @author Matthew Kindy II, Matt Bernhard
 */
public interface ICiphertext {


    /**
     * @return the data contained in this ciphertext
     */
    public byte[] getData();
}