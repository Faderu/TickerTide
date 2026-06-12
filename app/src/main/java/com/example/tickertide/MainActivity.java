package com.example.tickertide;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.tickertide.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity - Activity utama yang bertindak sebagai host untuk:
 * 1. NavHostFragment (Navigation Component)
 * 2. BottomNavigationView untuk navigasi antar fragment
 *
 * Semua perpindahan antar Fragment dihandle oleh NavController.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup NavController dari NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Hubungkan BottomNavigationView dengan NavController
            NavigationUI.setupWithNavController(binding.bottomNav, navController);

            // Sembunyikan BottomNav saat berada di halaman detail saham
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.stockDetailFragment) {
                    binding.bottomNav.setVisibility(android.view.View.GONE);
                } else {
                    binding.bottomNav.setVisibility(android.view.View.VISIBLE);
                }
            });
        }
    }

    /**
     * Handle tombol back agar NavController yang mengurus navigasi kembali
     */
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}