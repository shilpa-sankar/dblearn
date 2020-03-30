package com.example.dblearn2;

public class data {
    Double lati,longi;
    data(){

    }

    public data(Double lati, Double longi, String dname) {
        this.lati = lati;
        this.longi = longi;
    }


    public Double getLati() {
        return lati;
    }

    public void setLati(Double lati) {
        this.lati = lati;
    }

    public Double getLongi() {
        return longi;
    }

    public void setLongi(Double longi) {
        this.longi = longi;
    }
}
