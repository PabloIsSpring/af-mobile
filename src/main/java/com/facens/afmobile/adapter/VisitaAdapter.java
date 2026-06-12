package com.facens.afmobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.facens.afmobile.model.Visita;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class VisitaAdapter extends RecyclerView.Adapter<VisitaAdapter.ViewHolder>{

    private List<Visita> visitas;
    private OnItemClickListener listener;
    private OnItemDeleteListener deleteListener;

    public VisitaAdapter (List<Visita> visitas) {
        this.visitas = visitas;
    }

    public interface OnItemClickListener {
        void onItemClick(Visita v);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(Visita v, int position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int pos) {
        Visita vi = visitas.get(pos);
        holder.txt1.setText(vi.getTitulo()+" - "+ vi.getData()+" - "+vi.getCategoria());
        holder.txt2.setText(vi.getTemperatura()+"C°"+" - "+vi.getCondicaoTempo()+" - "+" - "+vi.getDescricao()+"\n"+
                            vi.getLatitude()+" - "+vi.getLongitude());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(vi);
            }
        });

        holder.itemView.setOnTouchListener(new View.OnTouchListener() {
            private long lastClickTime = 0;

            @Override
            public boolean onTouch(View v, android.view.MotionEvent event) {
                if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                    long currentTime = System.currentTimeMillis();

                    if (currentTime - lastClickTime < 300) {
                        int position = holder.getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION && deleteListener != null) {
                            deleteListener.onItemDelete(vi, position);
                        }
                    }
                    lastClickTime = currentTime;
                }
                return false;
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txt1, txt2;
        public ViewHolder(View itemView) {
            super(itemView);
            txt1 = itemView.findViewById(android.R.id.text1);
            txt2 = itemView.findViewById(android.R.id.text2);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setOnItemDeleteListener(OnItemDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    @Override
    public int getItemCount() {
        return visitas.size();
    }
}
