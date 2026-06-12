package com.example.tickertide.ui;

import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.tickertide.R;
import com.example.tickertide.adapter.StockAdapter;
import com.example.tickertide.databinding.FragmentDashboardBinding;
import com.example.tickertide.local.DatabaseHelper;
import com.example.tickertide.model.Stock;
import com.example.tickertide.network.ApiClient;
import com.example.tickertide.network.ApiService;
import com.example.tickertide.utils.AppExecutors;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * DashboardFragment - Fragment utama yang menampilkan daftar saham.
 *
 * Alur kerja:
 * 1. Cek koneksi internet
 * 2. Jika ONLINE  → fetch data dari API (Retrofit async), simpan ke SQLite, tampilkan
 * 3. Jika OFFLINE → load data terakhir dari SQLite, tampilkan tombol Refresh
 *
 * Klik pada item saham → navigasi ke StockDetailFragment via NavDirections + Safe Args.
 */
public class DashboardFragment extends Fragment implements StockAdapter.OnStockClickListener {

    private static final String TAG = "DashboardFragment";

    // Daftar simbol saham default yang akan ditampilkan (30 emiten populer)
    private static final String DEFAULT_SYMBOLS = "AAPL,MSFT,GOOGL,AMZN,NVDA,META,TSLA,BRK.B,LLY,V,TSM,AVGO,JPM,WMT,UNH,MA,JNJ,PG,HD,ORCL,COST,MRK,BAC,ABBV,CRM,CVX,NFLX,AMD,PEP,KO";

    private FragmentDashboardBinding binding;
    private StockAdapter adapter;
    private DatabaseHelper dbHelper;
    private AppExecutors executors;
    
    // Simpan list original untuk keperluan filter pencarian
    private List<Stock> allStocksList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper  = DatabaseHelper.getInstance(requireContext());
        executors = AppExecutors.getInstance();

        setupRecyclerView();
        setupRefreshButton();
        setupSearchView();

