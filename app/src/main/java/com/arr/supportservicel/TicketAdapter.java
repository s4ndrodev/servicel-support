package com.arr.supportservicel;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arr.supportservicel.model.Ticket;
import com.arr.supportservicel.utils.TimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.arr.supportservicel.R;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.TicketViewHolder> {

    public interface OnTicketClickListener {
        void onTicketClick(Ticket ticket);
    }

    private final List<Ticket> tickets = new ArrayList<>();
    private final OnTicketClickListener listener;

    public TicketAdapter(OnTicketClickListener listener) {
        this.listener = listener;
    }

    public void actualizar(List<Ticket> nuevosTickets) {
        tickets.clear();
        tickets.addAll(nuevosTickets);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        Ticket ticket = tickets.get(position);

        holder.tvTicketId.setText("Ticket #" + ticket.getId().substring(0, 8).toUpperCase());
        holder.tvUltimoMensaje.setText(
                ticket.getUltimoMensaje().isEmpty() ? "Sin mensajes" : ticket.getUltimoMensaje());
        holder.tvHoraTicket.setText(TimeFormatter.formatear(ticket.getUltimaActualizacion()));
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor("cerrado".equals(ticket.getEstado()) ? 0xFFC9BFA8 : 0xFF3E7A4F);
        holder.dotEstado.setBackground(dot);

        holder.itemView.setOnClickListener(v -> listener.onTicketClick(ticket));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class TicketViewHolder extends RecyclerView.ViewHolder {
        final View dotEstado;
        final TextView tvTicketId, tvHoraTicket;
        final TextView tvUltimoMensaje;

        TicketViewHolder(@NonNull View itemView) {
            super(itemView);
            dotEstado = itemView.findViewById(R.id.dotEstado);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvUltimoMensaje = itemView.findViewById(R.id.tvUltimoMensaje);
            tvHoraTicket = itemView.findViewById(R.id.tvHoraTicket);
        }
    }
}
