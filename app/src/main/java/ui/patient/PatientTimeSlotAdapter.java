package ui.patient;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import example.pclinic.com.R;
import ui.doctor.TimeSlot;

public class PatientTimeSlotAdapter extends RecyclerView.Adapter<PatientTimeSlotAdapter.ViewHolder> {

    private List<TimeSlot> timeSlots = new ArrayList<>();
    private OnSlotSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnSlotSelectedListener {
        void onSlotSelected(TimeSlot slot);
    }

    public void setTimeSlots(List<TimeSlot> slots) {
        this.timeSlots = slots;
        selectedPosition = -1; // Reset selection when slots change
        notifyDataSetChanged();
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public TimeSlot getSelectedSlot() {
        if (selectedPosition >= 0 && selectedPosition < timeSlots.size()) {
            return timeSlots.get(selectedPosition);
        }
        return null;
    }

    public void setOnSlotSelectedListener(OnSlotSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TimeSlot slot = timeSlots.get(position);
        holder.bind(slot, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return timeSlots.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private TextView tvTimeSlot;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_time_slot);
            tvTimeSlot = itemView.findViewById(R.id.tv_time_slot);
        }

        void bind(TimeSlot slot, boolean isSelected) {
            tvTimeSlot.setText(slot.getDisplayTime());

            // Update UI based on disabled and selected state
            if (slot.isDisabled()) {
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.error));
                tvTimeSlot.setTextColor(itemView.getContext().getColor(R.color.white));
                tvTimeSlot.setText(slot.getDisplayTime() + "\n(Đã đặt)");
                cardView.setAlpha(0.6f);
                itemView.setOnClickListener(null);
                itemView.setClickable(false);
            } else if (isSelected) {
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.colorPrimary));
                tvTimeSlot.setTextColor(itemView.getContext().getColor(R.color.white));
                cardView.setAlpha(1.0f);
                itemView.setClickable(true);
            } else {
                cardView.setCardBackgroundColor(itemView.getContext().getColor(R.color.surfaceLight));
                tvTimeSlot.setTextColor(itemView.getContext().getColor(R.color.textPrimary));
                cardView.setAlpha(1.0f);
                itemView.setClickable(true);
            }

            if (!slot.isDisabled()) {
                itemView.setOnClickListener(v -> {
                    int previousPosition = selectedPosition;
                    selectedPosition = getAdapterPosition();
                    
                    // Notify previous selection changed
                    if (previousPosition != -1) {
                        notifyItemChanged(previousPosition);
                    }
                    // Notify new selection changed
                    notifyItemChanged(selectedPosition);
                    
                    // Notify listener
                    if (listener != null) {
                        listener.onSlotSelected(slot);
                    }
                });
            }
        }
    }
}

