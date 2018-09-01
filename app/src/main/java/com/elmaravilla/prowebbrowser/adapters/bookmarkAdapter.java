package com.elmaravilla.prowebbrowser.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.elmaravilla.prowebbrowser.R;
import com.elmaravilla.prowebbrowser.activities.MainActivity;
import com.elmaravilla.prowebbrowser.model.bookmarksModel;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;

public class bookmarkAdapter extends RealmRecyclerViewAdapter<bookmarksModel , bookmarkAdapter.MyViewHolder> {
    Context context;
    RealmResults<bookmarksModel> realmResults;
    Realm realm;
    public bookmarkAdapter(Context context  , @Nullable OrderedRealmCollection<bookmarksModel> data, boolean autoUpdate) {
        super(data, autoUpdate);
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.model_item, parent, false);
        realm = Realm.getDefaultInstance();
        realmResults = realm.where(bookmarksModel.class).findAll();
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        final bookmarksModel obj = getData().get(position);
        holder.itemView.setTag(position);
        holder.data = obj;
        holder.title.setText(obj.getTitle());
        holder.url.setText(obj.getUrl());
        if (obj.getFavicon() != null){
            byte[] favicon = obj.getFavicon();
            Bitmap bitmap = BitmapFactory.decodeByteArray(favicon , 0 , favicon.length);
            holder.imgFavicon.setImageBitmap(bitmap);
        }
     holder.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realmResults.get(position).deleteFromRealm();
                    }
                });
            }
        });

    }
    @Override
    public long getItemId(int index) {
        //noinspection ConstantConditions
        return getItemCount();
    }
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public TextView title , url;
        public ImageView imgFavicon;
        public bookmarksModel data;
        public ImageButton imageButton;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.textTitle);
            url = (TextView)view.findViewById(R.id.textUrl);
            imgFavicon = (ImageView)view.findViewById(R.id.imgFavicon);
            imageButton = (ImageButton) view.findViewById(R.id.removeItem);
            view.setOnLongClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = (int) view.getTag();
                    Intent i = new Intent(context , MainActivity.class);
                    i.putExtra("link" , url.getText().toString());
                    context.startActivity(i);

                }
            });
        }

        @Override
        public boolean onLongClick(View v) {
            //activity.deleteItem(data);
            return true;
        }
    }
}
