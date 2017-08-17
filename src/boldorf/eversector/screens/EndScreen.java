package boldorf.eversector.screens;

import asciiPanel.AsciiPanel;
import boldorf.util.FileManager;
import boldorf.apwt.Display;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.apwt.windows.PopupWindow;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.DISPLAYED_SCORES;
import static boldorf.eversector.Main.disqualified;
import static boldorf.eversector.Main.kills;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.optionIs;
import static boldorf.eversector.Main.player;
import static boldorf.eversector.Main.startGame;
import boldorf.eversector.ships.Reputation.ReputationRange;
import boldorf.eversector.ships.Ship;
import static boldorf.eversector.screens.StartScreen.getTitleArt;
import static boldorf.eversector.storage.Options.LEADERBOARD;
import static boldorf.eversector.storage.Options.OPTION_TRUE;
import boldorf.eversector.storage.Paths;
import boldorf.util.Utility;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * 
 */
public class EndScreen extends Screen implements WindowScreen<PopupWindow>
{
    /** The minimum number of turns that must pass before a score is logged. */
    public static final int MIN_TURNS = 10;
    
    public static final Color COLOR_SCORE = AsciiPanel.brightWhite;
    public static final Color COLOR_HEADER = AsciiPanel.brightYellow;
    
    private PopupWindow window;
    
    public EndScreen(Display display, ColorString message,
            boolean leaderboard)
    {
        super(display);
        window = new PopupWindow(display, new Border(2), new Line(true, 2, 1));
        if (leaderboard && optionIs(OPTION_TRUE, LEADERBOARD))
            setUpLeaderboard();
        setUpWindow(message);
    }
    
    public EndScreen(Display display, boolean leaderboard)
        {this(display, null, leaderboard);}

    @Override
    public void displayOutput()
    {
        getDisplay().writeCenter(getDisplay().getCenterY() - 
                getTitleArt().length / 2 - window.getContents().size() / 2 - 2,
                getTitleArt());
        window.display();
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (key.getKeyCode() == KeyEvent.VK_R)
        {
            try
            {
                return new StartScreen(getDisplay(), startGame());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }
            
            return null;
        }
        
        System.exit(0);
        return null;
    }

    @Override
    public PopupWindow getWindow()
        {return window;}
    
    private void setUpWindow(ColorString message)
    {
        List<ColorString> contents = window.getContents();
        
        if (message != null)
            contents.add(message);
        
        int shipValue = player.calculateShipValue();
        
        // If the player hasn't improved their ship, don't print it
        if (shipValue > Ship.BASE_VALUE)
        {
            contents.add(new ColorString("You owned a ship worth ")
                    .add(new ColorString(Integer.toString(shipValue),
                            COLOR_FIELD))
                    .add(" credits."));
        }
        
        // Print either leadership and reputation status
        if (player.isAligned() &&
                player.getReputation(player.getFaction()).get() != 0)
        {
            ReputationRange reputation = player.getReputation(
                    player.getFaction()).getRange();
            
            String playerArticle = player.isLeader() ? "the" :
                    Utility.getArticle(reputation.getAdjective());
            String playerTitle = player.isLeader() ? "leader" : "member";
            
            contents.add(new ColorString("You were " + playerArticle + " ")
                    .add(new ColorString(reputation.getAdjective()
                            .toLowerCase(), reputation.getColor())
                    .add(" " + playerTitle + " of the ")
                    .add(player.getFaction())).add("."));
        }
        else if (player.isPirate())
        {
            contents.add(new ColorString("You were a despicable pirate."));
        }
        else if (!player.isAligned())
        {
            contents.add(new ColorString("You were an unaligned wanderer."));
        }
        
        // If the player has discovered any sectors, print the amount
        int discoveredSectors = map.getNDiscoveredSectors();
        if (discoveredSectors > 5)
        {
            contents.add(new ColorString("You revealed ")
                    .add(new ColorString(Integer.toString(discoveredSectors),
                            COLOR_FIELD))
                    .add(" sectors."));
        }
        
        if (map.getTurn() == 0)
        {
            contents.add(new ColorString("Thanks for playing!"));
        }
        else
        {
            contents.add(new ColorString("Thanks for playing ")
                    .add(new ColorString(Integer.toString(map.getTurn()),
                            COLOR_FIELD))
                    .add(" " + Utility.makePlural("turn", map.getTurn())
                            + "!"));
        }
        
        contents.add(new ColorString("Press ")
                .add(new ColorString("r", COLOR_FIELD))
                .add(" to play again or anything else to quit."));
    }
    
