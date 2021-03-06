package crypto;

import java.util.Map;

/**
 * Plaintext representation of the selection made for a given race.
 *
 * Created by Matthew Kindy II on 11/19/2014.
 */
public class PlaintextRaceSelection extends ARaceSelection {

    private Map<String, Integer> voteMap;

    public PlaintextRaceSelection(Map<String, Integer> voteMap, String title, int size) {
        super(title,size);
        this.voteMap = voteMap;
    }

    public Map<String, Integer> getRaceSelectionsMap(){
        return voteMap;
    }

    public String getTitle(){
        return title;
    }

}
