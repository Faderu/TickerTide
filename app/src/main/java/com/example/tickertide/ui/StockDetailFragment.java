package com.example.tickertide.ui;

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

import com.example.tickertide.R;
import com.example.tickertide.databinding.FragmentStockDetailBinding;
import com.example.tickertide.local.DatabaseHelper;
import com.example.tickertide.model.Stock;
import com.example.tickertide.network.ApiClient;
import com.example.tickertide.network.ApiService;
import com.example.tickertide.utils.AppExecutors;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * StockDetailFragment - Fragment yang menampilkan detail lengkap satu saham.
 *
 * Menerima parameter "stockSymbol" dari DashboardFragment via Safe Args (NavDirections).
 * Alur:
 * 1. Ambil symbol dari argument (Safe Args)
 * 2. Cek koneksi internet
 * 3. ONLINE  → fetch profil lengkap dari API
 * 4. OFFLINE → load dari SQLite lokal berdasarkan symbol
 */
public class StockDetailFragment extends Fragment {

    private static final String TAG = "StockDetailFragment";

    private FragmentStockDetailBinding binding;
    private DatabaseHelper dbHelper;
    private AppExecutors executors;

    // Symbol yang diterima dari DashboardFragment via Safe Args
    private String stockSymbol;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ambil argument dari Safe Args
        if (getArguments() != null) {
            StockDetailFragmentArgs args = StockDetailFragmentArgs.fromBundle(getArguments());
            stockSymbol = args.getStockSymbol();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentStockDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper  = DatabaseHelper.getInstance(requireContext());
        executors = AppExecutors.getInstance();

        setupToolbar();

        if (stockSymbol != null && !stockSymbol.isEmpty()) {
            loadStockDetail(stockSymbol);
        } else {
            Toast.makeText(requireContext(), "Symbol saham tidak valid", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Setup toolbar dengan tombol back.
     */
    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).navigateUp()
        );

        if (stockSymbol != null) {
            binding.toolbar.setTitle(stockSymbol);
        }
    }

    /**
     * Load detail saham: cek koneksi → pilih sumber.
     */
    private void loadStockDetail(String symbol) {
        if (isNetworkAvailable()) {
            fetchDetailFromApi(symbol);
        } else {
            loadDetailFromLocalDb(symbol);
        }
    }

