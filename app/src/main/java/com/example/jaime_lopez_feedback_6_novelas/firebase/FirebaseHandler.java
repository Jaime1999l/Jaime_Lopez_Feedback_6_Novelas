package com.example.jaime_lopez_feedback_6_novelas.firebase;

import android.util.Log;

import com.example.jaime_lopez_feedback_6_novelas.model.Credenciales;
import com.example.jaime_lopez_feedback_6_novelas.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.*;

public class FirebaseHandler {
    private FirebaseFirestore db;

    public FirebaseHandler() {
        this.db = FirebaseFirestore.getInstance();
    }

    // CRUD --> Usuarios
    public void guardarUsuario(Usuario usuario, OnCompleteListener<Void> onCompleteListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .document(usuario.getId())
                .set(usuario)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHandler", "Error al guardar usuario", e);
                });
    }

    public void obtenerUsuario(String id, OnCompleteListener<DocumentSnapshot> listener) {
        db.collection("usuarios").document(id).get().addOnCompleteListener(listener);
    }

    public void eliminarUsuario(String id) {
        db.collection("usuarios").document(id).delete()
                .addOnSuccessListener(aVoid -> System.out.println("Usuario eliminado con éxito"))
                .addOnFailureListener(e -> System.err.println("Error al eliminar usuario: " + e.getMessage()));
    }

    public void guardarCredenciales(Credenciales credenciales, OnCompleteListener<Void> onCompleteListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("credenciales")
                .document(String.valueOf(credenciales.getId())) // Usar el ID como clave del documento
                .set(credenciales)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHandler", "Error al guardar credenciales", e);
                });
    }

    public void obtenerUsuarioPorCorreo(String email, OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("usuarios")
                .whereEqualTo("correo", email)
                .get()
                .addOnCompleteListener(listener);
    }

    public void obtenerCredenciales(String idCredenciales, OnCompleteListener<DocumentSnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("credenciales")
                .document(idCredenciales)
                .get()
                .addOnCompleteListener(listener);
    }
}

