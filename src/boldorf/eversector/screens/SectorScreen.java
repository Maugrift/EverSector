package boldorf.eversector.screens;

import boldorf.apwt.screens.KeyScreen;
import boldorf.apwt.screens.Keybinding;
import boldorf.apwt.Display;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorString;
import boldorf.apwt.screens.Screen;
import boldorf.apwt.screens.WindowScreen;
import boldorf.apwt.windows.AlignedWindow;
import boldorf.apwt.windows.Border;
import boldorf.apwt.windows.Line;
import boldorf.eversector.Main;
import static boldorf.eversector.Main.COLOR_FIELD;
import static boldorf.eversector.Main.COLOR_SELECTION_BACKGROUND;
import static boldorf.eversector.Main.attackers;
import static boldorf.eversector.Main.map;
import static boldorf.eversector.Main.playSoundEffect;
import static boldorf.eversector.Main.player;
import boldorf.eversector.entities.Station;
import boldorf.eversector.map.Sector;
import static boldorf.eversector.storage.Paths.ENGINE;
import static boldorf.eversector.storage.Paths.MINE;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import squidpony.squidmath.Coord;

/** The Screen used for navigating Sectors. */
class SectorScreen extends Screen implements WindowScreen<AlignedWindow>,
        PopupMaster, KeyScreen
{
    private AlignedWindow window;
    private Screen popup;
    private int cursor;
    
    public SectorScreen(Display display)
    {
        super(display);
        window = new AlignedWindow(display, Coord.get(0, 0), new Border(2));
        cursor = 0;
    }

    @Override
    public void displayOutput()
    {
        setUpWindow();
        window.display();
        
        if (popup != null)
        {
            if (popup instanceof WindowScreen &&
                    ((WindowScreen) popup).getWindow() instanceof AlignedWindow)
            {
                ((AlignedWindow) ((WindowScreen) popup).getWindow())
                        .setLocation(Coord.get(1, window.getBottomRight().y
                                + 3));
            }
            
            popup.displayOutput();
        }
    }

    @Override
    public Screen processInput(KeyEvent key)
    {
        if (popup != null)
        {
            popup = popup.processInput(key);
            
            if (popup instanceof SectorScreen)
            {
                popup = null;
                return this;
            }
            
            return popup instanceof PlanetScreen ||
                    popup instanceof StationScreen ||
                    popup instanceof EndScreen ?
                    popup : this;
        }
        
        // This is necessary both here and below to avoid interruptions
        if (!attackers.isEmpty())
        {
            popup = new BattleScreen(getDisplay(), attackers.poll(), false);
            return this;
        }
        
        boolean nextTurn = false;
        Screen nextScreen = this;
        
        if (isLooking())
        {
            switch (key.getKeyCode())
            {
                case KeyEvent.VK_UP:
                    if (player.getSector().isValidOrbit(cursor - 1))
                        cursor--;
                    break;
                case KeyEvent.VK_DOWN:
                    if (player.getSector().isValidOrbit(cursor + 1))
                        cursor++;
                    break;
                case KeyEvent.VK_L: case KeyEvent.VK_ENTER:
                case KeyEvent.VK_ESCAPE:
                    cursor = 0;
                    break;
            }
            
            return this;
        }
        
        switch (key.getKeyCode())
        {
            case KeyEvent.VK_UP:
                if (player.orbit(false))
                {
                    nextTurn = true;
                    playSoundEffect(ENGINE);
                }
                break;
            case KeyEvent.VK_DOWN:
                if (player.orbit(true))
                {
                    nextTurn = true;
                    playSoundEffect(ENGINE);
                    if (!player.isInSector())
                        nextScreen = new MapScreen(getDisplay());
                }
                break;
            case KeyEvent.VK_LEFT:
                Station station =
                        player.getSector().getStationAt(player.getOrbit());
                if (station != null && player.isHostile(station.getFaction()) &&
                        player.canClaimStation(station, false))
                {
                    popup = new ClaimStationScreen(getDisplay());
                }
                else if (player.dock())
                {
                    nextTurn = true;
                    nextScreen = new StationScreen(getDisplay());
                    playSoundEffect(MINE);
                }
                break;
            case KeyEvent.VK_RIGHT:
                if (player.mine())
                {
                    playSoundEffect(MINE);
                    nextTurn = true;
                    break;
                }
                
                // Fix this creating a message when unable to mine an asteroid belt
                if (player.canLand())
                    popup = new LandScreen(getDisplay());
                break;
            case KeyEvent.VK_A:
            {
                if (!player.hasWeapons())
                    break;
                
                Sector sector = player.getSector();
                if (sector.getShipsAt(player.getOrbit()).size() <= 1)
                    break;
                
                if (sector.isCenter())
                {
                    popup = new CenterAttackScreen(getDisplay());
                }
                else if (sector.getShipsAt(player.getOrbit()).size() == 2)
                {
                    popup = new BattleScreen(getDisplay(),
                            sector.getFirstOtherShip(player), true);
                }
                else
                {
                    popup = new AttackScreen(getDisplay());
                }
                break;
            }
            case KeyEvent.VK_L:
                cursor = player.getOrbit();
                break;
        }
        
        if (nextTurn)
            map.nextTurn();
        
        if (!attackers.isEmpty())
        {
            popup = new BattleScreen(getDisplay(), attackers.poll(), false);
            return this;
        }
        
        return nextScreen;
    }
    
    @Override
    public List<Keybinding> getKeybindings()
    {
        List<Keybinding> keybindings = new ArrayList<>();
        keybindings.add(new Keybinding("change orbit", ExtChars.ARROW1_U,
                ExtChars.ARROW1_D));
        if (!isLooking())
        {
            keybindings.add(new Keybinding("land", ExtChars.ARROW1_R));
            keybindings.add(new Keybinding("dock", ExtChars.ARROW1_L));
            
            if (player.hasWeapons())
                keybindings.add(new Keybinding("attack", "a"));
        }
        keybindings.add(new Keybinding("look", "l"));
        return keybindings;
    }

    @Override
    public AlignedWindow getWindow()
        {return window;}
    
    @Override
    public Screen getPopup()
        {return popup;}
    
    @Override
    public boolean hasPopup()
        {return popup != null;}
    
    private boolean isLooking()
        {return cursor != 0;}
    
    private void setUpWindow()
    {
        List<ColorString> contents = window.getContents();
        Sector sector = Main.player.getSector();
        contents.clear();
        window.getSeparators().clear();
        
        contents.add(new ColorString(sector.toString()));
        contents.add(new ColorString("Location: ")
                .add(new ColorString(sector.getLocation().x + ","
                        + sector.getLocation().y, COLOR_FIELD)));
        contents.add(new ColorString("Star: ").add(sector.getStar()));
        contents.add(new ColorString("Ruler: ")
                .add(sector.isClaimed() ? sector.getFaction().toColorString() :
                new ColorString("Disputed", COLOR_FIELD)));
        
        window.addSeparator(new Line(true, 2, 1));
        for (int orbit = 1; orbit <= sector.getOrbits(); orbit++)
        {
            ColorString orbitSymbols = sector.getSymbolsForOrbit(orbit);
            if (cursor == orbit)
                orbitSymbols.setBackground(COLOR_SELECTION_BACKGROUND);
            contents.add(orbitSymbols);
        }
        
        window.addSeparator(new Line(false, 1, 2, 1));
        contents.addAll(sector.getOrbitContents(isLooking() ? cursor :
                player.getOrbit()));
    }
}