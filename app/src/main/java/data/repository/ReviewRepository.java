package data.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import data.db.ReviewDao;
import data.model.Review;

@Singleton
public class ReviewRepository {

    private final ReviewDao reviewDao;

    @Inject
    public ReviewRepository(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    public long create(Review review) {
        return reviewDao.insert(review);
    }

    public void update(Review review) {
        Executors.newSingleThreadExecutor().execute(() -> {
            reviewDao.update(review);
        });
    }

    public MutableLiveData<List<Review>> getRecentByDoctor(long doctorId, int limit) {
        MutableLiveData<List<Review>> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Review> reviews = reviewDao.findRecentByDoctor(doctorId, limit);
            result.postValue(reviews);
        });
        return result;
    }

    public MutableLiveData<Double> getAverageRating(long doctorId) {
        MutableLiveData<Double> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            Double avgRating = reviewDao.getAverageRatingByDoctor(doctorId);
            result.postValue(avgRating != null ? avgRating : 0.0);
        });
        return result;
    }

    public MutableLiveData<Integer> getTotalReviews(long doctorId) {
        MutableLiveData<Integer> result = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            int count = reviewDao.countByDoctor(doctorId);
            result.postValue(count);
        });
        return result;
    }
}
