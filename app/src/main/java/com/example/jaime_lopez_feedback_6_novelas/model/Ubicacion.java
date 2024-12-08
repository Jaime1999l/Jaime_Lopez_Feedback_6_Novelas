package com.example.jaime_lopez_feedback_6_novelas.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ubicaciones")
public class Ubicacion {

    @PrimaryKey(autoGenerate = true)
    private int id; // ID generado automáticamente por Room
    private double latitud;
    private double longitud;
    private String direccion;

    public Ubicacion(double latitud, double longitud, String direccion) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
    }

    // Constructor vacío (obligatorio para Room)
    public Ubicacion() {
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
