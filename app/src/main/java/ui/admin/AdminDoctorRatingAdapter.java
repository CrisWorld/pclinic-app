package ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;import java.util.List;
import java.util.Locale;
import data.dto.DoctorRatingInfo;
import example.pclinic.com.R;

public class AdminDoctorRatingAdapter extends RecyclerView.Adapter<AdminDoctorRatingAdapter.ViewHolder> {

    private List<DoctorRatingInfo> topDoctors;

    public void setData(List<DoctorRatingInfo> doctors) {
        this.topDoctors = doctors;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_top_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DoctorRatingInfo doctor = topDoctors.get(position);
        holder.tvDoctorName.setText(doctor.doctorName);
        holder.tvRating.setText(String.format(Locale.US, "%.1f ★", doctor.averageRating));
    }

    @Override
    public int getItemCount() {
        return topDoctors != null ? topDoctors.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvRating;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvTopDoctorName);
            tvRating = itemView.findViewById(R.id.tvTopDoctorRating);
        }
    }
}
