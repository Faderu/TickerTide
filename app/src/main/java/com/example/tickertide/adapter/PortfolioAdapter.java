package com.example.tickertide.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tickertide.R;
import com.example.tickertide.model.PortfolioItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.PortfolioViewHolder> {

    private List<PortfolioItem> items = new ArrayList<>();
    private final OnPortfolioClickListener listener;

    public interface OnPortfolioClickListener {
        void onPortfolioClick(PortfolioItem item);
    }

    public PortfolioAdapter(OnPortfolioClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<PortfolioItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PortfolioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_card, parent, false); // Reusing the same card layout
        return new PortfolioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PortfolioViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PortfolioViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvSymbol;
        private final TextView tvCompanyName;
        private final TextView tvPrice;
        private final TextView tvChangePercent;
        private final TextView tvSymbolLetter;
        private final android.widget.ImageView ivLogo;

        PortfolioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSymbol       = itemView.findViewById(R.id.tv_stock_symbol);
            tvCompanyName  = itemView.findViewById(R.id.tv_company_name);
            tvPrice        = itemView.findViewById(R.id.tv_stock_price);
            tvChangePercent = itemView.findViewById(R.id.tv_change_percent);
            tvSymbolLetter  = itemView.findViewById(R.id.tv_symbol_letter);
            ivLogo          = itemView.findViewById(R.id.iv_stock_logo);
        }

        void bind(PortfolioItem item, OnPortfolioClickListener listener) {
            tvSymbol.setText(item.getSymbol());
            
            // Re-purpose company name to show shares
            String sharesText = String.format(Locale.US, "%.2f Shares", item.getShares());
            tvCompanyName.setText(sharesText);
            
            tvPrice.setText(String.format(Locale.US, "$%.2f", item.getTotalValue()));

            // Initial letter
            if (item.getSymbol() != null && !item.getSymbol().isEmpty()) {
                tvSymbolLetter.setText(item.getSymbol().substring(0, 1).toUpperCase());
                tvSymbolLetter.setVisibility(View.VISIBLE);
            }

            // Load logo via Glide
            String logoUrl = "https://financialmodelingprep.com/image-stock/" + item.getSymbol() + ".png";
            com.bumptech.glide.Glide.with(itemView.getContext())
                    .load(logoUrl)
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(@androidx.annotation.Nullable com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            return false; // biarkan letter text tetap terlihat sebagai fallback
                        }
                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            tvSymbolLetter.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(ivLogo);

            // Format profit/loss
            double plPercent = item.getProfitLossPercent();
            String changeText = String.format(Locale.US, "%+.2f%%", plPercent);
            tvChangePercent.setText(changeText);

            int colorRes = plPercent >= 0 ? R.color.positive_green : R.color.negative_red;
            tvChangePercent.setTextColor(ContextCompat.getColor(itemView.getContext(), colorRes));

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPortfolioClick(item);
            });
        }
    }
}
