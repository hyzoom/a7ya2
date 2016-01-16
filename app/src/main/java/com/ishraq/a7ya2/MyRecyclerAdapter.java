package com.ishraq.a7ya2;

/**
 * Created by hp on 01/01/2016.
 */

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.CustomViewHolder> {
    private List<FeedItem> feedItemList;
    private Context mContext;

    public MyRecyclerAdapter(Context context, List<FeedItem> feedItemList) {
        this.feedItemList = feedItemList;
        this.mContext = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_row, null);

        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    private View.OnClickListener onClickListener(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedItem feedItem = feedItemList.get(position);
                String sentTitle = feedItem.getTitle();
                String sentContent = feedItem.getContent();
                String sentImage = feedItem.getThumbnail();

                Intent i = new Intent(mContext, SingleSubject.class);
                i.putExtra("sentTitle", Html.fromHtml(sentTitle).toString().replace("*", "\n"));
                i.putExtra("sentContent", Html.fromHtml(sentContent).toString().replace("*", "\n"));
                i.putExtra("sentImage", sentImage);
                mContext.startActivity(i);
            }
        };
    }

    @Override
    public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
        FeedItem feedItem = feedItemList.get(i);

        //Download image using picasso library
        Picasso.with(mContext).load(feedItem.getThumbnail())
                .error(R.drawable.back)
                .placeholder(R.drawable.back)
                .into(customViewHolder.imageView);


        //Setting text view title
        customViewHolder.textView.setText(Html.fromHtml(feedItem.getTitle()));

//
        customViewHolder.textView.setOnClickListener(onClickListener(i));
        customViewHolder.imageView.setOnClickListener(onClickListener(i));
    }

    @Override
    public int getItemCount() {
        return (null != feedItemList ? feedItemList.size() : 0);
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView imageView;
        protected TextView textView;

        public CustomViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
            this.textView = (TextView) view.findViewById(R.id.title);

        }
    }
}