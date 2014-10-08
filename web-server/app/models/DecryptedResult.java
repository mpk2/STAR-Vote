package models;

import play.db.ebean.Model;
import supervisor.model.Ballot;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model of a DecryptedResult on the Web-Server. An entity of the database
 * that stores the encrypted running total Ballot and decrypted vote totals
 * for the candidates of a given precinct.
 *
 * Created by Matthew Kindy II on 10/5/2014.
 */

@Entity
public class DecryptedResult extends Model {

    /**
     * This member object is a finder that aids in retrieving DecryptedResults by their precinctIDs
     */
    public static Finder<Long, DecryptedResult> find = new Finder<>(Long.class, DecryptedResult.class);

    @Id
    public long id;

    public String precinctID;

    @OneToMany
    public Map<String, BigInteger> candidateResults = new HashMap<>();

    public Ballot precinctResultsBallot;

    /**
     * Constructor
     *
     * @param precinctID                the ID of the precinct of these results
     * @param candidateResults          the map of candidates to their vote totals
     * @param precinctResultsBallot     the encrypted totalled ballot for this precinct
     */
    public DecryptedResult(String precinctID, Map<String, BigInteger> candidateResults, Ballot precinctResultsBallot) {

        this.precinctID             = precinctID;
        this.candidateResults       = candidateResults;
        this.precinctResultsBallot  = precinctResultsBallot;
    }


    /**
     * @see play.db.ebean.Model.Finder#all()
     */
    public static List<DecryptedResult> all() {
        return find.all();
    }

    /**
     * Database lookup for a VotingRecord with the given precinctID.
     *
     * @param precinctID       the ID of the precinct from which this record was collected
     * @return                 the corresponding VoteRecord or null if non-existent
     */
    public static Map<String, BigInteger> getResults(String precinctID) {
        return find.where().ieq("precinctID", precinctID).findUnique().candidateResults;
    }

    public static Ballot getResultsBallot(String precinctID) {
        return find.where().ieq("precinctID", precinctID).findUnique().precinctResultsBallot;
    }

    /**
     * Store a VotingRecord into the database.
     *
     * @param record        record to be stored
     */
    public static void create(DecryptedResult record) {
        record.save();
    }

    /**
     * Remove a VoteRecord from a database
     *
     * @param record        record to be removed
     */
    public static void remove(DecryptedResult record){
        record.delete();
    }

    /**
     * @return appropriately formatted String representation
     */
    public String toString() {
        return "PrecinctID: " + precinctID + ", Candidate Results: " + candidateResults.toString() + ", Ballot Form: " + precinctResultsBallot.toString();
    }

}