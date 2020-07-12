package main.java.world.room;

import java.util.*;

public enum Domain {
    surface,
    air,
    deepWater,
    surfaceWater,
    subterranean,
    hyperspace,
    aether,
    grey; //empty space. Devoid of all creation.

    public static List<Domain> decodeDomains(String encodedDomains){
        if(encodedDomains == null || encodedDomains.isEmpty())
            return new ArrayList<>();

        Set<Domain> domainSet = new LinkedHashSet<>();
        String[] rawDomains = encodedDomains.split(";");
        for(String rawDomain: rawDomains){
            try{
                if(rawDomain != null) domainSet.add(Domain.valueOf(rawDomain));
            }catch (IllegalArgumentException e){
                System.out.printf("Tried to parse invalid domain:%s\n", rawDomain);
            }
        }
        return new ArrayList<>(domainSet);
    }

    public static String encodeDomains(List<Domain> domains){
        if(domains.isEmpty())
            return "";
        StringBuilder encoded = new StringBuilder();
        Set<Domain> domainSet = new LinkedHashSet<>(domains);
        boolean first = true;
        for(Domain domain: domainSet){
            if(first) first = false;
            else encoded.append(";");
            encoded.append(domain.name());
        }
        return encoded.toString();
    }

    public static String getTransitionDescription(Domain sourceDomain, Domain destinationDomain){
        if(sourceDomain == surface && destinationDomain == air)
            return "leaps into the air";
        if(sourceDomain == air && destinationDomain == surface)
            return "lands on the surface";
        if(sourceDomain == air && destinationDomain == surfaceWater)
            return "lands with a splash";
        return "goes from the " + sourceDomain.name() + " to the " + destinationDomain.name();
    }

    public String getTravelVerb(){
        return "travels";
    }
}