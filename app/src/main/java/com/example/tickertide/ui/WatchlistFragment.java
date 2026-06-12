package com.example.tickertide.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tickertide.adapter.StockAdapter;
import com.example.tickertide.databinding.FragmentWatchlistBinding;
import com.example.tickertide.local.DatabaseHelper;
import com.example.tickertide.model.Stock;
import com.example.tickertide.utils.AppExecutors;

import java.util.List;

public class WatchlistFragment extends Fragment implements StockAdapter.OnStockClickListener {

    private FragmentWatchlistBinding binding;
    private StockAdapter adapter;
    private DatabaseHelper dbHelper;
    private AppExecutors executors;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWatchlistBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        executors = AppExecutors.getInstance();

        setupRecyclerView();
        loadWatchlist();
    }

    private void setupRecyclerView() {
        adapter = new StockAdapter(this);
        binding.recyclerViewWatchlist.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewWatchlist.setAdapter(adapter);

        // Tambahkan Swipe-to-Delete
        androidx.recyclerview.widget.ItemTouchHelper itemTouchHelper = new androidx.recyclerview.widget.ItemTouchHelper(
                new androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT | androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull androidx.recyclerview.widget.RecyclerView recyclerView, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, @NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull androidx.recyclerview.widget.RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Stock stock = adapter.getCurrentList().get(position);
                        
                        // Hapus dari watchlist
                        stock.setWatchlist(false);
                        executors.diskIO().execute(() -> {
                            dbHelper.insertOrUpdateStock(stock);
                            executors.mainThread().execute(() -> {
                                loadWatchlist(); // Refresh UI
                                android.widget.Toast.makeText(requireContext(), stock.getSymbol() + " dihapus dari Watchlist", android.widget.Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                });
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewWatchlist);
    }

    private void loadWatchlist() {
        executors.diskIO().execute(() -> {
            List<Stock> localStocks = dbHelper.getStarredWatchlistStocks();
            executors.mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                
                if (localStocks.isEmpty()) {
                    binding.layoutEmptyState.setVisibility(View.VISIBLE);
                    binding.recyclerViewWatchlist.setVisibility(View.GONE);
                } else {
                    binding.layoutEmptyState.setVisibility(View.GONE);
                    binding.recyclerViewWatchlist.setVisibility(View.VISIBLE);
                    adapter.submitList(localStocks);
                }
            });
        });
    }

    @Override
    public void onStockClick(Stock stock) {
        // Navigate to details (Assuming we have an action_nav_watchlist_to_detail or just use global action if it exists)
        // Let's use a dynamic safe args approach or just define the action in nav_graph.
        // Actually, we can reuse the same detail fragment. We will need to add an action in nav_graph.xml
        try {
            WatchlistFragmentDirections.ActionWatchlistToDetail action = 
                    WatchlistFragmentDirections.actionWatchlistToDetail(stock.getSymbol());
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