    private void setUpLeaderboard()
    {
        List<ColorString> contents = window.getContents();
        
        if (disqualified)
        {
            window.getContents().addAll(LeaderboardScore.buildLeaderboard());
            contents.add(new ColorString("Your score has been disqualified due "
                    + "to debug command usage.", COLOR_SCORE));
        }
        else if (map.getTurn() <= MIN_TURNS)
        {
            window.getContents().addAll(LeaderboardScore.buildLeaderboard());
            contents.add(new ColorString("This game has been too short to log "
                    + "a score.", COLOR_SCORE));
        }
        else if (player.calculateShipValue() <= Ship.BASE_VALUE)
        {
            window.getContents().addAll(LeaderboardScore.buildLeaderboard());
            contents.add(new ColorString("You have not scored enough for a "
                    + "leaderboard entry.", COLOR_SCORE));
        }
        else
        {
            String reputationAdjective;
            if (player.isAligned())
            {
                reputationAdjective = player.getReputation(
                        player.getFaction()).getRange().getAdjective();
            }
            else if (player.isPirate())
            {
                reputationAdjective = "Pirate";
            }
            else
            {
                reputationAdjective = "Wanderer";
            }
            
            LeaderboardScore playerScore = new LeaderboardScore(
                    player.calculateShipValue(), map.getTurn(), kills,
                    map.getNDiscoveredSectors(), reputationAdjective,
                    player.isLeader());

            // Current player's score must be added after the leaderboard print
            // so that it isn't duplicated in the displayed list
            int nScores = printLeaderboard(playerScore);
            try
            {
                FileManager.save(playerScore.toProperties(),
                        Paths.LEADERBOARD + "score_" + nScores + ".properties");
            }
            catch (IOException e) {}
        }
        
        window.addSeparator();
    }
    
    /**
     * Prints the leaderboard as well as the given score, if separate.
     * @param playerScore the score to add to the leaderboard and print
     * regardless of position
     * @return the number of scores
     */
    private int printLeaderboard(LeaderboardScore playerScore)
    {
        List<ColorString> contents = window.getContents();
        List<LeaderboardScore> scores = LeaderboardScore.getLeaderboardScores();
        
        if (scores == null || scores.isEmpty())
        {
            contents.add(new ColorString("LEADERBOARD", COLOR_HEADER));
            contents.add(new ColorString(playerScore.toString(), COLOR_SCORE));
            return 1;
        }
        
        // Sort scores again
        scores.add(playerScore);
        scores.sort(Comparator.reverseOrder());
        
        boolean playerScoreNoted = false;
        contents.add(LeaderboardScore.buildLeaderboardHeader(scores.size()));
        
        for (int i = 0; i < Math.min(scores.size(), DISPLAYED_SCORES); i++)
        {
            ColorString scoreString = new ColorString(scores.get(i).toString());
            
            if (!playerScoreNoted && scores.get(i) == playerScore)
            {
                scoreString.setForeground(COLOR_SCORE);
                playerScoreNoted = true;
            }
            
            contents.add(scoreString);
        }
        
        int playerRank = scores.indexOf(playerScore);
        if (!playerScoreNoted)
        {
            if (playerRank > DISPLAYED_SCORES)
            {
                contents.add(new ColorString("("
                        + (playerRank - DISPLAYED_SCORES) + " more)",
                        AsciiPanel.brightBlack));
            }
            
            contents.add(new ColorString(playerScore.toString(), COLOR_SCORE));
        }
        
        return scores.size();
    }
}