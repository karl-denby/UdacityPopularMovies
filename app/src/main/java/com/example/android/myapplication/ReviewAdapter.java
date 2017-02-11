package com.example.android.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{

    private static final String TAG = ReviewAdapter.class.getSimpleName();
    private static int viewHolderCount;
    private int mNumberItems;
    private final String[] mAuthors;
    private final String[] mReviews;

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    ReviewAdapter(int numberOfItems, String[] authors, String[] reviews) {
        mNumberItems = numberOfItems;
        viewHolderCount = 0;
        mAuthors = authors;
        mReviews = reviews;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.recycled_movie_reviews;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        ReviewViewHolder viewHolder = new ReviewViewHolder(view);
        viewHolderCount++;

        return viewHolder;
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ReviewViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        holder.bind(position);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available
     */
    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    // COMPLETED (5) Implement OnClickListener in the NumberViewHolder class
    /**
     * Cache of the children views for a list item.
     */
    class ReviewViewHolder extends RecyclerView.ViewHolder {

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView listReviewAuthorView;
        // Will display which ViewHolder is displaying this data
        TextView listReviewTextView;

        /**
         * Constructor for our ViewHolder.
         * @param itemView The View that you inflated in
         *                 {@link ReviewAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        ReviewViewHolder(View itemView) {
            super(itemView);

            listReviewAuthorView = (TextView) itemView.findViewById(R.id.tv_review_author);
            listReviewTextView = (TextView) itemView.findViewById(R.id.tv_review_text);

        }

        /**
         * given the id of the review we can grab it from an array that contains all authors/reviews
         * @param listIndex Position of the item in the list
         */
        void bind(int listIndex) {
            listReviewAuthorView.setText(mAuthors[listIndex]);
            listReviewTextView.setText(mReviews[listIndex]);
        }
    }

}
