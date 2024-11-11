package me.kyrobi.cynagenteams;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static me.kyrobi.cynagenteams.Datastore.myDataStore;

public final class CynagenTeams extends JavaPlugin {

    public static CynagenTeams plugin;

    @Override
    public void onEnable() {
        plugin = this;

        this.saveDefaultConfig();

        Datastore.initialize();

        this.getCommand("teams").setExecutor((CommandExecutor)new CommandHandler(this));
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks((Plugin)this);
        HandlerList.unregisterAll((Plugin)this);

        Datastore.uninitialize();

        for(ListingData listing: myDataStore.values()){
            if(!listing.isPartyStillValid()){
                Datastore.removePartyListing(listing.getPartyName());
            }
        }
    }

    public static CynagenTeams getPluginInstance(){
        return plugin;
    }
}
