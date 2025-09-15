package com.example.tenantapp.ui;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.*;
import com.example.tenantapp.R;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.VH> {

    private final List<Map<String,Object>> items;

    public TenantAdapter(List<Map<String,Object>> items) {
        this.items = items;
    }

    public void replace(List<Map<String,Object>> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_tenant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Map<String,Object> m = items.get(pos);
        h.tvName.setText(s(m.get("full_name")));
        String unit = s(m.get("unit_number"));
        String floor = m.get("floor") != null ? String.valueOf(m.get("floor")) : "-";
        h.tvUnit.setText("Unit " + unit + " • Floor " + floor);
        h.tvPhone.setText(s(m.get("phone")));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static String s(Object o) { return o == null ? "" : String.valueOf(o); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvUnit, tvPhone;
        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvTenantName);
            tvUnit = v.findViewById(R.id.tvTenantUnit);
            tvPhone = v.findViewById(R.id.tvTenantPhone);
        }
    }
}
