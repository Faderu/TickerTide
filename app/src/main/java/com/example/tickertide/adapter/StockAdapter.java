package com.example.tickertide.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tickertide.R;
import com.example.tickertide.model.Stock;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

/**
 * StockAdapter - RecyclerView Adapter untuk menampilkan list saham di DashboardFragment.
 *
 * Menggunakan ListAdapter dengan DiffUtil untuk update list yang efisien
 * (hanya me-render item yang berubah, bukan seluruh list).
 *
 * Klik item akan memanggil OnStockClickListener yang diimplementasikan di Fragment.
 * Fragment kemudian menggunakan NavDirections + Safe Args untuk berpindah ke StockDetailFragment.
 */
public class StockAdapter extends ListAdapter<Stock, StockAdapter.StockViewHolder> {

    /**
     * Interface callback untuk event klik item saham.
     * Diimplementasikan oleh DashboardFragment.
     */
    public interface OnStockClickListener {
        void onStockClick(Stock stock);
    }

    private final OnStockClickListener clickListener;

    public StockAdapter(OnStockClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    // DiffUtil callback untuk perbandingan item secara efisien
    private static final DiffUtil.ItemCallback<Stock> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Stock>() {
                @Override
                public boolean areItemsTheSame(@NonNull Stock oldItem, @NonNull Stock newItem) {
                    // Item sama jika simbol sahamnya sama
                    return oldItem.getSymbol().equals(newItem.getSymbol());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Stock oldItem, @NonNull Stock newItem) {
                    // Konten sama jika harga dan perubahan sama
                    return oldItem.getCurrentPrice() == newItem.getCurrentPrice()
                            && oldItem.getChangePercent() == newItem.getChangePercent();
                }
            };

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_card, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock stock = getItem(position);
        holder.bind(stock, clickListener);
    }

    // ================================================================
    // ViewHolder
    // ================================================================

    static class StockViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final TextView tvSymbol;
        private final TextView tvCompanyName;
        private final TextView tvPrice;
        private final TextView tvChangePercent;

        StockViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView       = itemView.findViewById(R.id.card_stock);
            tvSymbol       = itemView.findViewById(R.id.tv_stock_symbol);
            tvCompanyName  = itemView.findViewById(R.id.tv_company_name);
            tvPrice        = itemView.findViewById(R.id.tv_stock_price);
            tvChangePercent = itemView.findViewById(R.id.tv_change_percent);
        }

        void bind(Stock stock, OnStockClickListener listener) {
            // Set data ke view
            tvSymbol.setText(stock.getSymbol());
            tvCompanyName.setText(stock.getCompanyName());
            tvPrice.setText(String.format(Locale.US, "$%.2f", stock.getCurrentPrice()));

            // Format perubahan harga dengan tanda + / -
            double change = stock.getChangePercent();
            String changeText = String.format(Locale.US, "%+.2f%%", change);
            tvChangePercent.setText(changeText);

            // Warna hijau jika naik, merah jika turun
            int colorRes = stock.isPositiveChange()
                    ? R.color.positive_green
                    : R.color.negative_red;

            int color = ContextCompat.getColor(itemView.getContext(), colorRes);
            tvChangePercent.setTextColor(color);

            // Background tonal card juga berubah (subtle)
            int bgColorRes = stock.isPositiveChange()
                    ? R.color.positive_green_surface
                    : R.color.negative_red_surface;
            cardView.setCardBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), bgColorRes)
            );

            // Set click listener - Fragment akan mengurus navigasi ke detail
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStockClick(stock);
                }
            });
        }
    }
}
