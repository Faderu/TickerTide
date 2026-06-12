package com.example.tickertide.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tickertide.adapter.PortfolioAdapter;
import com.example.tickertide.databinding.FragmentPortfolioBinding;
import com.example.tickertide.local.DatabaseHelper;
import com.example.tickertide.model.PortfolioItem;
import com.example.tickertide.utils.AppExecutors;

import java.util.List;
import java.util.Locale;

public class PortfolioFragment extends Fragment implements PortfolioAdapter.OnPortfolioClickListener {

    private FragmentPortfolioBinding binding;
    private PortfolioAdapter adapter;
    private DatabaseHelper dbHelper;
    private AppExecutors executors;
    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPortfolioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        executors = AppExecutors.getInstance();
        prefs = requireContext().getSharedPreferences("TickerTidePrefs", Context.MODE_PRIVATE);

        // Inisialisasi saldo jika belum ada
        if (!prefs.contains("cash_balance")) {
            prefs.edit().putFloat("cash_balance", 10000.00f).apply();
        }

        setupRecyclerView();
        loadPortfolio();
    }

    private void setupRecyclerView() {
        adapter = new PortfolioAdapter(this);
        binding.recyclerViewPortfolio.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPortfolio.setAdapter(adapter);
    }

    private void loadPortfolio() {
        executors.diskIO().execute(() -> {
            List<PortfolioItem> items = dbHelper.getAllPortfolioItems();
            
            double totalHoldingsValue = 0;
            double totalCost = 0;
            
            for (PortfolioItem item : items) {
                totalHoldingsValue += item.getTotalValue();
                totalCost += item.getTotalCost();
            }
            
            final double finalTotalHoldingsValue = totalHoldingsValue;
            final double finalTotalCost = totalCost;

            executors.mainThread().execute(() -> {
                if (items.isEmpty()) {
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewPortfolio.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewPortfolio.setVisibility(View.VISIBLE);
                    adapter.submitList(items);
                }
                
                updatePortfolioSummary(finalTotalHoldingsValue, finalTotalCost);
            });
        });
    }

    private void updatePortfolioSummary(double totalHoldingsValue, double totalCost) {
        float cashBalance = prefs.getFloat("cash_balance", 10000.00f);
        double totalEquity = cashBalance + totalHoldingsValue;
        
        double totalPL = totalHoldingsValue - totalCost;
        double totalPLPercent = (totalCost == 0) ? 0 : (totalPL / totalCost) * 100;

        binding.tvTotalEquity.setText(String.format(Locale.US, "$%,.2f", totalEquity));
        binding.tvCashBalance.setText(String.format(Locale.US, "$%,.2f", cashBalance));
        
        String plText = String.format(Locale.US, "$%+,.2f (%+.2f%%)", totalPL, totalPLPercent);
        binding.tvTotalPl.setText(plText);
        
        int colorRes = totalPL >= 0 ? com.example.tickertide.R.color.positive_green : com.example.tickertide.R.color.negative_red;
        binding.tvTotalPl.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), colorRes));
    }

    @Override
    public void onPortfolioClick(PortfolioItem item) {
        // Navigate to detail
        try {
            PortfolioFragmentDirections.ActionPortfolioToDetail action = 
                    PortfolioFragmentDirections.actionPortfolioToDetail(item.getSymbol());
            Navigation.findNavController(requireView()).navigate(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
