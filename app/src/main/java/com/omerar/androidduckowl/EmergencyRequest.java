package com.omerar.androidduckowl;

import java.util.HashMap;
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
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("uuid",this.uuid);
        hashMap.put("name",this.name);
        hashMap.put("phone",this.phone);
        hashMap.put("street",this.street);
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