    /**
     * Fetch detail saham dari API (Retrofit async).
     */
    private void fetchDetailFromApi(String symbol) {
        showLoading(true);

        ApiService apiService = ApiClient.getApiService();
        Call<List<Stock>> call = apiService.getStockProfile(
                symbol,
                ApiService.RAPIDAPI_HOST,
                ApiService.RAPIDAPI_KEY
        );

        call.enqueue(new Callback<List<Stock>>() {
            @Override
            public void onResponse(@NonNull Call<List<Stock>> call,
                                   @NonNull Response<List<Stock>> response) {
                if (!isAdded() || binding == null) return;
                showLoading(false);

                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {
                    Stock stock = response.body().get(0);
                    
                    // Fetch status watchlist & timestamp dari lokal
                    executors.diskIO().execute(() -> {
                        Stock local = dbHelper.getStockBySymbol(stock.getSymbol());
                        if (local != null) {
                            stock.setWatchlist(local.isWatchlist());
                        }
                        
                        executors.mainThread().execute(() -> {
                            if (!isAdded() || binding == null) return;
                            populateUI(stock);
                            Log.d(TAG, "Detail saham " + symbol + " loaded dari API.");
                        });
                    });
                } else {
                    Log.e(TAG, "API response kosong untuk " + symbol);
                    loadDetailFromLocalDb(symbol);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Stock>> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                showLoading(false);
                Log.e(TAG, "Gagal fetch detail: " + t.getMessage());
                // Fallback ke data lokal
                loadDetailFromLocalDb(symbol);
                Toast.makeText(requireContext(),
                        getString(R.string.error_offline_mode),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Load detail saham dari SQLite menggunakan AppExecutors di background thread.
     */
    private void loadDetailFromLocalDb(String symbol) {
        executors.diskIO().execute(() -> {
            Stock stock = dbHelper.getStockBySymbol(symbol);

            executors.mainThread().execute(() -> {
                if (!isAdded() || binding == null) return;
                
                showLoading(false);
                if (stock != null) {
                    populateUI(stock);
                    Log.d(TAG, "Detail saham " + symbol + " loaded dari lokal DB.");
                } else {
                    binding.layoutError.setVisibility(View.VISIBLE);
                    binding.scrollContent.setVisibility(View.GONE);
                    Log.w(TAG, "Saham " + symbol + " tidak ditemukan di lokal DB.");
                }
            });
        });
    }

    /**
     * Mengisi semua view dengan data saham yang diterima.
     */
    private void populateUI(Stock stock) {
        binding.scrollContent.setVisibility(View.VISIBLE);

        binding.tvDetailSymbol.setText(stock.getSymbol());
        binding.tvDetailCompanyName.setText(stock.getCompanyName() != null
                ? stock.getCompanyName() : "-");

        // Load logo via Glide
        if (stock.getSymbol() != null && !stock.getSymbol().isEmpty()) {
            String logoUrl = "https://financialmodelingprep.com/image-stock/" + stock.getSymbol() + ".png";
            com.bumptech.glide.Glide.with(this)
                    .load(logoUrl)
                    .into(binding.ivDetailLogo);
        }

        binding.tvDetailPrice.setText(
                String.format(Locale.US, "$%.2f", stock.getCurrentPrice())
        );

        // Change percent dengan warna
        double change = stock.getChangePercent();
        String changeText = String.format(Locale.US, "%+.2f%%", change);
        binding.tvDetailChange.setText(changeText);

        int colorRes = stock.isPositiveChange()
                ? R.color.positive_green
                : R.color.negative_red;
        binding.tvDetailChange.setTextColor(
                requireContext().getColor(colorRes)
        );

        // Detail statistik
        binding.tvDetailDayHigh.setText(
                String.format(Locale.US, "$%.2f", stock.getDayHigh())
        );
        binding.tvDetailDayLow.setText(
                String.format(Locale.US, "$%.2f", stock.getDayLow())
        );
        binding.tvDetailVolume.setText(formatVolume(stock.getVolume()));
        binding.tvDetailMarketCap.setText(formatMarketCap(stock.getMarketCap()));

        // Setup Bookmark Toggle
        updateBookmarkIcon(stock.isWatchlist());
        binding.ivBookmark.setOnClickListener(v -> {
            boolean newState = !stock.isWatchlist();
            stock.setWatchlist(newState);
            updateBookmarkIcon(newState);
            
            // Simpan ke database via background thread
            AppExecutors.getInstance().diskIO().execute(() -> {
                DatabaseHelper.getInstance(requireContext()).updateWatchlistStatus(stock.getSymbol(), newState);
                AppExecutors.getInstance().mainThread().execute(() -> {
                    if (!isAdded()) return;
                    String msg = newState ? "Ditambahkan ke Watchlist" : "Dihapus dari Watchlist";
                    android.widget.Toast.makeText(requireContext(), msg, android.widget.Toast.LENGTH_SHORT).show();
                });
            });
        });

        // Setup Trade Buttons
        binding.btnBuy.setOnClickListener(v -> showTradeDialog(stock, true));
        binding.btnSell.setOnClickListener(v -> showTradeDialog(stock, false));

        // Setup Alert Button
        binding.ivAlert.setOnClickListener(v -> showAlertDialog(stock));
    }

    private void showAlertDialog(Stock stock) {
        android.content.Context context = requireContext();
        android.widget.EditText input = new android.widget.EditText(context);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Target Harga ($)");
        
        new androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Set Price Alert untuk " + stock.getSymbol())
            .setMessage("Harga saat ini: $" + stock.getCurrentPrice() + "\nMasukkan target harga:")
            .setView(input)
            .setPositiveButton("Simpan", (dialog, which) -> {
                String val = input.getText().toString();
                if (!val.isEmpty()) {
                    try {
                        double target = Double.parseDouble(val);
                        boolean isAbove = target > stock.getCurrentPrice();
                        
                        com.example.tickertide.model.PriceAlert alert = new com.example.tickertide.model.PriceAlert(
                            stock.getSymbol(), target, isAbove, true
                        );
                        
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            DatabaseHelper.getInstance(requireContext()).insertAlert(alert);
                            AppExecutors.getInstance().mainThread().execute(() -> 
                                android.widget.Toast.makeText(context, "Alert berhasil disimpan!", android.widget.Toast.LENGTH_SHORT).show()
                            );
                        });
                    } catch (NumberFormatException e) {
                        android.widget.Toast.makeText(context, "Input tidak valid", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void showTradeDialog(Stock stock, boolean isBuy) {
        android.content.Context context = requireContext();
        android.widget.EditText input = new android.widget.EditText(context);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Jumlah lembar saham");
        
        String title = isBuy ? "Beli " + stock.getSymbol() : "Jual " + stock.getSymbol();
        
        new androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage("Harga saat ini: $" + stock.getCurrentPrice() + "\nMasukkan jumlah lembar:")
            .setView(input)
            .setPositiveButton(isBuy ? "Beli" : "Jual", (dialog, which) -> {
                String val = input.getText().toString();
                if (!val.isEmpty()) {
                    try {
                        double shares = Double.parseDouble(val);
                        executeTrade(stock, shares, isBuy);
                    } catch (NumberFormatException e) {
                        android.widget.Toast.makeText(context, "Input tidak valid", android.widget.Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }

    private void executeTrade(Stock stock, double shares, boolean isBuy) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("TickerTidePrefs", android.content.Context.MODE_PRIVATE);
            float currentBalance = prefs.getFloat("cash_balance", 10000.00f);
            double totalCost = shares * stock.getCurrentPrice();
            
            com.example.tickertide.model.PortfolioItem portfolio = db.getPortfolioItemBySymbol(stock.getSymbol());
            if (portfolio == null) {
                portfolio = new com.example.tickertide.model.PortfolioItem(stock.getSymbol(), 0, 0);
            }
            
            if (isBuy) {
                if (currentBalance >= totalCost) {
                    // Update Balance
                    currentBalance -= totalCost;
                    prefs.edit().putFloat("cash_balance", currentBalance).apply();
                    
                    // Update Portfolio (Weighted Average Cost)
                    double newTotalCost = (portfolio.getShares() * portfolio.getAvgBuyPrice()) + totalCost;
                    portfolio.setShares(portfolio.getShares() + shares);
                    portfolio.setAvgBuyPrice(newTotalCost / portfolio.getShares());
                    
                    db.insertOrUpdatePortfolioItem(portfolio);
                    
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        if (isAdded()) {
                            android.widget.Toast.makeText(requireContext(), "Pembelian " + shares + " lot " + stock.getSymbol() + " berhasil!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        if (isAdded()) {
                            android.widget.Toast.makeText(requireContext(), "Gagal membeli saham!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else { // SELL
                if (portfolio.getShares() >= shares) {
                    // Update Balance
                    currentBalance += totalCost;
                    prefs.edit().putFloat("cash_balance", currentBalance).apply();
                    
                    // Update Portfolio
                    portfolio.setShares(portfolio.getShares() - shares);
                    if (portfolio.getShares() <= 0) {
                        db.deletePortfolioItem(stock.getSymbol());
                    } else {
                        db.insertOrUpdatePortfolioItem(portfolio);
                    }
                    
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        if (isAdded()) {
                            android.widget.Toast.makeText(requireContext(), "Penjualan " + shares + " lot " + stock.getSymbol() + " berhasil!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    AppExecutors.getInstance().mainThread().execute(() -> {
                        if (isAdded()) {
                            android.widget.Toast.makeText(requireContext(), "Lembar saham tidak cukup!", android.widget.Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void updateBookmarkIcon(boolean isWatchlist) {
        if (isWatchlist) {
            binding.ivBookmark.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            binding.ivBookmark.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    // ================================================================
    // Formatting Helpers
    // ================================================================

    private String formatVolume(double volume) {
        if (volume >= 1_000_000_000L) {
            return String.format(Locale.US, "%.2fB", volume / 1_000_000_000.0);
        } else if (volume >= 1_000_000L) {
            return String.format(Locale.US, "%.2fM", volume / 1_000_000.0);
        } else if (volume >= 1_000L) {
            return String.format(Locale.US, "%.2fK", volume / 1_000.0);
        }
        return String.valueOf((long) volume);
    }

    private String formatMarketCap(double marketCap) {
        if (marketCap >= 1_000_000_000_000L) {
            return String.format(Locale.US, "$%.2fT", marketCap / 1_000_000_000_000.0);
        } else if (marketCap >= 1_000_000_000L) {
            return String.format(Locale.US, "$%.2fB", marketCap / 1_000_000_000.0);
        } else if (marketCap >= 1_000_000L) {
            return String.format(Locale.US, "$%.2fM", marketCap / 1_000_000.0);
        }
        return "$" + (long) marketCap;
    }

    // ================================================================
    // UI State & Network Check
    // ================================================================

    private void showLoading(boolean show) {
        binding.progressBarDetail.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.scrollContent.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager)
                requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;

        android.net.Network network = cm.getActiveNetwork();
        if (network == null) return false;

        android.net.NetworkCapabilities cap = cm.getNetworkCapabilities(network);
        return cap != null && (
                cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                || cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR)
                || cap.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
