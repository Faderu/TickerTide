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
    }

    private void loadAlerts() {
        executors.diskIO().execute(() -> {
            List<PriceAlert> alerts = dbHelper.getAllActiveAlerts();

            executors.mainThread().execute(() -> {
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
