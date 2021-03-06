package verifier.test;

import auditorium.*;
import crypto.adder.AdderPublicKey;
import sexpression.ASExpression;
import sexpression.ListExpression;
import sexpression.StringExpression;
import votebox.AuditoriumParams;
import votebox.events.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

/**
 * This will generate an auditorium log with a prescribed set of contents, so things like hash chaining and
 * verification can be tested.
 *
 * @author Matt Bernhard
 */
public class AuditoriumLogGenerator {

    /** This is the log object that we will use to generate the file */
    private static Log log;

    /** This will ensure that everything will be written to the log by the same host */
    private static HostPointer hp;

    /**
     * The top layer of our hosts, will be a temporal layer. We need a reference so we can directly build messages without
     * actually sending them over the network.
     */
    private static IAuditoriumLayer topLayer;

    /** For generating random strings like bid's*/
    private static Random rand;

    /** A decimal formatter for generating PINs */
    private static DecimalFormat format = new DecimalFormat("00000");

    /** Necessary for initializing our integrity layer as well as logging certain kinds of events, like EncryptedCastWithNIZKs */
    private static AdderPublicKey publicKey;


    /**
     * Set up the generator, including initializing the Log with the log file
     *
     */
    public static void setUp() {
        rand = new Random();

        AuditoriumHost host = new AuditoriumHost("0", new AuditoriumParams("test.conf"), "0000000000");
        log = host.getLog();

        IKeyStore ks = new SimpleKeyStore("keys");

        try { publicKey = ks.loadPEK(); }
        catch (AuditoriumCryptoException e) {e.printStackTrace();}

        topLayer = new AuditoriumIntegrityLayer(AAuditoriumLayer.BOTTOM, host, ks);



        hp = new HostPointer("0", "127.0.0.1", 9000);
    }

    /**
     * A utility method for logging event messages so that they are signed and given a temporal assignment
     *
     * @param datum the message to log
     * @throws IOException if the log fails in writing
     */
    private static void logDatum(ASExpression datum) throws IOException {

        /* Build a dag of messages (from TemporalLayer) */
        /* Make new datum - Wrap with everything that is in the last list. */
        ArrayList<ASExpression> list = new ArrayList<>();
        for (MessagePointer p : log.getLast())
            list.add(p.toASE());

        /* build the succeeds clause */
        ASExpression newDatum = new ListExpression(StringExpression.makeString("succeeds"), new ListExpression(list), datum);

        Message msg = new Message("announce", hp, "0", topLayer.makeAnnouncement(newDatum));

        log.logAnnouncement(msg);
    }

    /**
     * A utility message that will insert data in the log that invalidates the hash chain
     */
    private static void compromiseHashChain(ASExpression datum) throws IncorrectFormatException, IOException {
        log.logAnnouncementNoChain(new Message("announce", hp, "0", topLayer.makeAnnouncement(datum)));

    }

    /**
     * Utility method for generating a string of random numbers, for PINs and BID's
     */
    private static String getRandomString(){
        /* Generate a random PIN */
        return format.format(rand.nextInt(100000));
    }

    /**
     * A utility method for generating a random byte array, for ballot data
     */
    private static byte[] getBlob() {
        int n = (int) (Math.random() * 100);
        byte[] array = new byte[n];
        for (int i = 0; i < n; i++)
            array[i] = (byte) (Math.random() * 256);

        return array;
    }

