package com.example.tickertide.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tickertide.adapter.AlertsAdapter;
import com.example.tickertide.databinding.FragmentAlertsBinding;
import com.example.tickertide.local.DatabaseHelper;
import com.example.tickertide.model.PriceAlert;
import com.example.tickertide.utils.AppExecutors;

import java.util.List;

public class AlertsFragment extends Fragment {

    private FragmentAlertsBinding binding;
    private AlertsAdapter adapter;
    private DatabaseHelper dbHelper;
    private AppExecutors executors;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlertsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        executors = AppExecutors.getInstance();

        setupRecyclerView();
        loadAlerts();
    }

    private void setupRecyclerView() {
        adapter = new AlertsAdapter();
        binding.recyclerViewAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewAlerts.setAdapter(adapter);

        androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback swipeCallback = new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                PriceAlert alert = adapter.getItem(position);
                
                executors.diskIO().execute(() -> {
                    dbHelper.deleteAlert(alert.getId());
                    loadAlerts(); // Reload list after deletion
                    
                    executors.mainThread().execute(() -> {
                        if (isAdded()) {
                            android.widget.Toast.makeText(requireContext(), "Alert dihapus", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        };
        new androidx.recyclerview.widget.ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.recyclerViewAlerts);
    }

    private void loadAlerts() {
        executors.diskIO().execute(() -> {
            List<PriceAlert> alerts = dbHelper.getAllActiveAlerts();

            executors.mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                
                if (alerts.isEmpty()) {
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewAlerts.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewAlerts.setVisibility(View.VISIBLE);
                    adapter.submitList(alerts);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
