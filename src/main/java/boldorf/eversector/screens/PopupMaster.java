package boldorf.eversector.screens;

import boldorf.apwt.screens.Screen;

/**
 * A screen that manages popup screens.
 *
 * @author Boldorf Smokebane
 */
public interface PopupMaster
{
    /**
     * Gets the popup screen.
     *
     * @return the popup screen
     */
    Screen getPopup();

    /**
     * Returns true if the screen is currently displaying a popup scren.
     *
     * @return true if the screen is currently displaying a popup scren
     */
    default boolean hasPopup()
    {
        return getPopup() != null;
    }
}
