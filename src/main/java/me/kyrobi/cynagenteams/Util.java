package me.kyrobi.cynagenteams;

import com.earth2me.essentials.Essentials;
import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.datatypes.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static me.kyrobi.cynagenteams.CynagenTeams.getPluginInstance;

public class Util {

    public static String getNameFromPDC(ItemMeta meta){
        NamespacedKey key = new NamespacedKey(getPluginInstance(), "partyName");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(key, PersistentDataType.STRING)) {
            return container.get(key, PersistentDataType.STRING);
        }

        return null;
    }

    public static void setNameToPDC(ItemMeta meta, String partyName){
        NamespacedKey key = new NamespacedKey(getPluginInstance(), "partyName");
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, partyName);
    }

    public static void descTrimmer(List<String> lore, String desc){
        int lineLength = 36;
        for (int i = 0; i < desc.length(); i += lineLength) {
            // Check if the remaining part is less than line length
            if (i + lineLength < desc.length()) {
                lore.add(ChatColor.DARK_AQUA + desc.substring(i, i + lineLength));
            } else {
                // Add the last part of the string
                lore.add(ChatColor.DARK_AQUA + desc.substring(i));
            }
        }
    }

    public static String timeAgo(long epochMillis) {
        long currentTime = System.currentTimeMillis();
        long duration = currentTime - epochMillis;

        if (duration < 0) {
            return "In the future"; // In case the provided time is in the future
        }

        long years = TimeUnit.MILLISECONDS.toDays(duration) / 365;
        long months = TimeUnit.MILLISECONDS.toDays(duration) % 365 / 30;
        long days = TimeUnit.MILLISECONDS.toDays(duration) % 365 % 30;
        long hours = TimeUnit.MILLISECONDS.toHours(duration) % 24;

        if (years > 0) {
            return years + " year" + (years > 1 ? "s" : "") + " ago";
        } else if (months > 0) {
            return months + " month" + (months > 1 ? "s" : "") + " ago";
        } else if (days > 0) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else if (hours > 0) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else {
            return "Just now";
        }
    }


    public static String formatCreationDate(long epochMillis) {
        // Create a Date object from the given time in milliseconds
        Date date = new Date(epochMillis);

        // Define the desired date format
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy");

        // Format the date into the desired string format
        return sdf.format(date);
    }

    public static Party getPartyFromName(String name){
        for(Party party: PartyAPI.getParties()){
            if(party.getName().equals(name)){
                return party;
            }
        }

        return null;
    }

    public static Essentials getEssentialsAPI(){
        return (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
    }

    public static String addCommaToNumber(int number){
        DecimalFormat df = new DecimalFormat("#,###");
        String formatted = df.format(number);
        return formatted;
    }

}
