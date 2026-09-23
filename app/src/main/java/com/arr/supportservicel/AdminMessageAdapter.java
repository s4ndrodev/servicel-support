package com.arr.supportservicel;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arr.supportservicel.model.AdminMessage;
import com.arr.supportservicel.utils.TimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.arr.supportservicel.R;

public class AdminMessageAdapter extends RecyclerView.Adapter<AdminMessageAdapter.ViewHolder> {

    private final List<AdminMessage> mensajes = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void actualizar(List<AdminMessage> nuevos) {
        mensajes.clear();
        mensajes.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_admin_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AdminMessage mensaje = mensajes.get(position);
        boolean esSoporte = AdminMessage.REMITENTE_SOPORTE.equals(mensaje.getRemitente());

        holder.tvTexto.setText(mensaje.getTexto());
        holder.tvHora.setText(TimeFormatter.formatear(mensaje.getTimestamp()));
        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) holder.bubbleContainer.getLayoutParams();
        if (esSoporte) {
            holder.bubbleContainer.setBackgroundResource(R.drawable.bubble_mine);
            holder.tvTexto.setTextColor(0xFFFFFFFF);
            holder.tvHora.setTextColor(0xB3FFFFFF);
            params.gravity = Gravity.END;
        } else {
            holder.bubbleContainer.setBackgroundResource(R.drawable.bubble_theirs);
            holder.tvTexto.setTextColor(0xFF2A241C);
            holder.tvHora.setTextColor(0xFF6B6156);
            params.gravity = Gravity.START;
        }
        holder.bubbleContainer.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return mensajes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout bubbleContainer;
        final TextView tvTexto;
        final TextView tvHora;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            bubbleContainer = itemView.findViewById(R.id.bubbleContainerAdmin);
            tvTexto = itemView.findViewById(R.id.tvTextoAdmin);
            tvHora = itemView.findViewById(R.id.tvHoraAdmin);
        }
    }
}
