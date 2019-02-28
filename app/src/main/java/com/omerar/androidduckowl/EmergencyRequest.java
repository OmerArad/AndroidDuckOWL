package com.omerar.androidduckowl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EmergencyRequest {
    private String uuid = "";
    private String name = "";
    private String phone = "";
    private String street = "";
    private String occupants = "";
    private String danger = "";
    private String vacant = "";
    private String firstaid = "";
    private String water = "";
    private String food = "";
    private String message = "";

    public EmergencyRequest(String uuid, String name, String phone, String street, String occupants, String danger, String vacant, String firstaid, String water, String food, String message) {
        this.uuid = uuid;
        this.name = name;
        this.phone = phone;
        this.street = street;
        this.occupants = occupants;
        this.danger = danger;
        this.vacant = vacant;
        this.firstaid = firstaid;
        this.water = water;
        this.food = food;
        this.message = message;
    }

    public HashMap getMap() {
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        // The order here is important! LinkedHashMap will keep the order :)
        /*
            offline.fname      = webServer.arg(1);
            offline.street     = webServer.arg(2);
            offline.phone      = webServer.arg(3);
            offline.occupants  = webServer.arg(4);
            offline.danger     = webServer.arg(5);
            offline.vacant     = webServer.arg(6);
            offline.firstaid   = webServer.arg(7);
            offline.water      = webServer.arg(8);
            offline.food       = webServer.arg(9);
            offline.msg        = webServer.arg(10);
         */
        hashMap.put("uuid",this.uuid);  // this is being ignored by the device, it has to be unique! id = webserver.arg(0)
        hashMap.put("name",this.name);
        hashMap.put("street",this.street);
        hashMap.put("phone",this.phone);
        hashMap.put("occupants",this.occupants);
        hashMap.put("danger",this.danger);
        hashMap.put("vacant",this.vacant);
        hashMap.put("firstaid",this.firstaid);
        hashMap.put("water",this.water);
        hashMap.put("food",this.food);
        hashMap.put("message",this.message);

        return hashMap;
    }

    public EmergencyRequest(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getOccupants() {
        return occupants;
    }

    public void setOccupants(String occupants) {
        this.occupants = occupants;
    }

    public String getDanger() {
        return danger;
    }

    public void setDanger(String danger) {
        this.danger = danger;
    }

    public String getVacant() {
        return vacant;
    }

    public void setVacant(String vacant) {
        this.vacant = vacant;
    }

    public String getFirstaid() {
        return firstaid;
    }

    public void setFirstaid(String firstaid) {
        this.firstaid = firstaid;
    }

    public String getWater() {
        return water;
    }

    public void setWater(String water) {
        this.water = water;
    }

    public String getFood() {
        return food;
    }

    public void setFood(String food) {
        this.food = food;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
