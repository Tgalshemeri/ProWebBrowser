package com.elmaravilla.prowebbrowser.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.elmaravilla.prowebbrowser.R;
import com.elmaravilla.prowebbrowser.Utils.decoration;
import com.elmaravilla.prowebbrowser.model.bookmarksModel;
import com.elmaravilla.prowebbrowser.model.historyModel;

import net.alhazmy13.gota.Gota;
import net.alhazmy13.gota.GotaResponse;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.realm.Realm;

public class MainActivity extends Activity implements Gota.OnRequestPermissionsBack {

 ViewFlipper viewFlipper;
 int i = 0;
 Dialog dialog;
 Bitmap[] bitmaps;
 String bundledText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Gota.Builder(this).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE , Manifest.permission.READ_EXTERNAL_STORAGE)
                .requestId(1)
                .setListener(this)
                .check();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            bundledText = bundle.getString("link");
        }
        Realm.init(this);
        setContentView(R.layout.activity_main);
        viewFlipper = (ViewFlipper)findViewById(R.id.viewFlipper);
        viewFlipper.addView(new customWebView(this , "https://google.com") , 0);
        registerForContextMenu(viewFlipper);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemms to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public void onRequestBack(int requestId, @NonNull GotaResponse gotaResponse) {

    }


    class tabAdapter extends RecyclerView.Adapter<tabAdapter.ViewHolder> {

        private Bitmap[] mData;
        private LayoutInflater mInflater;


        tabAdapter(Context context, Bitmap[] data) {
            this.mInflater = LayoutInflater.from(context);
            this.mData = data;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.custom_dialog, parent, false);
            return new ViewHolder(view);
        }
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {

            holder.itemView.setTag(position);
            holder.imageView.setImageResource(R.drawable.ic_launcher_foreground);
            holder.imageView.setImageBitmap(bitmaps[position]);
            holder.myTextView.setText("Tab " + String.valueOf(position+1));
          holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                        viewFlipper.removeViewAt(position);
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Tab was removed", Toast.LENGTH_LONG).show();

                }
            });
        }
        @Override
        public int getItemCount() {
            return mData.length;
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView myTextView;
            ImageView imageView;
            ImageButton imageButton;
            ViewHolder(final View itemView) {
                super(itemView);
                imageView = (ImageView)itemView.findViewById(R.id.image);
                myTextView = (TextView)itemView.findViewById(R.id.text);
                imageButton = (ImageButton)itemView.findViewById(R.id.button);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (int)view.getTag();
                        viewFlipper.setDisplayedChild(pos);
                        dialog.dismiss();
                    }
                });
            }


        }

    }

    class customWebView extends FrameLayout {
        String url;
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        WebView webView;
        boolean letItSave;
        boolean requestDesktop = false;
        Switch requestSwitch;
        String homeLink;
        SwipeRefreshLayout swipeRefreshLayout;

        public customWebView(Context context, String url) {
            super(context);
            this.url = url;
            initView(context, url);
        }

        public customWebView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context, url);

        }

        public customWebView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onCreateContextMenu(ContextMenu menu) {
            super.onCreateContextMenu(menu);
            final WebView.HitTestResult testResult = webView.getHitTestResult();
            if (testResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                    testResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

                menu.setHeaderTitle(testResult.getExtra());
                menu.add(0, 1, 0, "Save Image").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        String DownloadImageURL = testResult.getExtra();

                        if (URLUtil.isValidUrl(DownloadImageURL)) {

                            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(DownloadImageURL));
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            DownloadManager downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                            downloadManager.enqueue(request);

                            Toast.makeText(getContext(), "Image Downloaded Successfully.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Sorry.. Something Went Wrong.", Toast.LENGTH_LONG).show();
                        }
                        return false;
                    }

                });
            }
        }




        @SuppressLint("ResourceAsColor")
        public void initView(final Context context, String url) {
            View view = inflate(context, R.layout.custom_layout, this);
            // View view = mInflater.inflate(R.layout.custom_layout , null );
            getSharedPrefs();
            final Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);
            toolbar.inflateMenu(R.menu.top_menu);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.showTab:
                            dialog  = new Dialog(MainActivity.this);
                            RecyclerView recyclerView = new RecyclerView(getApplicationContext());
                            recyclerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , LinearLayout.LayoutParams.MATCH_PARENT));
                            dialog.setContentView(recyclerView);
                            bitmaps = new Bitmap[viewFlipper.getChildCount()];
                            for (i=0; i<viewFlipper.getChildCount(); i++){
                                viewFlipper.getChildAt(i).setDrawingCacheEnabled(true);
                                viewFlipper.getChildAt(i).buildDrawingCache(true);
                                bitmaps[i] = viewFlipper.getChildAt(i).getDrawingCache(true);
                            }
                            tabAdapter adapter = new tabAdapter(getApplicationContext() , bitmaps);
                            GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext() , 4 , GridLayoutManager.VERTICAL , false);
                            recyclerView.setLayoutManager(layoutManager);
                            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                                    GridLayout.VERTICAL);
                            recyclerView.setAdapter(adapter);
                            recyclerView.addItemDecoration(new decoration(
                                    5,
                                    4));
                            dialog.show();
                            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                    WindowManager.LayoutParams.WRAP_CONTENT);

                            return true;
                        case R.id.addBtn:
                            if (viewFlipper.getChildCount() == 0){
                                viewFlipper.addView(new customWebView(MainActivity.this , "https://google.com") , 0);
                            } else {
                                viewFlipper.addView(new customWebView(MainActivity.this, "https://google.com"), i);
                                viewFlipper.setDisplayedChild(i);
                                Toast.makeText(getApplicationContext(), "Added new Tab", Toast.LENGTH_LONG).show();
                            }
                            return true;
                    }
                    return false;
                }
            });
            webView = (WebView) view.findViewById(R.id.webView);
            swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefresh);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    webView.reload();
                }
            });


            final BottomNavigationView navigationView = (BottomNavigationView) view.findViewById(R.id.navigationView);
            disableShiftMode(navigationView);
            final com.cielyang.android.clearableedittext.ClearableEditText editText = (com.cielyang.android.clearableedittext.ClearableEditText) view.findViewById(R.id.editUrl);

            editText.clearFocus();



            final FrameLayout frameLayout = (FrameLayout) view.findViewById(R.id.frameLayout);


            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDisplayZoomControls(false);

            //webView.getSettings().setDisplayZoomControls(true);
            //webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
