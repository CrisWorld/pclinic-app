package ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import data.dto.ReviewInfo;
import example.pclinic.com.R;

public class PatientReviewAdapter extends RecyclerView.Adapter<PatientReviewAdapter.ReviewViewHolder> {

    private List<ReviewInfo> reviewList;

    public void updateData(List<ReviewInfo> newList) {
        this.reviewList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_item_patient_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewInfo review = reviewList.get(position);
        holder.rbRating.setRating(review.rating);
        holder.tvDoctorName.setText("Đánh giá cho: " + review.doctorName);
        holder.tvDescription.setText(review.description);
        holder.tvDescription.setVisibility(review.description != null && !review.description.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        RatingBar rbRating;
        TextView tvDoctorName, tvDescription;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            rbRating = itemView.findViewById(R.id.rbPatientReviewRating);
            tvDoctorName = itemView.findViewById(R.id.tvPatientReviewDoctorName);
            tvDescription = itemView.findViewById(R.id.tvPatientReviewDescription);
        }
    }
}
