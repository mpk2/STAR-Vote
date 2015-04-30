package crypto.adder;

import crypto.AHomomorphicCiphertext;
import crypto.EncryptedRaceSelection;
import crypto.ExponentialElGamalCiphertext;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;

import java.util.*;

/**
 * Represents an Elgamal private key.
 *
 * @author David Walluck
 * @version $LastChangedRevision$ $LastChangedDate$
 * @since 0.0.1
 */
public class AdderPrivateKeyShare extends AdderKey {

    private AdderInteger x;

    /**
     * Creates a new PrivateKey with the specified parameter values.
     *
     * @param p     the prime
     * @param g     the generator
     * @param x     the private value
     * @param f     the message base
     */
    public AdderPrivateKeyShare(AdderInteger p, AdderInteger g, AdderInteger x, AdderInteger f) {
        super(p, g, f);
        this.x = x;
    }

    /**
     * Computes the authority's partial decryption of a sum. The
     * \f$i\f$th component of the partial decryption is computed
     * as \f$G_i ^ x\f$, where \f$G_i\f$ is the first component of
     * the \f$i\f$ th ciphertext in the sum.
     *
     * @param vote      the vote
     *
     * @return          the partial decryption of the given vote
     */
    public List<AdderInteger> partialDecrypt(EncryptedRaceSelection vote) {
        Map<String, AHomomorphicCiphertext> cipherList = vote.getRaceSelectionsMap();
        List<AdderInteger> resultList = new ArrayList<>(cipherList.size());

        for (AHomomorphicCiphertext ciphertext : cipherList.values())
            resultList.add(partialDecrypt((ExponentialElGamalCiphertext)ciphertext));

        return resultList;
    }

    public AdderInteger partialDecrypt(ExponentialElGamalCiphertext ciphertext) {

            AdderInteger bigG = ciphertext.getG();
            return bigG.pow(x);
    }


    /**
     * Returns the prime <tt>p</tt>.
     *
     * @return the prime <tt>p</tt>
     */
    public AdderInteger getP() {
        return p;
    }

    /**
     * Returns the sub-prime <tt>q</tt>.
     *
     * @return the sub-prime <tt>q</tt>.
     */
    public AdderInteger getQ() {
        return q;
    }

    /**
     * Returns the generator <tt>g</tt>.
     *
     * @return the generator <tt>g</tt>
     */
    public AdderInteger getG() {
        return g;
    }

    /**
     * Returns the private value <tt>x</tt>.
     *
     * @return the private value <tt>x</tt>
     */
    public AdderInteger getX() {
        return x;
    }

    /**
     * Returns the message base <tt>f</tt>.
     *
     * @return the message base <tt>f</tt>
     */
    public AdderInteger getF() {
        return f;
    }

    /**
     * Creates a <tt>PrivateKey</tt> from the string standard representation as
     * described in the {@link #toString} method.
     *
     * @param  s a string that specifies a <tt>PrivateKey</tt>
     * @return a <tt>PrivateKey</tt> with the specified values
     */
    public static AdderPrivateKeyShare fromString(String s) {
        StringTokenizer st = new StringTokenizer(s, "pgxf", true);

        try {
            if (!st.nextToken().equals("p")) {
                throw new InvalidPrivateKeyException("expected token: `p\'");
            }

            AdderInteger p = new AdderInteger(st.nextToken());

            if (!st.nextToken().equals("g")) {
                throw new InvalidPrivateKeyException("expected token: `g\'");
            }

            AdderInteger g = new AdderInteger(st.nextToken(), p);

            if (!st.nextToken().equals("x")) {
                throw new InvalidPrivateKeyException("expected token: `x\'");
            }

            AdderInteger x = new AdderInteger(st.nextToken(),
                                              p.subtract(AdderInteger.ONE)
                                              .divide(AdderInteger.TWO));

            if (!st.nextToken().equals("f")) {
                throw new InvalidPrivateKeyException("expected token: `f\'");
            }

            AdderInteger f = new AdderInteger(st.nextToken(), p);

            if (st.hasMoreTokens()) {
                throw new InvalidPrivateKeyException("too many tokens");
            }

            return new AdderPrivateKeyShare(p, g, x, f);
        } catch (NoSuchElementException | NumberFormatException nsee) {
            throw new InvalidPrivateKeyException(nsee.getMessage());
        }
    }

    /**
     * Returns a <code>String</code> object representing this
     * <code>PrivateKey</code>.
     * @return the string representation of this private key
     */
    public String toString() {

        return "p" + p.toString() + "g" + g.toString() + "x" + x.toString() + "f" + f.toString();
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @return the S-Expression equivalent of this PrivateKey
     */
    public ASExpression toASE(){
    	return new ListExpression(
    		StringExpression.makeString("private-key"),
    		p.toASE(),
    		g.toASE(),
    		x.toASE(),
    		f.toASE()
    	);
    }
    
    /**
     * Method for interop with VoteBox's S-Expression system.
     * 
     * @param ase - S-Expression representation of a PrivateKey
     * @return the PrivateKey equivalent of ase
     */
    public static AdderPrivateKeyShare fromASE(ASExpression ase){
    	ListExpression exp = (ListExpression)ase;
    	
    	if(!(exp.get(0).toString()).equals("private-key"))
    		throw new RuntimeException("Not private-key");
    	
    	AdderInteger p = AdderInteger.fromASE(exp.get(1));
    	AdderInteger g = AdderInteger.fromASE(exp.get(2));
    	AdderInteger x = AdderInteger.fromASE(exp.get(3));
    	AdderInteger f = AdderInteger.fromASE(exp.get(4));
    	
    	return new AdderPrivateKeyShare(p, g, x, f);
    }
}
