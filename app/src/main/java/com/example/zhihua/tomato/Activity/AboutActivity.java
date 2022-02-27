package com.example.zhihua.tomato.Activity;

import android.support.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.zhihua.todolist.BuildConfig;
import com.example.zhihua.todolist.R;

import me.drakeet.multitype.Items;
import me.drakeet.support.about.AbsAboutActivity;
import me.drakeet.support.about.Card;
import me.drakeet.support.about.Category;

public class AboutActivity extends AbsAboutActivity {

    @Override
    protected void onCreateHeader(@NonNull ImageView icon, @NonNull TextView slogan, @NonNull TextView version) {
        icon.setImageResource(R.mipmap.ic_launcher);
        slogan.setText("生命不息，奋斗不止");
        version.setText("v" + BuildConfig.VERSION_NAME);
    }


    @Override
    protected void onItemsCreated(@NonNull Items items) {
        items.add(new Category("介绍"));
        items.add(new Card(getString(R.string.about_app)));
        items.add(new Category("功能特性"));
        items.add(new Card(getString(R.string.about_function)));

    }
}
