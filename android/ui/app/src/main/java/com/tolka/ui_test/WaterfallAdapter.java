package com.tolka.ui_test;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class WaterfallAdapter extends RecyclerView.Adapter<WaterfallAdapter.ViewHolder>
{
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        private TextView mTextTitle;
        private ImageView mImage;

        public ViewHolder( View v )
        {
            super( v );
            mTextTitle = (TextView) v.findViewById( R.id.text );
            mImage = v.findViewById( R.id.img );
        }
    }

    @Override
    public WaterfallAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
    {
        View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.item_waterfall, parent, false );
        return new ViewHolder( view );
    }

    @Override
    public void onBindViewHolder( WaterfallAdapter.ViewHolder holder, int position )
    {
        holder.mTextTitle.setText( "test title" + position );
        if ( position % 3 == 0)
        {
            holder.mImage.setImageResource( R.drawable.img1 );
        }
        else if ( position % 3 == 1)
        {
            holder.mImage.setImageResource( R.drawable.img2 );
        }
        else
        {
            holder.mImage.setImageResource( R.drawable.img3 );
        }
    }

    @Override
    public int getItemCount()
    {
        return 10;
    }
}
