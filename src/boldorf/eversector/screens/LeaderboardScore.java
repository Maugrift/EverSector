package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import static boldorf.eversector.Main.DISPLAYED_SCORES;
import boldorf.eversector.screens.EndScreen;
import static boldorf.eversector.screens.EndScreen.COLOR_HEADER;
import boldorf.eversector.storage.Paths;
import boldorf.util.FileManager;
import boldorf.util.Utility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * A score on the leaderboard, consisting of a ship name, credits, and
 * reputation.
 */
public class LeaderboardScore implements Comparable<LeaderboardScore>
{
    public static final String INVALID = "INVALID";
    
    private String  shipName;
    private Integer score;
    private Integer turns;
    private Integer kills;
    private Integer sectors;
    private String  reputation;
    private boolean leader;
    
    public LeaderboardScore(Properties properties)
    {
        if (properties == null || properties.isEmpty())
            throw new IllegalArgumentException();
        
        shipName   = properties.getProperty("shipName");
        score      = Utility.parseInt(properties.getProperty("score"));
        turns      = Utility.parseInt(properties.getProperty("turns"));
        kills      = Utility.parseInt(properties.getProperty("kills"));
        sectors    = Utility.parseInt(properties.getProperty("sectors"));
        reputation = properties.getProperty("reputation");
        leader     = "true".equals(properties.getProperty("leader"));
    }
    
    /**
     * Creates a new LeaderboardScore from its three components.
     * @param shipName the name of the ship to attribute the score to
     * @param score the score
     * @param turns the number of turns played
     * @param kills the number of ships destroyed
     * @param sectors the number of sectors discovered
     * @param reputation the reputation
     * @param leader true if the player was a leader
     */
    public LeaderboardScore(String shipName, int score, int turns, int kills,
            int sectors, String reputation, boolean leader)
    {
        this.shipName   = shipName;
        this.score      = score;
        this.turns      = turns;
        this.kills      = kills;
        this.sectors    = sectors;
        this.reputation = reputation;
        this.leader     = leader;
    }
    
    /**
     * Creates a new LeaderboardScore without a ship name accompanying it.
     * @param score the score
     * @param turns the number of turns played
     * @param kills the number of ships destroyed
     * @param sectors the number of sectors discovered
     * @param reputation the reputation of the score
     * @param leader true if the player was a leader
     */
    public LeaderboardScore(int score, int turns, int kills, int sectors,
            String reputation, boolean leader)
        {this(null, score, turns, kills, sectors, reputation, leader);}
    
    @Override
    public String toString()
    {
        if (!isValid())
            return INVALID;
        
        StringBuilder builder = new StringBuilder();
        
        if (shipName != null)
            builder.append(shipName).append(": ");
        
        builder.append(score).append(" Credits, ")
                .append(turns).append(" Turns, ")
                .append(sectors).append(" Sectors, ");
        
        if (kills > 0)
        {
            builder.append(kills).append(" ")
                    .append(Utility.makePlural("Kill", kills)).append(", ");
        }
        
        builder.append(reputation);
        if (leader)
            builder.append(" (Leader)");
        
        return builder.toString();
    }
    
    /**
     * Returns a Properties object representing the score.
     * @return a Properties object representing the score
     */
    public Properties toProperties()
    {
        Properties properties = new Properties();
        
        if (shipName != null)
            properties.setProperty("shipName", shipName);
        
        properties.setProperty("score",      score.toString());
        properties.setProperty("turns",      turns.toString());
        properties.setProperty("kills",      kills.toString());
        properties.setProperty("sectors",    sectors.toString());
        properties.setProperty("reputation", reputation);
        properties.setProperty("leader",     Boolean.toString(leader));
        return properties;
    }

    /**
     * Returns true if the two required fields (score and reputation) are set.
     * @return true if the score and reputation of the object are not their
     * default values
     */
    public boolean isValid()
    {
        return score != null && turns != null && kills != null &&
                sectors != null && reputation != null;
    }
    
    @Override
    public int compareTo(LeaderboardScore other)
        {return Integer.compare(score, other.score);}
    
    public static List<ColorString> buildLeaderboard()
    {
        List<ColorString> leaderboard = new LinkedList<>();
        FileManager.createContainingFolders(Paths.LEADERBOARD);
        List<LeaderboardScore> scores = getLeaderboardScores();
        
        if (scores == null || scores.isEmpty())
            return leaderboard;
        
        leaderboard.add(buildLeaderboardHeader(scores.size()));
        for (int i = 0; i < Math.min(scores.size(), DISPLAYED_SCORES); i++)
            leaderboard.add(new ColorString(scores.get(i).toString()));
        
        return leaderboard;
    }
    
    public static ColorString buildLeaderboardHeader(int nScores)
    {
        ColorString header = new ColorString("LEADERBOARD", COLOR_HEADER);
        if (nScores > DISPLAYED_SCORES)
            header.add(" (" + nScores + " Scores Total)");
        return header;
    }
    
    /**
     * Returns a sorted list of every leaderboard score.
     * @return an ArrayList of Integers parsed from the leaderboard file and
     * sorted from greatest to least
     */
    public static List<LeaderboardScore> getLeaderboardScores()
    {
        List<LeaderboardScore> scores = new ArrayList<>();
        
        try
        {
            int index = 1;
            while (scores.add(new LeaderboardScore(FileManager.load(
                    Paths.LEADERBOARD + "score_" + index + ".properties"))))
            {
                index++;
            }
        }
        catch (IllegalArgumentException | IOException e) {}
        // Do nothing, but stop the loop
        
        // Sort scores from highest to lowest
        scores.sort(Comparator.reverseOrder());
        return scores;
    }
}