package com.android.parkplatz;

public class uploadinfo {

    public String title;
    public double lat;
    public double lang;
    public String address;
    public String image;
    public String phone;
    public String time;
    public String type;

    public uploadinfo() {
    }

    public uploadinfo(String title, double lat, double lang, String address, String image, String phone, String time,String type) {
        this.title = title;
        this.lat = lat;
        this.lang = lang;
        this.address = address;
        this.image = image;
        this.phone = phone;
        this.time = time;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLang() {
        return lang;
    }

    public void setLang(double lang) {
        this.lang = lang;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