        // Load data pertama kali
        loadStockData();
    }

    /**
     * Setup SearchView untuk memfilter list saham secara lokal
     */
    private void setupSearchView() {
        binding.searchBar.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterStocks(query);
                binding.searchBar.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterStocks(newText);
                return true;
            }
        });
    }

    /**
     * Memfilter saham berdasarkan query (simbol atau nama perusahaan)
     */
    private void filterStocks(String query) {
        if (allStocksList == null) return;
        
        if (query == null || query.isEmpty()) {
            adapter.submitList(allStocksList);
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<Stock> filteredList = new java.util.ArrayList<>();
        
        for (Stock stock : allStocksList) {
            boolean matchSymbol = stock.getSymbol() != null && stock.getSymbol().toLowerCase().contains(lowerQuery);
            boolean matchName = stock.getCompanyName() != null && stock.getCompanyName().toLowerCase().contains(lowerQuery);
            if (matchSymbol || matchName) {
                filteredList.add(stock);
            }
        }
        
        adapter.submitList(filteredList);
    }

    /**
     * Setup RecyclerView dengan adapter dan layout manager.
     */
    private void setupRecyclerView() {
        adapter = new StockAdapter(this);
        binding.recyclerViewStocks.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );
        binding.recyclerViewStocks.setAdapter(adapter);
    }

    /**
     * Setup tombol Refresh yang muncul saat offline.
     */
    private void setupRefreshButton() {
        binding.btnRefresh.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                loadStockData();
            } else {
                Toast.makeText(requireContext(),
                        getString(R.string.error_no_internet),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Tombol refresh di swipe-to-refresh layout
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadStockData);
    }

    /**
     * Logika utama loading data.
     * Cek koneksi → pilih sumber data (API atau SQLite).
     */
    private void loadStockData() {
        if (isNetworkAvailable()) {
            // ONLINE: fetch dari API
            fetchFromApi();
        } else {
            // OFFLINE: load dari SQLite dan tampilkan tombol refresh
            loadFromLocalDb();
            showOfflineState(true);
        }
    }

    /**
     * Fetch data saham dari API menggunakan Retrofit secara Asynchronous.
     */
    private void fetchFromApi() {
        showLoading(true);
        showOfflineState(false);

        ApiService apiService = ApiClient.getApiService();

        Call<List<Stock>> call = apiService.getStockQuotes(
                DEFAULT_SYMBOLS,
                ApiService.RAPIDAPI_HOST,
                ApiService.RAPIDAPI_KEY
        );

        call.enqueue(new Callback<List<Stock>>() {
            @Override
            public void onResponse(@NonNull Call<List<Stock>> call,
                                   @NonNull Response<List<Stock>> response) {
                showLoading(false);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    allStocksList = response.body();
                    // Update UI dengan data baru (kirim query search saat ini jika ada)
                    String currentQuery = binding.searchBar.getQuery().toString();
                    filterStocks(currentQuery);
                    
                    // Simpan ke SQLite untuk fallback offline
                    saveToLocalDb(allStocksList);
                    Log.d(TAG, "API sukses, " + allStocksList.size() + " saham loaded.");
                } else {
                    Log.e(TAG, "API response error: " + response.code());
                    // Fallback ke data lokal jika response error
                    loadFromLocalDb();
                    Toast.makeText(requireContext(),
                            getString(R.string.error_api_response),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Stock>> call, @NonNull Throwable t) {
                // JARINGAN PUTUS → tampilkan data lokal + tombol refresh
                showLoading(false);
                binding.swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "API gagal (offline): " + t.getMessage());

                loadFromLocalDb();
                showOfflineState(true);
                Toast.makeText(requireContext(),
                        getString(R.string.error_offline_mode),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Load data dari SQLite menggunakan AppExecutors di background thread.
     */
    private void loadFromLocalDb() {
        executors.diskIO().execute(() -> {
            // Operasi SQLite di background thread
            List<Stock> localStocks = dbHelper.getAllCachedStocks();

            // Update UI di main thread
            executors.mainThread().execute(() -> {
                if (localStocks.isEmpty()) {
                    showEmptyState(true);
                } else {
                    showEmptyState(false);
                    allStocksList = localStocks;
                    String currentQuery = binding.searchBar.getQuery().toString();
                    filterStocks(currentQuery);
                    Log.d(TAG, "Loaded " + localStocks.size() + " saham dari lokal DB.");
                }
            });
        });
    }

    /**
     * Simpan list saham ke SQLite menggunakan AppExecutors di background thread.
     */
    private void saveToLocalDb(List<Stock> stocks) {
        executors.diskIO().execute(() -> {
            dbHelper.insertOrUpdateStocks(stocks);
            Log.d(TAG, stocks.size() + " saham disimpan ke lokal DB.");
        });
    }

    // ================================================================
    // Navigation - StockAdapter.OnStockClickListener
    // ================================================================

    /**
     * Dipanggil saat user mengklik item saham di RecyclerView.
     * Navigasi ke StockDetailFragment membawa parameter symbol via Safe Args.
     */
    @Override
    public void onStockClick(Stock stock) {
        // Gunakan NavDirections dengan Safe Args untuk membawa symbol saham
        DashboardFragmentDirections.ActionDashboardToDetail action =
                DashboardFragmentDirections.actionDashboardToDetail(stock.getSymbol());
        Navigation.findNavController(requireView()).navigate(action);
    }

    // ================================================================
    // UI State Helpers
    // ================================================================

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerViewStocks.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showOfflineState(boolean show) {
        binding.layoutOfflineBanner.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRefresh.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showEmptyState(boolean show) {
        binding.layoutEmptyState.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.recyclerViewStocks.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ================================================================
    // Network Check
    // ================================================================

    /**
     * Cek apakah perangkat terhubung ke internet.
     * Menggunakan NetworkCapabilities (API 23+) untuk pengecekan yang akurat.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        android.net.Network network = cm.getActiveNetwork();
        if (network == null) return false;

        NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
