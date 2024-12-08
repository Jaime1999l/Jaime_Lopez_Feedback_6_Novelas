package com.example.jaime_lopez_feedback_6_novelas.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "usuarios")
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    private String id;
    private String nombre;
    private String correo;
    private String rol; // "user" / "admin"
    private Ubicacion ubicacionActual;
    private String idCredenciales;

    public Usuario(String id, String nombre, String correo, String rol, Ubicacion ubicacionActual, String idCredenciales) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.ubicacionActual = ubicacionActual;
        this.idCredenciales = idCredenciales;
    }

    public Usuario() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Ubicacion getUbicacionActual() {
        return ubicacionActual;
    }

    public void setUbicacionActual(Ubicacion ubicacionActual) {
        this.ubicacionActual = ubicacionActual;
    }

    public String getIdCredenciales() {
        return idCredenciales;
    }

    public void setIdCredenciales(String idCredenciales) {
        this.idCredenciales = idCredenciales;
    }

    public boolean esAdministrador() {
        return "admin".equals(rol);
    }
}

