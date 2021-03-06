package az.com.newazhong.workoffice.activity;

import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import az.com.newazhong.R;
import az.com.newazhong.utilsclass.base.BaseActivity;
import az.com.newazhong.utilsclass.myViews.Header;
import az.com.newazhong.utilsclass.utils.ProgressDialogUtil;
import az.com.newazhong.workoffice.adapter.TypeFixDetailAdapter;
import az.com.newazhong.workoffice.bean.TypeFix;
import butterknife.Bind;
import butterknife.ButterKnife;

public class TypeFixTypeActivity extends BaseActivity implements TypeFixDetailAdapter.CallBackPosition {

    @Bind(R.id.header)
    Header header;
    @Bind(R.id.tvTitle)
    TextView tvTitle;
    @Bind(R.id.tvDepartment)
    TextView tvDepartment;
    @Bind(R.id.tvTime)
    TextView tvTime;

    TypeFix.DataBean.ContentBean typeFix;
    @Bind(R.id.webView)
    WebView webView;
    int width;
    String headerT;
    @Bind(R.id.recyclerView)
    RecyclerView recyclerView;

    List<String> imageList = new ArrayList<>();
    List<String> fileList = new ArrayList<>();
    @Bind(R.id.tvAddress)
    TextView tvAddress;
    @Bind(R.id.activity_noticeand_detail)
    LinearLayout activityNoticeandDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        ProgressDialogUtil.startLoad(this, "解析数据中");

        //支持javascript
        webView.getSettings().setJavaScriptEnabled(true);
        // 设置可以支持缩放
        webView.getSettings().setSupportZoom(false);
        // 设置出现缩放工具
        webView.getSettings().setBuiltInZoomControls(false);
        //扩大比例的缩放
        webView.getSettings().setUseWideViewPort(true);
        //自适应屏幕
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setTextSize(WebSettings.TextSize.LARGEST);
        Intent intent = getIntent();
        typeFix = (TypeFix.DataBean.ContentBean) intent.getSerializableExtra("bean");
        headerT = intent.getStringExtra("header");
        header.setTvTitle(headerT);
        tvTitle.setText(typeFix.getTitle());
        tvDepartment.setText(typeFix.getDepartmentName());
        tvTime.setText(typeFix.getCreateTime());
        tvAddress.setText(typeFix.getAddress());
        if (typeFix.getPictures() != null && typeFix.getPictures().size() != 0) {
            String path1 = typeFix.getPictures().toString();
            Log.e("XXXH", path1);
            String newString = path1.toString().replace("[", "");
            String newString1 = newString.toString().replace("]", "");
            Log.e("XXXH", newString1);
            String[] temp = null;
            temp = newString1.split(",");
            Log.e("XXXH", temp[0]);
            for (int i = 0; i < temp.length; i++) {
                String path = temp[i];
                if (path.indexOf(".png") != -1 || path.indexOf(".jpeg") != -1 || path.indexOf(".jpg") != -1) {
                    imageList.add(path);
                } else {
                    String [] temp1 = null;
                    temp1 = path.split("/");
                    int size = temp1.length;
                    path = temp1[size-1];
                    fileList.add(path);
                }
            }
        }
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setNestedScrollingEnabled(false);
        TypeFixDetailAdapter adapter = new TypeFixDetailAdapter(this, fileList);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemLitener(this);


        Display display = getWindowManager().getDefaultDisplay();
        // 方法一(推荐使用)使用Point来保存屏幕宽、高两个数据
        Point outSize = new Point();
        // 通过Display对象获取屏幕宽、高数据并保存到Point对象中
        display.getSize(outSize);
        // 从Point对象中获取宽、高
        width = outSize.x;
        int y = outSize.y;

        int num = 0;
        String imagePath = "";
        while (num < imageList.size()) {
            if (num == 0) {
                imagePath = "<img src=\"" + imagePath + imageList.get(num) + "\" style=\"height:229px; width:376px\" />";
                num += 1;
            } else {
                imagePath = imagePath + "<img src=\"" + imageList.get(num) + "\" style=\"height:229px; width:376px\" />";
                num += 1;
            }
        }
        webView.loadDataWithBaseURL(null, typeFix.getContent() + imagePath, "text/html", "utf-8", null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ProgressDialogUtil.stopLoad();
            }
        }, 1000);
    }

    @Override
    public void onItemClick(int position) {
        String path = fileList.get(position);
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        Uri content_url = Uri.parse(path);//此处填链接
        intent.setData(content_url);
        startActivity(intent);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            imgReset();//重置webview中img标签的图片大小
            // html加载完成之后，添加监听图片的点击js函数
            //addImageClickListner();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    /**
     * 对图片进行重置大小，宽度就是手机屏幕宽度，高度根据宽度比便自动缩放
     **/
    private void imgReset() {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName('img'); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "var img = objs[i];   " +
                "    img.style.maxWidth = '100%'; img.style.height = 'auto'; img.style.width = +" + width * 2 + "+'px';  " +
                "}" +
                "})()");
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_typefix_detail;
    }

    @Override
    protected boolean isHasHeader() {
        return true;
    }

    @Override
    protected void rightClient() {

    }
}
