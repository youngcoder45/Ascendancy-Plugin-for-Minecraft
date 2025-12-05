package io.github.hyscript7.ascendancy.data.factions.simple;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class Faction {
    private String name;
    private ArrayList<FactionMember> members;

    public Faction(String name) {
        this.name = name;
        this.members = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder membersString = new StringBuilder();
        for (FactionMember member : members) {
            membersString.append(member.toString()).append(", ");
        }
        membersString.delete(membersString.length() - 2, membersString.length());

        return name + " (" +membersString.toString() + ")";
    }
}
