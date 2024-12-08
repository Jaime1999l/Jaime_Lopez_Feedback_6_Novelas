package com.example.jaime_lopez_feedback_6_novelas.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "credenciales")
public class Credenciales {

    @PrimaryKey(autoGenerate = true)
    private String id;
    private String contrasena;

    public Credenciales(String id, String contrasena) {
        this.id = id;
        this.contrasena = contrasena;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}
