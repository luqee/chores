package com.ntatma.tatua;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by luqi on 7/25/17.
 */

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private List<Category> categoryList;
    private Context context;

    private CategoriesAdapterListener categoriesAdapterListener;

    public interface CategoriesAdapterListener{
        void onCategoryClicked(String name);
    }



    public CategoriesAdapter(Context context, List<Category> categoryList, CategoriesAdapterListener listener){
        this.context = context;
        this.categoryList = categoryList;
        this.categoriesAdapterListener = listener;
    }
    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_category, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CategoriesAdapter.ViewHolder holder, int position) {
        final Category category = categoryList.get(position);
        holder.txtName.setText(category.getName());
        holder.txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                categoriesAdapterListener.onCategoryClicked(category.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView txtName;
        public ViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.textViewName);
        }
    }
}
