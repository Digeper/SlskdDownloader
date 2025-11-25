package org.muzika.slskddownload.services;


import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SlskdSearchIdController {
    HashMap<String, Boolean> searchIds;

    HashMap<String, Integer> triedId;

    public SlskdSearchIdController(){
        searchIds = new HashMap<>(Map.of("2432e9ef-7f8e-42cc-99d5-ae9d15be93ab", false, "7272a62a-8457-4f14-9300-be3ec5de4a9b", false, "7795f422-6ec7-42f9-bf99-ce402b6c3ebd",
                false, "f9c7ac8a-52c8-4cb1-ae37-33472c2ca204", false, "aa4ba3e1-194f-400b-b023-40c5091f7f5a", false,
                "8997f23c-5475-48ef-9516-32bc1f8c3b0b", false, "bb4f44c8-d2d3-436e-8861-8b0c66313e5d", false,
                "5823b99c-2c5d-42b9-955d-7755502f0e46", false));
        triedId = new HashMap<>();
    }

    public String ReserveId(){
        for (String key : searchIds.keySet()) {
            if (!searchIds.get(key)) {
                searchIds.replace(key, true);
                return key;
            }
        }
        return null;
    }

    public void UnReserveId(String key){
        searchIds.replace(key, false);
    }



}
