package com.example.tickertide.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tickertide.R;
import com.example.tickertide.model.PriceAlert;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlertsAdapter extends RecyclerView.Adapter<AlertsAdapter.AlertsViewHolder> {

    private List<PriceAlert> items = new ArrayList<>();
    
    public void submitList(List<PriceAlert> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public PriceAlert getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public AlertsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_card, parent, false);
        return new AlertsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlertsViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AlertsViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSymbol;
        private final TextView tvCompanyName;
        private final TextView tvPrice;
        private final TextView tvChangePercent;
        private final TextView tvSymbolLetter;
        private final android.widget.ImageView ivLogo;

        AlertsViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymbol       = itemView.findViewById(R.id.tv_stock_symbol);
            tvCompanyName  = itemView.findViewById(R.id.tv_company_name);
            tvPrice        = itemView.findViewById(R.id.tv_stock_price);
            tvChangePercent = itemView.findViewById(R.id.tv_change_percent);
            tvSymbolLetter  = itemView.findViewById(R.id.tv_symbol_letter);
            ivLogo          = itemView.findViewById(R.id.iv_stock_logo);
        }

        void bind(PriceAlert item) {
            tvSymbol.setText(item.getSymbol());
            
            String condition = item.isAbove() ? "Bila harga naik melewati" : "Bila harga turun di bawah";
            tvCompanyName.setText(condition);
            
            tvPrice.setText(String.format(Locale.US, "$%.2f", item.getTargetPrice()));

            if (item.getSymbol() != null && !item.getSymbol().isEmpty()) {
                tvSymbolLetter.setText(item.getSymbol().substring(0, 1).toUpperCase());
                tvSymbolLetter.setVisibility(View.VISIBLE);
            }

            String logoUrl = "https://financialmodelingprep.com/image-stock/" + item.getSymbol() + ".png";
            com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(logoUrl)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            tvSymbolLetter.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivLogo);

            tvChangePercent.setText(item.isActive() ? "Aktif" : "Selesai");
            int colorRes = item.isActive() ? R.color.positive_green : R.color.text_secondary;
            tvChangePercent.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));
        }
    }
}
