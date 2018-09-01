package com.elmaravilla.prowebbrowser.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.elmaravilla.prowebbrowser.R;
import com.elmaravilla.prowebbrowser.adapters.bookmarkAdapter;
import com.elmaravilla.prowebbrowser.model.bookmarksModel;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class bookmarkActivity extends Activity {
    Realm realm;
    RecyclerView recyclerView;
    TextView textView;
    Button btnDelete;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_layout);
        realm = Realm.getDefaultInstance();
        recyclerView = findViewById(R.id.recycler);
        textView = findViewById(R.id.textWarning);
        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.where(bookmarksModel.class).findAll().deleteAllFromRealm();

                    }
                });

            }
        });
        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        OrderedRealmCollection realmCollection = realm.where(bookmarksModel.class).findAll();
        if (realmCollection.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(),"It is empty" , Toast.LENGTH_LONG).show();
        }
        bookmarkAdapter adapter = new bookmarkAdapter (this, realm.where(bookmarksModel.class).findAll() , true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }
}
