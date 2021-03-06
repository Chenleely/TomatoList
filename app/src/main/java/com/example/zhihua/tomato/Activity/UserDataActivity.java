package com.example.zhihua.tomato.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.model.GuidePage;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.example.zhihua.todolist.R;
import com.example.zhihua.tomato.Adapter.FragmentAdapter;
import com.example.zhihua.tomato.Fragment.RankFragment;
import com.example.zhihua.tomato.Fragment.StatisticFragment;
import com.example.zhihua.tomato.Utils.PhotoUtils;
import com.example.zhihua.tomato.Utils.SPUtils;
import com.example.zhihua.tomato.Bean.User;
import com.example.zhihua.tomato.Widget.CircleImageView;
import com.kekstudio.dachshundtablayout.DachshundTabLayout;
import com.kekstudio.dachshundtablayout.indicators.DachshundIndicator;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.glide.transformations.BlurTransformation;
import me.drakeet.materialdialog.MaterialDialog;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

/**
 * @author zhengzhong on 2016/8/6 16:16
 * Email zheng_zhong@163.com
 */
public class UserDataActivity extends BasicActivity implements View.OnClickListener {
    private static final String TAG = "login";
    @ViewInject(R.id.user_head)
    private CircleImageView user_head;
    @ViewInject(R.id.takePic)
    private Button takePic;
    @ViewInject(R.id.takeGallery)
    private Button takeGallery;
    private static final int CODE_GALLERY_REQUEST = 0xa0;
    private static final int CODE_CAMERA_REQUEST = 0xa1;
    private static final int CODE_RESULT_REQUEST = 0xa2;
    private static final int CAMERA_PERMISSIONS_REQUEST_CODE = 0x03;
    private static final int STORAGE_PERMISSIONS_REQUEST_CODE = 0x04;
    private File fileUri = new File(Environment.getExternalStorageDirectory().getPath() + "/photo.jpg");
    private File fileCropUri = new File(Environment.getExternalStorageDirectory().getPath() + "/crop_photo.jpg");
    private Uri imageUri;
    private Uri
            cropImageUri;
    private User user;
    private FloatingActionButton edit;
    private EditText et_nickname = null;
    private EditText et_autograph = null;
    private TextView tv_nickname;
    private TextView tv_autograph;
    private TextView toolbar_username;
    private Dialog mCameradialog;
    private Toolbar toolbar;
    private ImageView top_bg;
    private CircleImageView toolbar_userhead;
    private String imgPath;
    private ViewPager viewPager;
    private DachshundTabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBar();
        setContentView(R.layout.activity_user_data);
        initView();
        initViewPager();
        saveInformation();
        setUserDataFromBmob();
        glideLoad();
        initGuide();
    }

    private void initView(){
        toolbar_username = (TextView) findViewById(R.id.toolbar_username);
        tv_nickname = (TextView) findViewById(R.id.nickname);
        tv_autograph = (TextView) findViewById(R.id.autograph);
        edit = (FloatingActionButton) findViewById(R.id.user_edit);
        user_head = (CircleImageView) findViewById(R.id.user_head);
        top_bg = (ImageView) findViewById(R.id.top_bg);
        toolbar_userhead = (CircleImageView) findViewById(R.id.toolbar_userhead);

    }

    private void initViewPager() {

        mTabLayout = (DachshundTabLayout) findViewById(R.id.tab_layout_user);
        viewPager = (ViewPager) findViewById(R.id.view_pager_user);
        List<String> titles = new ArrayList<>();
        titles.add("??????");
        titles.add("??????");
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new StatisticFragment());
        fragments.add(new RankFragment());

        viewPager.setOffscreenPageLimit(2);

        FragmentAdapter mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mTabLayout.setAnimatedIndicator(new DachshundIndicator(mTabLayout));
        viewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(viewPager);
        mTabLayout.setTabsFromPagerAdapter(mFragmentAdapter);
    }

    @OnClick({R.id.takePic, R.id.takeGallery})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.takePic:
                mCameradialog.cancel();
                autoObtainCameraPermission();
                break;
            case R.id.takeGallery:
                mCameradialog.cancel();
                autoObtainStoragePermission();
                break;
            default:
        }
    }


    /**
     * ????????????????????????
     */
    private void autoObtainCameraPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                Toasty.info(this, "????????????????????????", Toast.LENGTH_SHORT, true).show();
            }
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, CAMERA_PERMISSIONS_REQUEST_CODE);
        } else {//???????????????????????????????????????
            if (hasSdcard()) {
                imageUri = Uri.fromFile(fileUri);
                //??????FileProvider????????????content?????????Uri
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    imageUri = FileProvider.getUriForFile(UserDataActivity.this, "com.example.fileprovider", fileUri);
                }
                PhotoUtils.takePicture(this, imageUri, CODE_CAMERA_REQUEST);
            } else {
                Toasty.info(this, "????????????SD???", Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            //??????????????????????????????????????????
            case CAMERA_PERMISSIONS_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (hasSdcard()) {
                        imageUri = Uri.fromFile(fileUri);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                            imageUri = FileProvider.getUriForFile(UserDataActivity.this, "com.example.fileprovider", fileUri);//??????FileProvider????????????content?????????Uri
                        PhotoUtils.takePicture(this, imageUri, CODE_CAMERA_REQUEST);
                    } else {
                        Toasty.info(this, "????????????SD???", Toast.LENGTH_SHORT, true).show();
                    }
                } else {
                    Toasty.info(this, "?????????????????????", Toast.LENGTH_SHORT, true).show();
                }
                break;


            }
            //????????????????????????Sdcard????????????
            case STORAGE_PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PhotoUtils.openPic(this, CODE_GALLERY_REQUEST);
                } else {

                    Toasty.info(this, "???????????????SD???", Toast.LENGTH_SHORT, true).show();
                }
                break;
            default:
        }
    }

    private static final int OUTPUT_X = 480;
    private static final int OUTPUT_Y = 480;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                //??????????????????
                case CODE_CAMERA_REQUEST:
                    cropImageUri = Uri.fromFile(fileCropUri);
                    PhotoUtils.cropImageUri(this, imageUri, cropImageUri, 1, 1, OUTPUT_X, OUTPUT_Y, CODE_RESULT_REQUEST);
                    break;
                //????????????????????????
                case CODE_GALLERY_REQUEST:
                    if (hasSdcard()) {
                        cropImageUri = Uri.fromFile(fileCropUri);
                        Uri newUri = Uri.parse(PhotoUtils.getPath(this, data.getData()));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            newUri = FileProvider.getUriForFile(this, "com.example.fileprovider", new File(newUri.getPath()));
                        }
                        PhotoUtils.cropImageUri(this, newUri, cropImageUri, 1, 1, OUTPUT_X, OUTPUT_Y, CODE_RESULT_REQUEST);
                    } else {
                        Toasty.info(this, "????????????SD???", Toast.LENGTH_SHORT, true).show();
                    }
                    break;
                case CODE_RESULT_REQUEST:
                    final Bitmap bitmap = PhotoUtils.getBitmapFromUri(cropImageUri, this);
                    if (bitmap != null) {
                        user.setLocalImg(bitmap);
                        glideLoad();
//                        showImages(bitmap);
//                        Glide.with(getApplicationContext())
//                                .load(bitmap)
//                                .apply(bitmapTransform(new BlurTransformation(25, 3)))
//                                .into(top_bg);

                        String picPath = cropImageUri.getPath();
                        Log.i("login",picPath);
                        final BmobFile bmobFile = new BmobFile(new File(picPath));

                        bmobFile.uploadblock(new UploadFileListener() {

                            @Override
                            public void done(BmobException e) {
                                if (e==null){
                                    user = User.getCurrentUser(User.class);
                                    user.setImg(bmobFile);
                                    user.update(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if (e==null){
                                                Log.i("login", "???????????????" + bmobFile.getUrl());
                                                setUserDataFromBmob();
                                            } else {
                                                Log.i("login", "???????????????" + e.getMessage());
                                            }
                                        }
                                    });
                                }else {
                                    Log.i("login", "????????? " + e.getMessage());
                                }



                            }

                            @Override
                            public void onProgress(Integer value) {
                                // ????????????????????????????????????
                            }
                        });
                    }
                    break;
                default:
            }
        }
    }


    /**
     * ????????????sdk??????
     */

    private void autoObtainStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSIONS_REQUEST_CODE);
        } else {
            PhotoUtils.openPic(this, CODE_GALLERY_REQUEST);
        }

    }

    private void showImages(Bitmap bitmap) {
        user_head.setImageBitmap(bitmap);
    }

    /**
     * ????????????????????????SDCard???????????????
     */
    public static boolean hasSdcard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * ?????????????????????????????????????????????
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent intent = new Intent(UserDataActivity.this, LoginActivity.class);
            setResult(2, intent);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * ??????????????????
     */
    private void saveInformation(){
        user_head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopDialog();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog();
            }});
    }


    /**
     * ?????????????????????
     */
    private void setStatusBar(){
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * ????????????????????????
     */
    private void showPopDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mCameradialog = new Dialog(this, R.style.BottomDialog);
        LinearLayout root = (LinearLayout) inflater.from(this).inflate(R.layout.pop_menu,null);
        root.findViewById(R.id.takePic).setOnClickListener(this);
        root.findViewById(R.id.takeGallery).setOnClickListener(this);
        root.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameradialog.cancel();
            }
        });
        mCameradialog.setContentView(root);
        Window dialogWindow = mCameradialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        //        dialogWindow.setWindowAnimations(R.style.dialogstyle); // ????????????
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // ?????????????????????????????????
        lp.x = 0; // ?????????X??????
        lp.y = 0; // ?????????Y??????
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // ??????
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        lp.alpha = 9f; // ?????????
        dialogWindow.setAttributes(lp);
        mCameradialog.show();
    }

    /**
     * ???????????????????????????
     */
    protected void showEditDialog() {
        User user = User.getCurrentUser(User.class);
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View textEntryView = layoutInflater.inflate(R.layout.dialog_edit, null);
        et_nickname = (EditText) textEntryView.findViewById(R.id.edit_nickname);
        et_autograph = (EditText)textEntryView.findViewById(R.id.edit_autograph);
        et_nickname.setText(user.getNickName());
        et_autograph.setText(user.getAutograph());
        final MaterialDialog editDialog = new MaterialDialog(UserDataActivity.this);
        editDialog.setTitle("????????????");
        editDialog.setView(textEntryView);
        editDialog.setPositiveButton("??????", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nickname = et_nickname.getText().toString();
                String autograph = et_autograph.getText().toString();
                User user = User.getCurrentUser(User.class);
                user.setNickName(nickname);
                user.setAutograph(autograph);
                user.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null){
                            Log.i(TAG, "????????????");
                            setUserDataFromBmob();
                            editDialog.dismiss();
                        }else {
                            Log.i(TAG, "??????"+ e.getMessage());
                        }
                    }
                });


            }
        });
        editDialog.setNegativeButton("??????", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editDialog.dismiss();
            }
        });
        editDialog.show();// ???????????????

    }


    /**
     * ???Bmob??????????????????
     */
    private void setUserDataFromBmob(){
        user = User.getCurrentUser(User.class);
        BmobQuery<User> bmobQuery = new BmobQuery();
        bmobQuery.getObject(user.getObjectId(), new QueryListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                toolbar_username.setText(user.getNickName());
                tv_nickname.setText(user.getNickName());
                tv_autograph.setText(user.getAutograph());
                BmobFile userImg = user.getImg();
                SPUtils.put(UserDataActivity.this,"URL",userImg.getUrl());
                userImg.download(new DownloadFileListener() {
                    @Override
                    public void done(String path, BmobException e) {
                        if(e==null){
                            Log.i(TAG, "????????????: " + path);
                            imgPath = path;
                            SPUtils.put(UserDataActivity.this, "path", imgPath);
                            SPUtils.put(UserDataActivity.this, "head_signature", String.valueOf(System.currentTimeMillis()));//????????????????????????
                            glideLoad();

                        }else{
                            Log.i(TAG, "????????????" + e.getMessage());
                        }
                    }

                    @Override
                    public void onProgress(Integer integer, long l) {

                    }
                });

            }
        });
    }

    /**
     * Glide????????????
     */
    private void glideLoad(){

        RequestOptions options_1 = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new ObjectKey(SPUtils.get(UserDataActivity.this,"head_signature","")))
                .placeholder(R.drawable.default_photo);

        RequestOptions options_2 = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .signature(new ObjectKey(SPUtils.get(UserDataActivity.this,"head_signature","")))
                .placeholder(R.drawable.ic_img1);

        Glide.with(getApplicationContext())
                .load(SPUtils.get(UserDataActivity.this, "path" ,""))
                .apply(options_1)
                .into(toolbar_userhead);

        Glide.with(getApplicationContext())
                .load(SPUtils.get(UserDataActivity.this, "path" ,""))
                .apply(options_1)
                .into(user_head);

        Glide.with(getApplicationContext())
                .load(SPUtils.get(UserDataActivity.this, "path" ,""))
                .apply(bitmapTransform(new BlurTransformation(25, 3)))
                .apply(options_2)
                .into(top_bg);
    }

    /**
     * ????????????
     */
    private void initGuide(){
        NewbieGuide.with(this)
                .setLabel("guide2")
                .setShowCounts(1)//????????????
                .alwaysShow(false)//????????????????????????????????????
                .addGuidePage(GuidePage.newInstance()
                        .addHighLight(user_head)
                        .setLayoutRes(R.layout.guide_user_info))
                .show();

    }


}

