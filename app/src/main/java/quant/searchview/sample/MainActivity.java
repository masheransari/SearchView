package quant.searchview.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * update by cz on 2016/11/26
 * 此搜索库,来自https://github.com/lapism/SearchView
 * 在此基本上,做了以下修改
 * 1:修改其原先左侧的arrow标记
 * 2:修改其改历史排序逻辑,让每次确认提交搜索的记录,时时动态修新
 * 3:移除左侧声音搜索,移除顶部筛选,简化布局
 * 4:增加默认style配置,移除无用部分attr引用,用户可更简单配置其初始样式
 * 5:重构大量搜索搜索逻辑,原作者搜索块bug较多.增加默认key保存历史,并完善本地列搜索与在线列搜索逻辑
 * 6:重构其历史表搜索以及更新逻辑,优化其动态筛选逻辑
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        findViewById(R.id.btn_local).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,LocalSearchActivity.class));
            }
        });

        findViewById(R.id.btn_dynamic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,DynamicSearchActivity.class));
            }
        });
    }
}
