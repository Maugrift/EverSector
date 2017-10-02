package boldorf.eversector.screens;

import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.MenuScreen;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.apwt.windows.PopupMenu;
import boldorf.apwt.windows.PopupWindow;
import boldorf.eversector.Main;
import boldorf.eversector.screens.PopupMaster;
import boldorf.eversector.ships.Reputation.ReputationRange;
import boldorf.eversector.ships.Ship;

import java.awt.event.KeyEvent;
import java.util.List;

import static boldorf.eversector.Main.*;

/**
 * The screen used to vote on faction leaders.
 *
 * @author Boldorf Smokebane
 */
public class VotingScreen extends MenuScreen<PopupMenu> implements WindowScreen<PopupWindow>, PopupMaster
{

    /**
     * Popup screen for the confirmation prompt.
     */
    private Screen popup;

    /**
     * Instantiates a new VotingScreen.
     */
    public VotingScreen()
    {
        super(new PopupMenu(new PopupWindow(Main.display, new Border(1), new Line(true, 1, 1)),
                COLOR_SELECTION_FOREGROUND, COLOR_SELECTION_BACKGROUND));
        pendingElection.gatherVotes();
        setUpMenu();
    }

    @Override
    public PopupWindow getWindow()
    {
        return (PopupWindow) getMenu().getWindow();
    }

    @Override
    public Screen getPopup()
    {
        return popup;
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (popup != null)
        {
            popup = popup.processInput(key);
            return this;
        }

        super.processInput(key);
        return this;
    }

    @Override
    public Screen onConfirm()
    {
        String selection = getMenu().getSelection().toString();
        popup = new VotingConfirmScreen(selection);
        return this;
    }

    @Override
    public Screen onCancel()
    {
        return new ElectionResultsScreen();
    }

    private void setUpMenu()
    {
        List<ColorString> contents = getWindow().getContents();
        contents.add(pendingElection.getDescription());
        contents.add(new ColorString("The candidates are listed below."));
        contents.add(new ColorString("Press ").add(new ColorString("enter", COLOR_FIELD)).add(
                " on a candidate to vote or ").add(new ColorString("escape", COLOR_FIELD)).add(" to abstain."));

        getWindow().addSeparator();
        for (int i = 0; i < pendingElection.getCandidates().size(); i++)
        {
            getMenu().getRestrictions().add(i + 4);
            Ship candidate = pendingElection.getCandidates().get(i);
            ReputationRange reputation = candidate.getReputation(pendingElection.getFaction()).getRange();
            contents.add(new ColorString(candidate.toString()).add(" ")
                                                              .add(new ColorString(
                                                                      "(" + reputation.getAdjective() + ")",
                                                                      reputation.getColor())));
        }

        getMenu().setSelectionIndex(4);
    }
}
