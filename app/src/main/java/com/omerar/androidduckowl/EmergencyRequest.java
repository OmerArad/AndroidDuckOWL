package com.omerar.androidduckowl;

import java.util.HashMap;
import java.util.Map;

public class EmergencyRequest {
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

    public EmergencyRequest(String name, String phone, String street, String occupants, String danger, String vacant, String firstaid, String water, String food, String message) {
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
        hashMap.put("name","test");
        hashMap.put("phone","11111");
        hashMap.put("street","11111");
        hashMap.put("occupants","1");
        hashMap.put("danger","0");
        hashMap.put("vacant","0");
        hashMap.put("firstaid","0");
        hashMap.put("water","0");
        hashMap.put("food","0");
        hashMap.put("message","test");

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
