package boldorf.eversector.actions;

import boldorf.eversector.Paths;
import boldorf.eversector.items.Module;
import boldorf.eversector.ships.Battle;
import boldorf.eversector.ships.Ship;
import boldorf.util.Utility;

import static boldorf.eversector.Main.addMessage;

public class Toggle implements Action
{
    public static final String SOUND_EFFECT_ENABLE = Paths.ON;
    public static final String SOUND_EFFECT_DISABLE = Paths.OFF;

    private final String module;

    public Toggle(String module)
    {
        this.module = module;
    }

    @Override
    public String canExecute(Ship actor)
    {
        if (actor == null)
        {
            return "Ship not found.";
        }

        Module moduleObj = actor.getModule(module);

        if (moduleObj == null)
        {
            return "The specified module was not found on the ship.";
        }

        String validateModule = actor.validateModule(module);
        if (validateModule != null)
        {
            return validateModule;
        }

        String effect = moduleObj.getEffect();

        if (effect == null)
        {
            return Utility.addCapitalizedArticle(moduleObj.getName()) + " cannot be activated.";
        }

        if (actor.hasFlag(effect))
        {
            return null;
        }

        return actor.validateResources(moduleObj.getActionResource(), moduleObj.getActionCost(),
                "activate " + moduleObj);
    }

    @Override
    public String execute(Ship actor)
    {
        String canExecute = canExecute(actor);
        if (canExecute != null)
        {
            return canExecute;
        }

        Module moduleObj = actor.getModule(module);
        String effect = moduleObj.getEffect();
        boolean activating = !actor.hasFlag(effect);
        if (activating)
        {
            actor.addFlag(effect);
        }
        else
        {
            actor.removeFlag(effect);
        }

        if (actor.isPlayer())
        {
            if (activating)
            {
                addMessage("Your " + moduleObj.toString().toLowerCase() + " has been deactivated.");
            }
            else
            {
                addMessage("Your " + moduleObj.toString().toLowerCase() + " has been activated and will drain " +
                           moduleObj.getActionCost() + " " + moduleObj.getActionResource().toLowerCase() +
                           " per turn of use.");
            }
        }
        else if (actor.isInBattle())
        {
            Battle battle = actor.getBattleLocation().getBattle();
            Ship player = actor.getLocation().getGalaxy().getPlayer();
            if (player != null && battle.getShips().contains(player))
            {
                addMessage(actor + " " + (activating ? "activates" : "deactivates") +
                           Utility.addArticle(module.toLowerCase()) + ".");
            }
        }

        return null;
    }
}