//To Prevent  Web page not available
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (error.getErrorCode() == -2) {
                            view.loadData("", "", null);
                            //To Show Alert Dialog
    //SplashScreenActivity.class is the Launcher Activity
                            // In Case of Frament instead of Activity Replace ClassName.this and getApplicationContext() with getActivity()

                            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setCancelable(false);
                            builder.setTitle(Html.fromHtml("<font color='#000'><b>An Error Occurred </b></font>"));
                            builder.setMessage(Html.fromHtml("<font color='#000'>Your Internet Connection is not working or you entered wrong link.</font>"));
                            builder.setPositiveButton(Html.fromHtml("<font color='#000'><b>OK</b></font>"), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //SplashScreenActivity.class is your Launcher Activity
                                    // In Case of Fragment instead of Activity Replace getApplicationContext()  with getActivity()
                                    dialog.dismiss();
                                    // Intent intent = new Intent(getContext(), SplashScreenActivity.class);


                                }
                            });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    swipeRefreshLayout.setRefreshing(true);
                    editText.setText(view.getUrl());
                }

                @Override
                public void onPageFinished(final WebView view, final String url) {
                    super.onPageFinished(view, url);
                    swipeRefreshLayout.setRefreshing(false);
                    editText.setText(view.getUrl());
                    Realm realm = Realm.getDefaultInstance();

                    if (letItSave == true) {
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                historyModel model = realm.createObject(historyModel.class);
                                model.setUrl(url);
                                model.setTitle(view.getTitle());
                                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                model.setDate(date);
                                if (view.getFavicon() != null) {
                                    Bitmap bitmap = view.getFavicon();
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    byte[] byteArray = stream.toByteArray();
                                    model.setFavicon(byteArray);

                                }
                            }
                        });

                    }


                    bookmarksModel id = realm.where(bookmarksModel.class).equalTo("url", webView.getUrl()).findFirst();

                    if (id != null) {
                        // Exists
                        navigationView.getMenu().getItem(2).setIcon(R.drawable.baseline_bookmark_black_18dp);
                    } else {
                        //Not Existed
                        navigationView.getMenu().getItem(2).setIcon(R.drawable.baseline_bookmark_border_black_18dp);

                    }

                }

                @Override
                public boolean shouldOverrideUrlLoading(final WebView view, String url) {
                    if (url.endsWith("mp3") || url.endsWith("pdf") || url.endsWith("mp4") || url.endsWith("doc") || url.endsWith("apk")) {

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                        //request.setDescription(s2);
                        request.setTitle(view.getTitle());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                            request.allowScanningByMediaScanner();
                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        }
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, view.getTitle());
                        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                        manager.enqueue(request);
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean shouldOverrideUrlLoading(final WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (request.getUrl().toString().endsWith("mp3") || request.getUrl().toString().endsWith("pdf") || request.getUrl().toString()
                                .endsWith("mp4") || request.getUrl().toString().endsWith("doc") || request.getUrl().toString().endsWith("apk")) {

                            DownloadManager.Request request1 = new DownloadManager.Request(request.getUrl());
                            //request.setDescription(s2);
                            request1.setTitle(view.getTitle());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                request1.allowScanningByMediaScanner();
                                request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                            }
                            request1.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, view.getTitle());
                            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                            manager.enqueue(request1);

                            return true;
                        }
                    }
                    return false;
                }
            });
            webView.setWebChromeClient(new WebChromeClient() {

                @Override
                public void onShowCustomView(View view, CustomViewCallback callback) {
                    super.onShowCustomView(view, callback);
                    // if a view already exists then immediately terminate the new one
                    if (mCustomView != null) {
                        callback.onCustomViewHidden();
                        return;
                    }
                    mCustomView = view;
                    webView.setVisibility(View.GONE);
                    editText.setVisibility(View.GONE);
                    toolbar.setVisibility(GONE);
                    frameLayout.setVisibility(View.VISIBLE);
                    frameLayout.addView(view);
                    mCustomViewCallback = callback;

                }

                @Override
                public void onHideCustomView() {
                    super.onHideCustomView();

                    if (mCustomView == null)
                        return;

                    webView.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    toolbar.setVisibility(VISIBLE);
                    frameLayout.setVisibility(View.GONE);
                    // Hide the custom view.
                    mCustomView.setVisibility(View.GONE);

                    // Remove the custom view from its container.
                    frameLayout.removeView(mCustomView);
                    mCustomViewCallback.onCustomViewHidden();

                    mCustomView = null;

                }
            });
            if (requestDesktop) {
                webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; WebView/3.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
                webView.reload();
            }
            if (bundledText != null){
                webView.loadUrl(bundledText);
            } else {
                if (homeLink.toString().startsWith("https://") || homeLink.startsWith("http://")) {
                    webView.loadUrl(homeLink);
                } else {
                    webView.loadUrl("https://" + homeLink.toString());
                }
            }
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                        editText.clearFocus();
                        if (textView.getText().toString().contains("https://") || textView.getText().toString().contains("http://")) {
                            webView.loadUrl(textView.getText().toString());
                        } else {
                            webView.loadUrl("https://" + textView.getText().toString());

                        }
                        return true;
                    }
                    return false;
                }
            });
            navigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.backBtn:
                            if (webView.canGoBack()) {
                                webView.goBack();
                            }
                            return true;
                        case R.id.forwardBtn:
                            if (webView.canGoForward()) {
                                webView.goForward();
                            }
                            return true;
                        case R.id.bookmarkBtn:
                            Realm realm = Realm.getDefaultInstance();
                            bookmarksModel id = realm.where(bookmarksModel.class).equalTo("url", webView.getUrl()).findFirst();

                            if (id != null) {
                                // Exists
                                if (!realm.isInTransaction()) {
                                    realm.beginTransaction();
                                    id.deleteFromRealm();
                                    realm.commitTransaction();
                                    navigationView.getMenu().getItem(2).setIcon(R.drawable.baseline_bookmark_border_black_18dp);
                                }

                            } else {
                                // Not exist
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        bookmarksModel model = realm.createObject(bookmarksModel.class);
                                        model.setUrl(webView.getUrl());
                                        model.setTitle(webView.getTitle());
                                        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                                        model.setDate(date);
                                        if (webView.getFavicon() != null) {
                                            Bitmap bitmap = webView.getFavicon();
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                            byte[] byteArray = stream.toByteArray();
                                            model.setFavicon(byteArray);
                                        }
                                    }
                                });
                                Toast.makeText(getContext(), "Save to Bookmarks", Toast.LENGTH_LONG).show();
                                navigationView.getMenu().getItem(2).setIcon(R.drawable.baseline_bookmark_black_18dp);
                            }
                            return true;


                        case R.id.settingsBtn:
                            final Dialog dialog = new Dialog(getContext());
                            View settingsView = LayoutInflater.from(getContext()).inflate(R.layout.settings_layout, null);
                            dialog.setContentView(settingsView);
                            dialog.show();
                            dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                            TextView aboutText = (TextView)settingsView.findViewById(R.id.about);
                            aboutText.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    Dialog aboutDialog = new Dialog(MainActivity.this);
                                    aboutDialog.setContentView(R.layout.about_layout);
                                    TextView email = (TextView)aboutDialog.findViewById(R.id.textEmail);
                                    email.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                                    "mailto","treafalshamari@gmail.com", null));
                                            intent.putExtra(Intent.EXTRA_SUBJECT, "");
                                            intent.putExtra(Intent.EXTRA_TEXT, "");
                                            startActivity(Intent.createChooser(intent, "Choose an Email client :"));
                                        }
                                    });
                                    aboutDialog.show();
                                    aboutDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
                                }
                            });
                            TextView homeText = (TextView)settingsView.findViewById(R.id.homePage);
                            homeText.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    final Dialog homeDialog = new Dialog(MainActivity.this);
                                    homeDialog.setContentView(R.layout.home_dialog);
                                    final EditText homeEdit = (EditText)homeDialog.findViewById(R.id.editHome);
                                    homeEdit.setText(homeLink);
                                    Button saveBtn = (Button)homeDialog.findViewById(R.id.btnSave);
                                    saveBtn.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            homeDialog.dismiss();
                                            SharedPreferences sharedPreferences = PreferenceManager
                                                    .getDefaultSharedPreferences(getContext());
                                            SharedPreferences.Editor editor = sharedPreferences.edit();
                                            editor.putString("home" , homeEdit.getText().toString());
                                            editor.commit();
                                        }
                                    });
                                    homeDialog.show();
                                    homeDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                                            WindowManager.LayoutParams.WRAP_CONTENT);
                                }
                            });
                            TextView historyText = (TextView) settingsView.findViewById(R.id.historyActivity);
                            historyText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(getContext(), historyActivity.class);
                                    context.startActivity(i);
                                    dialog.dismiss();
                                }
                            });
                            TextView bookmarkText = (TextView) settingsView.findViewById(R.id.bookmarkActivity);
                            bookmarkText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(getContext(), bookmarkActivity.class);
                                    context.startActivity(i);
                                    dialog.dismiss();
                                }
                            });
                            final TextView findOnPage = (TextView) settingsView.findViewById(R.id.findOnPage);
                            findOnPage.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();

                                    webView.showFindDialog("" , true);
                                }
                            });

                            requestSwitch = (Switch) settingsView.findViewById(R.id.requestSwitch);
                            SharedPreferences sharedPreferences = PreferenceManager
                                    .getDefaultSharedPreferences(getContext());
                            requestSwitch.setChecked(sharedPreferences.getBoolean("toggleButton", false));
                            requestSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if (b) {
                                        requestDesktop = true;
                                     //   Toast.makeText(getContext(), String.valueOf(requestDesktop), Toast.LENGTH_LONG).show();
                                        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; WebView/3.0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
                                        webView.reload();
                                    } else {
                                        requestDesktop = false;
                                     //   Toast.makeText(getContext(), String.valueOf(requestDesktop), Toast.LENGTH_LONG).show();
                                        webView.getSettings().setUserAgentString("");
                                        webView.reload();
                                    }
                                    SharedPreferences sharedPreferences = PreferenceManager
                                            .getDefaultSharedPreferences(getContext());
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("toggleButton", b);
                                    editor.commit();
                                    dialog.dismiss();
                                }
                            });


                            final Switch savingSwitch = (Switch) settingsView.findViewById(R.id.savingSwitch);
                            savingSwitch.setChecked(sharedPreferences.getBoolean("saving", true));
                            savingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if (b) {
                                        letItSave = true;
                                        Toast.makeText(getContext(), "Saving History is Enabled", Toast.LENGTH_LONG).show();
                                    } else {
                                        letItSave = false;
                                        Toast.makeText(getContext(), "Saving History is Disable", Toast.LENGTH_LONG).show();
                                    }
                                    SharedPreferences sharedPreferences = PreferenceManager
                                            .getDefaultSharedPreferences(getContext());
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean("saving", b);
                                    editor.commit();
                                    dialog.dismiss();
                                }
                            });
                            return true;
                        default:
                            return true;
                    }

                }
            });


        }

        private void getSharedPrefs() {
            SharedPreferences sharedPreferences = PreferenceManager
                    .getDefaultSharedPreferences(getContext());
            letItSave = sharedPreferences.getBoolean("saving", true);
            requestDesktop = sharedPreferences.getBoolean("toggleButton", false);
            homeLink = sharedPreferences.getString("home" , "https://google.com");


        }

        @SuppressLint("RestrictedApi")
        private void disableShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
            try {
                Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    item.setShiftingMode(false);
                    // set once again checked value, so view will be updated
                    item.setChecked(item.getItemData().isChecked());
                }
            } catch (NoSuchFieldException e) {
                Log.e("BNVHelper", "Unable to get shift mode field", e);
            } catch (IllegalAccessException e) {
                Log.e("BNVHelper", "Unable to change value of shift mode", e);
            }
        }




    }


}
