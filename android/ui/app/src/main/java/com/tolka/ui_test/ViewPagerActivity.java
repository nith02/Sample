package com.tolka.ui_test;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

public class ViewPagerActivity extends AppCompatActivity
{
    private ImageSwitcher mImgSwitcher;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_view_pager );

        {
            mImgSwitcher = findViewById( R.id.img );
            mImgSwitcher.setFactory( new ViewSwitcher.ViewFactory()
            {
                @Override
                public View makeView()
                {
                    ImageView imageView = new ImageView( ViewPagerActivity.this );
                    imageView.setScaleType( ImageView.ScaleType.FIT_CENTER );
                    imageView.setLayoutParams( new ImageSwitcher.LayoutParams( Gallery.LayoutParams.MATCH_PARENT, Gallery.LayoutParams.MATCH_PARENT ) );
                    return imageView;
                }
            } );
            mImgSwitcher.setInAnimation( AnimationUtils.loadAnimation( this, android.R.anim.slide_in_left ) );
            mImgSwitcher.setOutAnimation( AnimationUtils.loadAnimation( this, android.R.anim.slide_out_right ) );
        }
        {
            Button btn = findViewById( R.id.btn_prev );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    mImgSwitcher.setImageResource( R.drawable.img2 );
                }
            } );
        }
        {
            Button btn = findViewById( R.id.btn_next );
            btn.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View view )
                {
                    mImgSwitcher.setImageResource( R.drawable.img3 );
                }
            } );
        }
        {
            ViewPager viewpager = findViewById( R.id.viewpager );
            viewpager.setAdapter( new MyAdapter() );
        }
        {
            ViewPager viewpager = findViewById( R.id.viewpager2 );
            viewpager.setAdapter( new MyAdapter() );
        }
        mImgSwitcher.setImageResource( R.drawable.img1 );
    }

    public class MyAdapter extends PagerAdapter
    {

        @Override
        public int getCount()
        {
            return 3;
        }

        @Override
        public boolean isViewFromObject( View view, Object object )
        {
            return view == object;
        }

        @Override
        public Object instantiateItem( ViewGroup container, int position )
        {
            LayoutInflater inflater = (LayoutInflater)
                    ViewPagerActivity.this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

            View itemView = inflater
                    .inflate( R.layout.item_image, container, false );

            ImageView imageView = (ImageView) itemView.findViewById( R.id.img );
            imageView.setImageResource( R.drawable.img3 );
            container.addView( itemView );

            return itemView;
        }

        @Override
        public void destroyItem( ViewGroup container, int position, Object object )
        {
            container.removeView( (View) object );
        }

        @Nullable
        @Override
        public CharSequence getPageTitle( int position )
        {
            String [] titles = { "tab1", "tab2", "tab3" };
            return titles[position];
        }
    }
}