    /**
     * A utility method for logging the election start up of a supervisor, votebox, and ballot scanner.
     * This will get us through the polls-opened phase of voting, so now PINs can be entered and voters can vote.
     */
    private static void start3Machines() throws IOException {
       /* Add all of the machines to the network and assign labels to them */
        SupervisorEvent supEvent = new SupervisorEvent(0, 0, "inactive");
        AssignLabelEvent label = new AssignLabelEvent(0, 1, 1);
        AssignLabelEvent label2 = new AssignLabelEvent(0, 2, 2);
        VoteBoxEvent voteboxEvent = new VoteBoxEvent(1, 1, "inactive", 1, 0, 0);
        BallotScannerEvent scanner = new BallotScannerEvent(2, 2, "inactive", 1, 0, 0);

        /* Activate the system */
        ArrayList<StatusEvent> events = new ArrayList<>();
        events.add(new StatusEvent(0, 0, supEvent));
        events.add(new StatusEvent(0, 1, voteboxEvent));
        events.add(new StatusEvent(0, 2, scanner));
        ActivatedEvent active = new ActivatedEvent(0, events);

        /* Open the polls */
        PollsOpenEvent polls = new PollsOpenEvent(0, 44, "keyword");

        logDatum(supEvent.toSExp());
        logDatum(label.toSExp());
        logDatum(label2.toSExp());
        logDatum(voteboxEvent.toSExp());
        logDatum(scanner.toSExp());
        logDatum(active.toSExp());
        logDatum(polls.toSExp());
    }

    /**
     * A utility method that captures the voting process for one voter, including
     * PIN entering, authorization, committing, scanning, and casting.
     */
    private static void vote() throws IOException {

        String precinct = "precinct";

        ASExpression nonce = StringExpression.make(getRandomString());

        /* Get some ballot data */
        byte[] ballot = getBlob();

        PINEnteredEvent pin = new PINEnteredEvent(1, "pin");

        AuthorizedToCastEvent authorize = new AuthorizedToCastEvent(0, 1, nonce, precinct, ballot);

        /* Generate a random bid for this voting session */
        String bid = getRandomString();

        /* The votebox now commits the ballot */
        CommitBallotEvent cbe = new CommitBallotEvent(1, nonce, ballot, bid, precinct);

        /* We announce that the ballot was received */
        BallotReceivedEvent bre = new BallotReceivedEvent(0, 1, nonce, bid, precinct);

        /* Now we scan the ballot and accept it */
        BallotScannedEvent bse = new BallotScannedEvent(2, bid);
        BallotScanAcceptedEvent bsae = new BallotScanAcceptedEvent(0, bid);

        EncryptedCastBallotWithNIZKsEvent ecbwne = new EncryptedCastBallotWithNIZKsEvent(0, nonce, ballot, bid,1);

        logDatum(pin.toSExp());
        logDatum(authorize.toSExp());
        logDatum(cbe.toSExp());
        logDatum(bse.toSExp());
        logDatum(bsae.toSExp());
        logDatum(ecbwne.toSExp());
        logDatum(bre.toSExp());
    }

    /**
     * Utility method to close out the election logging process
     */
    private static void close() throws IOException {
        /* we close the polls */
        PollsClosedEvent pce = new PollsClosedEvent(0, 1000);

        logDatum(pce.toSExp());
    }

    /**
     * A method that will simply write one event to a log
     */
    public static void generateSimpleLog() throws IOException, IncorrectFormatException {

        start3Machines();
        SupervisorEvent e = new SupervisorEvent(0, 0, "activated");

        logDatum(e.toSExp());

        close();

    }

    /**
     * Generates a sample log for a simplified supervisor. The log will reflect
     * that the supervisor was connected to one votebox and one scanner, and that
     * the votebox voted once and the ballot was cast.
     */
    public static void generateSimpleSupervisorLog() throws IOException, IncorrectFormatException {

        start3Machines();

        /* Cast one vote */
        vote();

        close();

    }

    public static void generateSimpleSupervisorCompromisedHashLog() throws IOException, IncorrectFormatException {
        start3Machines();

        /* Cast one vote */
        vote();

        compromiseHashChain((new SupervisorEvent(0, 0, "status")).toSExp());

        close();
    }

    public static void generateLotsOfVotesLog() throws IOException, IncorrectFormatException{

        start3Machines();

        for(int i = 0; i < 100; i++)
            vote();

        close();
    }
}
