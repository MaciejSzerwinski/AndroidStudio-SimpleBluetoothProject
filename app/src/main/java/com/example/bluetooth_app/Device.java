package com.example.bluetooth_app;

public class Device {
    String name;
    String address_MAC;

    public Device(String name, String address_MAC) {
        this.name=name;
        this.address_MAC=address_MAC;
    }

    public String getName() {
        return name;
    }

    public String getAddressMAC() {
        return address_MAC;
    }
}
