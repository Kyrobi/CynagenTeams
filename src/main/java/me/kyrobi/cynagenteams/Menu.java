package me.kyrobi.cynagenteams;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.datatypes.party.Party;
import net.essentialsx.api.v2.services.mail.MailSender;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static me.kyrobi.cynagenteams.CynagenTeams.getPluginInstance;
import static me.kyrobi.cynagenteams.Datastore.myDataStore;
import static me.kyrobi.cynagenteams.Util.*;

public class Menu {
    public static void showGListings(Player player){
        // Create the main GUI with 6 rows
        ChestGui gui = new ChestGui(6, "Cynagen's Recruitment Board");

        List<ItemStack> items = new ArrayList<>();

        List<ListingData> listings = new ArrayList<>(myDataStore.values());

        // Newest listings first
        listings.sort(Comparator.comparingLong(ListingData::getCreationDate).reversed());

        for(ListingData listing: listings){

            ItemStack itemStack = new ItemStack(Material.PAPER);

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.GOLD + listing.getPartyName());

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Description");
            lore.add(ChatColor.GRAY + "------------");
            lore.add(" ");
            descTrimmer(lore, listing.getDescription());
            lore.add(" ");
            lore.add(ChatColor.GRAY + "------------");

            Party party = listing.getParty();
            if(party == null){
                continue;
            }
            OfflinePlayer partyLeader = Bukkit.getOfflinePlayer(party.getLeader().getUniqueId());
            lore.add(ChatColor.GRAY + "Party Name: " + ChatColor.WHITE + party.getName());
            lore.add(ChatColor.GRAY + "Leader: " + ChatColor.WHITE + partyLeader.getName());
            lore.add(ChatColor.GRAY + "Leader Last Online: " + ChatColor.WHITE + timeAgo(partyLeader.getLastLogin()));
            lore.add(ChatColor.GRAY + "Members: " + ChatColor.WHITE + listing.getParty().getMembers().size() + "/" + PartyAPI.getMaxPartySize());
            lore.add(ChatColor.GRAY + "Party Level: " + ChatColor.WHITE + party.getLevel());
            lore.add(ChatColor.GRAY + "Members online: " + ChatColor.WHITE + party.getOnlineMembers().size());
            lore.add(ChatColor.GRAY + "Date Listed: " + ChatColor.WHITE + formatCreationDate(listing.getCreationDate()));
            lore.add(ChatColor.GRAY + " ");
            lore.add(ChatColor.GREEN + "" + ChatColor.BOLD + "Click to join!");

            itemMeta.setLore(lore);

            setNameToPDC(itemMeta, listing.getPartyName());
            itemStack.setItemMeta(itemMeta);
            items.add(itemStack);
        }

