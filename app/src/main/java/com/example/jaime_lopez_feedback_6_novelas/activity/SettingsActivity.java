package com.example.jaime_lopez_feedback_6_novelas.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.jaime_lopez_feedback_6_novelas.R;


public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "UserPreferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private Switch switchDarkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cargar las preferencias de tema antes de setear el contenido
        loadThemePreference();

        setContentView(R.layout.settings_activity);

        // Inicializamos el switch del modo oscuro
        switchDarkMode = findViewById(R.id.switch_dark_mode);

        // Cargamos las preferencias guardadas
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);

        // Establecemos el estado del switch según la preferencia
        switchDarkMode.setChecked(isDarkMode);

        // Listener para cuando el usuario cambie el estado del switch
        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Guardamos la preferencia en SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(KEY_DARK_MODE, isChecked);
                editor.apply();

                // Cambiar el tema de la aplicación
                setAppTheme(isChecked);

                // Mostramos un mensaje para informar al usuario
                String message = isChecked ? "Modo oscuro activado" : "Modo claro activado";
                Toast.makeText(SettingsActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para cargar la preferencia de tema y aplicarla
    private void loadThemePreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        setAppTheme(isDarkMode);
    }

    // Método para aplicar el tema seleccionado
    private void setAppTheme(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
