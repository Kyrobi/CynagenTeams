package me.kyrobi.cynagenteams;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.datatypes.party.Party;
import com.ibm.icu.text.MessagePattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static me.kyrobi.cynagenteams.Datastore.myDataStore;
import static me.kyrobi.cynagenteams.Datastore.removePartyListing;
import static me.kyrobi.cynagenteams.Menu.showGListings;
import static me.kyrobi.cynagenteams.Menu.showLeaderboard;
import static me.kyrobi.cynagenteams.Util.getPartyFromName;

public class CommandHandler implements CommandExecutor {

    private CynagenTeams plugin;

    public CommandHandler(final CynagenTeams plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {

        Player player = (Player)commandSender;

        if(args.length == 0){
            showGListings(player);
            return true;
        }

//        Player player1 = Bukkit.getPlayer("MaddoxJKingsley");
//        PartyAPI.addToParty(player1, "Lemons", true);

        System.out.println("Args length: " + args.length);

        if(args.length >= 1){
            String option = args[0];
            System.out.println("Option: " + option);

            if(option.equals("leaderboard")){
                showLeaderboard(player);
                return false;
            }

            if(!PartyAPI.inParty(player)){
                player.sendMessage(ChatColor.RED + "You need to be a in party to use this command!");
                return false;
            }

            String partyName = PartyAPI.getPartyName(player);
            if(!getPartyFromName(partyName).getLeader().getUniqueId().toString().equals(player.getUniqueId().toString())){
                player.sendMessage(ChatColor.RED + "You need to be a party leader to use this command!");
                return false;
            }


            if(option.equals("add")){
                myDataStore.put(partyName, new ListingData(partyName));
                player.sendMessage(ChatColor.GREEN + "Added your party to the recruitment board!");
                return false;
            }

            if(option.equals("remove")){
                myDataStore.remove(partyName);
                removePartyListing(partyName);
                player.sendMessage(ChatColor.GREEN + "Removed your party form the recruitment board!");
                return false;
            }

            ListingData data = myDataStore.get(partyName);
            if(data == null){
                player.sendMessage(ChatColor.RED + "You need to list your party first!");
                return false;
            }

            if(option.equals("description") && args.length >= 2){
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 1; i < args.length; i++){
                    stringBuilder.append(args[i] + " ");
                }
                data.setDescription(stringBuilder.toString().trim());
                player.sendMessage(ChatColor.GREEN + "Listing description set!");
                return false;
            }

            if(option.equals("message") && args.length >= 2){
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 1; i < args.length; i++){
                    stringBuilder.append(args[i] + " ");
                }
                data.setMotd(stringBuilder.toString());
                player.sendMessage(ChatColor.GREEN + "Party message set! Your message is:");
                player.sendMessage(ChatColor.GRAY + stringBuilder.toString());
                return false;
            }
        }

        return false;
    }


    private boolean canUserModifyParty(Player player, String partyName){
        for(Party party: PartyAPI.getParties()){
            if(party.getLeader().getUniqueId().toString().equals(player.getUniqueId().toString()) && party.getName().equals(partyName)){

            }
        }
        return false;
    }

}
