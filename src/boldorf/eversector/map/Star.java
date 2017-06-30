package boldorf.eversector.map;

import asciiPanel.AsciiPanel;
import boldorf.util.Utility;
import boldorf.apwt.ExtChars;
import boldorf.apwt.glyphs.ColorChar;
import boldorf.apwt.glyphs.ColorString;
import static boldorf.eversector.Main.rng;
import java.awt.Color;

/** A star that possesses a type and a power level. */
public enum Star
{
    YELLOW_DWARF("Yellow Dwarf", 7, false, AsciiPanel.brightYellow,
            ExtChars.STAR),
    RED_DWARF("Red Dwarf", 5, false, AsciiPanel.red, '*'),
    NEUTRON_STAR("Neutron Star", 8, true, AsciiPanel.brightCyan, '*'),
    SUPERGIANT("Supergiant", 10, true, AsciiPanel.brightRed, ExtChars.CIRCLE);
    
    private String  type;
    private int     power;
    private boolean nebular;
    private Color   color;
    private char    symbol;
    
    Star(String type, int power, boolean nebular, Color color, char symbol)
    {
        this.type    = type;
        this.power   = power;
        this.nebular = nebular;
        this.color   = color;
        this.symbol  = symbol;
    }
    
    @Override
    public String toString()
        {return type;}
    
    public ColorString toColorString()
        {return new ColorString(type, color);}
    
    public String getType()
        {return type;}
    
    public int getPower()
        {return power;}
    
    /**
     * Returns true if the star is large enough to be surrounded by nebulae.
     * @return true if nebulae will generate in the orbits of the star
     */
    public boolean isNebular()
        {return nebular;}
    
    /**
     * Returns the symbol corresponding to the type of the star.
     * @return the symbol representing the star type
     */
    public ColorChar getSymbol()
        {return new ColorChar(symbol, color);}
    
    /**
     * Calculates the power level of the star at a certain orbit.
     * @param orbit the orbit at which to calculate the star's power, must be a
     * valid orbit
     * @return the reduced power of the star at the orbit, -1 if the orbit is
     * invalid
     */
    public int getPowerAt(int orbit)
        {return orbit > 0 ? Math.max(0, power - (orbit - 1)) : -1;}
    
    /**
     * Calculates the amount of energy generated by a solar array at the given
     * orbit.
     * @param orbit the orbit at which to calculate the star's solar power, must
     * be a valid orbit
     * @return the power generated by a solar array at the given orbit of the
     * star, -1 if the orbit is invalid
     */
    public int getSolarPowerAt(int orbit)
        {return getPowerAt(orbit) / (RED_DWARF.power + 1) + 1;}
    
    /**
     * Randomly generates the type of the star.
     * @return the type of the star as a String
     */
    public static Star generate()
    {
        return (Star) Utility.select(rng,
                new Star[] {YELLOW_DWARF, RED_DWARF, NEUTRON_STAR, SUPERGIANT},
                new double[] {0.35, 0.35, 0.15, 0.15});
    }
}