        // Create the paginated pane for content (5 rows)
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 4);

        // Add your items to the 'items' list here
        pages.populateWithItemStacks(items);

        // Add click handler for items
        pages.setOnClick(event -> {
            event.setCancelled(true);

            if(event.getCurrentItem() == null){
                return;
            }

            Player playerClicked = (Player) event.getWhoClicked();
            String name = getNameFromPDC(event.getCurrentItem().getItemMeta());
            Party party = getPartyFromName(name);
            if(party == null) {
                playerClicked.sendMessage(ChatColor.RED + "This party is no longer available.");
                return;
            }

            // Check if the party is full
            if(party.getMembers().size() >= PartyAPI.getMaxPartySize()){
                playerClicked.sendMessage(ChatColor.RED + "This party is full.");
                return;
            }

            // Check if the user is already in this party
            if(party.getMembers().containsKey(playerClicked.getUniqueId())){
                playerClicked.sendMessage(ChatColor.RED + "You are already in this party.");
                return;
            }

            // Check if the user is already in a different party
            if(PartyAPI.getPartyName(playerClicked) != null){
                playerClicked.sendMessage(ChatColor.RED + "You are already in a party.");
                return;
            }

            getEssentialsAPI().getUser(party.getLeader().getUniqueId()).addMail( "\n" + player.getName() + " has joined your party from the recruitment board! \n");
            PartyAPI.addToParty(playerClicked, party.getName(), false);
            playerClicked.sendMessage(ChatColor.GREEN + "You joined " + party.getName() + "!");
            ListingData partyData = myDataStore.get(party.getName());

            Bukkit.getScheduler().runTaskLater(getPluginInstance(), ()->{
                playerClicked.sendMessage(ChatColor.GRAY + "----------------");
                playerClicked.sendMessage(ChatColor.GRAY + " ");
                playerClicked.sendMessage(ChatColor.GOLD + "Party Message:");
                playerClicked.sendMessage(ChatColor.WHITE + partyData.getMotd());
                playerClicked.sendMessage(ChatColor.GRAY + " ");
                playerClicked.sendMessage(ChatColor.GRAY + "----------------");
            }, 20 * 5);
        });

        gui.addPane(pages);

        // Create black glass background for navigation bar
        OutlinePane background = new OutlinePane(0, 4, 9, 1);

        ItemStack borderBlock = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderBlock.getItemMeta();
        borderMeta.setDisplayName(ChatColor.GRAY + "-");
        borderBlock.setItemMeta(borderMeta);
        background.addItem(new GuiItem(borderBlock, event -> event.setCancelled(true)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);
        gui.addPane(background);

        // Create navigation pane
        StaticPane navigation = new StaticPane(0, 5, 9, 1);

        // Previous page button
        ItemStack previousButton = new ItemStack(Material.ARROW);
        ItemMeta previousMeta = previousButton.getItemMeta();
        previousMeta.setDisplayName(ChatColor.GRAY + "Previous Page");
        previousButton.setItemMeta(previousMeta);
        navigation.addItem(new GuiItem(previousButton, event -> {
            event.setCancelled(true);
            if (pages.getPage() > 0) {
                pages.setPage(pages.getPage() - 1);
                gui.update();
            }
        }), 0, 0);

        /*
        Leaderboard
         */
        ItemStack leaderboardButton = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta leaderboardMeta = leaderboardButton.getItemMeta();

        List<String> loreleaderboard = new ArrayList<>();

        loreleaderboard.add(ChatColor.GREEN + "Party with the highest levels");

        leaderboardMeta.setLore(loreleaderboard);

        leaderboardMeta.setDisplayName(ChatColor.GOLD + "Party Leaderboard");
        leaderboardButton.setItemMeta(leaderboardMeta);
        navigation.addItem(new GuiItem(leaderboardButton, event -> {
            event.setCancelled(true);
            showLeaderboard(player);
        }), 3, 0);



        // Guide Button
        ItemStack guideButton = new ItemStack(Material.BOOK);
        ItemMeta guideMeta = guideButton.getItemMeta();

        List<String> lore = new ArrayList<>();

        lore.add(ChatColor.GRAY + "Information");
        lore.add(" ");

        lore.add(ChatColor.GREEN + "List Party To Recruitment Board");
        lore.add(ChatColor.WHITE + "/teams add");
        lore.add(ChatColor.GRAY + "Adds your party to this list for");
        lore.add(ChatColor.GRAY + "others to find and join.");
        lore.add(ChatColor.GRAY + "(You need to be party leader)");
        lore.add(" ");

        lore.add(ChatColor.GREEN + "Remove Party From Recruitment Board");
        lore.add(ChatColor.WHITE + "/teams remove");
        lore.add(ChatColor.GRAY + "Removes your party from this list.");
        lore.add(ChatColor.GRAY + "(You need to be party leader)");
        lore.add(" ");

        lore.add(ChatColor.GREEN + "Change Party Description");
        lore.add(ChatColor.WHITE + "/teams description <desc>");
        lore.add(ChatColor.GRAY + "Example:");
        lore.add(ChatColor.GRAY + "/teams description my awesome party!");
        lore.add(" ");

        lore.add(ChatColor.GREEN + "Change Party Message");
        lore.add(ChatColor.WHITE + "/teams message <message>");
        lore.add(ChatColor.GRAY + "Set a message for when players join");
        lore.add(ChatColor.GRAY + "your party. For example, putting info");
        lore.add(ChatColor.GRAY + "on where to meet up.");
        lore.add(" ");

        guideMeta.setLore(lore);

        guideMeta.setDisplayName(ChatColor.GOLD + "Guide");
        guideButton.setItemMeta(guideMeta);
        navigation.addItem(new GuiItem(guideButton, event -> {
            event.setCancelled(true);
        }), 4, 0);

        // Next page button
        ItemStack teleportButton = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleportButton.getItemMeta();

        List<String> loreTeleport = new ArrayList<>();

        loreTeleport.add(ChatColor.GRAY + "You can set a warp with ");
        loreTeleport.add(ChatColor.GOLD + "/pwarp set <name> ");
        loreTeleport.add(ChatColor.GRAY + " ");
        loreTeleport.add(ChatColor.GRAY + "You can use this warp as a");
        loreTeleport.add(ChatColor.GRAY + "warp for all your party");
        loreTeleport.add(ChatColor.GRAY + "members to meet up!");

        teleportMeta.setLore(loreTeleport);

        teleportMeta.setDisplayName(ChatColor.GOLD + "Party Warp");
        teleportButton.setItemMeta(teleportMeta);
        navigation.addItem(new GuiItem(teleportButton, event -> {
            event.setCancelled(true);
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);
                gui.update();
            }
        }), 5, 0);

        // Next page button
        ItemStack nextButton = new ItemStack(Material.ARROW);
        ItemMeta nextMeta = nextButton.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GRAY + "Next Page");
        nextButton.setItemMeta(nextMeta);
        navigation.addItem(new GuiItem(nextButton, event -> {
            event.setCancelled(true);
            if (pages.getPage() < pages.getPages() - 1) {
                pages.setPage(pages.getPage() + 1);
                gui.update();
            }
        }), 8, 0);



        gui.addPane(navigation);
        gui.show(player);
    }

    public static void showLeaderboard(Player player){
        // Create the main GUI with 6 rows
        ChestGui gui = new ChestGui(6, "Cynagen Party Leaderboard");

        List<ItemStack> items = new ArrayList<>();

        List<Party> allParties = new ArrayList<>(PartyAPI.getParties());

        // Newest listings first
        allParties.sort(Comparator.comparingDouble(Party::getXp).reversed());

        int counter = 0;
        for(Party party: allParties){

            if(counter == 25){
                continue;
            }

            ItemStack itemStack = new ItemStack(Material.PAPER);

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName(ChatColor.WHITE + "#" + String.valueOf(counter + 1) + ". " + ChatColor.GOLD + party.getName());

            List<String> lore = new ArrayList<>();

            lore.add(ChatColor.GRAY + "Members: " + ChatColor.WHITE + party.getMembers().size() + "/" + PartyAPI.getMaxPartySize());
            lore.add(ChatColor.GRAY + "Party Level: " + ChatColor.WHITE + party.getLevel() + " " + ChatColor.GRAY + "(bugged i think)");
            lore.add(ChatColor.GRAY + "Party EXP: " + ChatColor.WHITE + addCommaToNumber((int)party.getXp()));

            itemMeta.setLore(lore);

            itemStack.setItemMeta(itemMeta);
            items.add(itemStack);
            ++counter;
        }

        // Create the paginated pane for content (5 rows)
        PaginatedPane pages = new PaginatedPane(0, 0, 9, 4);

        // Add your items to the 'items' list here
        pages.populateWithItemStacks(items);

        // Add click handler for items
        pages.setOnClick(event -> {
            event.setCancelled(true);
        });

        gui.addPane(pages);

        // Create black glass background for navigation bar
        OutlinePane background = new OutlinePane(0, 4, 9, 1);

        ItemStack borderBlock = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderBlock.getItemMeta();
        borderMeta.setDisplayName(ChatColor.GRAY + "-");
        borderBlock.setItemMeta(borderMeta);
        background.addItem(new GuiItem(borderBlock, event -> event.setCancelled(true)));
        background.setRepeat(true);
        background.setPriority(Pane.Priority.LOWEST);
        gui.addPane(background);

        // Create navigation pane
        StaticPane navigation = new StaticPane(0, 5, 9, 1);

        // Back Button
        ItemStack guideButton = new ItemStack(Material.BARRIER);
        ItemMeta guideMeta = guideButton.getItemMeta();

        guideMeta.setDisplayName(ChatColor.GOLD + "Back");
        guideButton.setItemMeta(guideMeta);
        navigation.addItem(new GuiItem(guideButton, event -> {
            event.setCancelled(true);
            showGListings(player);
        }), 4, 0);


        gui.addPane(navigation);
        gui.show(player);
    }
}
