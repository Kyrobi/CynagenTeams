package me.kyrobi.cynagenteams;

import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.datatypes.party.Party;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
@Setter
public class ListingData {


    private String partyName = "";
    private String description = "";
    private String motd = "";
    private long creationDate = 0;
    private boolean isPartyStillValid; // Stores if the party still exists.

    public ListingData(String partyName, long creationDate, String description, String motd){

        this.partyName = partyName;
        this.creationDate = creationDate;
        this.description = description;
        this.motd = motd;

        this.isPartyStillValid = isPartyStillValid(partyName);
    }

    public ListingData(String name){

        this.partyName = name;
        this.creationDate = System.currentTimeMillis();

        this.isPartyStillValid = isPartyStillValid(partyName);
    }

    UUID getPartyLeader(){
        for(Party party: PartyAPI.getParties()){
            if(party.getName().equals(this.partyName)){
                party.getLeader().getUniqueId();
            }
        }

        return null;
    }

    Party getParty(){
        for(Party party: PartyAPI.getParties()){
            if(party.getName().equals(this.partyName)){
                return party;
            }
        }

        return null;
    }

    private boolean isPartyStillValid(String name){
        for(Party party: PartyAPI.getParties()){
            if(party.getName().equals(name)){
                return true;
            }
        }
        return false;
    }

}
